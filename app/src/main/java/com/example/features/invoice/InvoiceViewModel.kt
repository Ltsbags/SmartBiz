package com.example.features.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.core.database.entity.InvoiceWithItems
import com.example.repositories.CustomerRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filterState = MutableStateFlow(InvoiceFilterState())
    private val sortOption = MutableStateFlow(InvoiceSortOption.NEWEST)
    private val selectedInvoiceForDetails = MutableStateFlow<InvoiceWithItems?>(null)
    private val selectedInvoiceForEdit = MutableStateFlow<InvoiceWithItems?>(null)
    private val isCreateInvoiceOpen = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)

    val nextInvoiceNumber = MutableStateFlow("INV-2026-001")

    init {
        loadNextInvoiceNumber()
    }

    private fun loadNextInvoiceNumber() {
        viewModelScope.launch {
            nextInvoiceNumber.value = invoiceRepository.generateNextInvoiceNumber()
        }
    }

    private data class FilterOptions(
        val query: String,
        val filter: InvoiceFilterState,
        val sort: InvoiceSortOption
    )

    private val filterOptionsFlow = combine(
        searchQuery,
        filterState,
        sortOption
    ) { query, filter, sort ->
        FilterOptions(query, filter, sort)
    }

    val uiState: StateFlow<InvoiceUiState> = combine(
        invoiceRepository.allInvoicesWithItems,
        inventoryRepository.allItems,
        customerRepository.allCustomers,
        filterOptionsFlow
    ) { rawInvoices, rawProducts, rawCustomers, options ->

        var filtered = rawInvoices

        // 1. Search Query Filter
        if (options.query.isNotBlank()) {
            filtered = filtered.filter { invWithItems ->
                val inv = invWithItems.invoice
                inv.invoiceNumber.contains(options.query, ignoreCase = true) ||
                        inv.customerName.contains(options.query, ignoreCase = true) ||
                        inv.customerPhone.contains(options.query, ignoreCase = true)
            }
        }

        // 2. Status Filter
        if (options.filter.statusFilter != "ALL") {
            filtered = filtered.filter { it.invoice.status == options.filter.statusFilter }
        }

        // 3. Payment Status Filter
        if (options.filter.paymentStatusFilter != "ALL") {
            filtered = filtered.filter { it.invoice.paymentStatus == options.filter.paymentStatusFilter }
        }

        // 4. Date Filter
        if (options.filter.dateFilter != "ALL") {
            val nowCal = Calendar.getInstance()
            val startOfPeriod = when (options.filter.dateFilter) {
                "TODAY" -> {
                    nowCal.set(Calendar.HOUR_OF_DAY, 0)
                    nowCal.set(Calendar.MINUTE, 0)
                    nowCal.set(Calendar.SECOND, 0)
                    nowCal.set(Calendar.MILLISECOND, 0)
                    nowCal.timeInMillis
                }
                "THIS_WEEK" -> {
                    nowCal.set(Calendar.DAY_OF_WEEK, nowCal.firstDayOfWeek)
                    nowCal.set(Calendar.HOUR_OF_DAY, 0)
                    nowCal.set(Calendar.MINUTE, 0)
                    nowCal.set(Calendar.SECOND, 0)
                    nowCal.set(Calendar.MILLISECOND, 0)
                    nowCal.timeInMillis
                }
                "THIS_MONTH" -> {
                    nowCal.set(Calendar.DAY_OF_MONTH, 1)
                    nowCal.set(Calendar.HOUR_OF_DAY, 0)
                    nowCal.set(Calendar.MINUTE, 0)
                    nowCal.set(Calendar.SECOND, 0)
                    nowCal.set(Calendar.MILLISECOND, 0)
                    nowCal.timeInMillis
                }
                else -> 0L
            }
            filtered = filtered.filter { it.invoice.date >= startOfPeriod }
        }

        // 5. Sorting
        filtered = when (options.sort) {
            InvoiceSortOption.NEWEST -> filtered.sortedByDescending { it.invoice.date }
            InvoiceSortOption.OLDEST -> filtered.sortedBy { it.invoice.date }
            InvoiceSortOption.AMOUNT_HIGH_TO_LOW -> filtered.sortedByDescending { it.invoice.totalAmount }
            InvoiceSortOption.AMOUNT_LOW_TO_HIGH -> filtered.sortedBy { it.invoice.totalAmount }
        }

        // Calculations
        val totalRevenue = rawInvoices
            .filter { it.invoice.status == "COMPLETED" || it.invoice.paymentStatus == "PAID" }
            .sumOf { it.invoice.totalAmount }

        val pendingAmount = rawInvoices
            .filter { it.invoice.status != "CANCELLED" }
            .sumOf { it.invoice.balanceAmount }

        InvoiceUiState(
            invoices = filtered,
            customers = rawCustomers,
            products = rawProducts,
            currencySymbol = settingsRepository.getCurrencySymbol(),
            searchQuery = options.query,
            filterState = options.filter,
            sortOption = options.sort,
            totalRevenue = totalRevenue,
            pendingAmount = pendingAmount,
            totalInvoicesCount = rawInvoices.size,
            isLoading = false,
            selectedInvoiceForDetails = selectedInvoiceForDetails.value,
            selectedInvoiceForEdit = selectedInvoiceForEdit.value,
            isCreateInvoiceOpen = isCreateInvoiceOpen.value,
            userMessage = userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InvoiceUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilterState(filter: InvoiceFilterState) {
        filterState.value = filter
    }

    fun updateSortOption(sort: InvoiceSortOption) {
        sortOption.value = sort
    }

    fun openCreateInvoice() {
        viewModelScope.launch {
            loadNextInvoiceNumber()
            isCreateInvoiceOpen.value = true
        }
    }

    fun closeCreateInvoice() {
        isCreateInvoiceOpen.value = false
    }

    fun selectInvoiceForDetails(invoice: InvoiceWithItems?) {
        selectedInvoiceForDetails.value = invoice
    }

    fun selectInvoiceForEdit(invoice: InvoiceWithItems?) {
        selectedInvoiceForEdit.value = invoice
    }

    fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        isComplete: Boolean
    ) {
        viewModelScope.launch {
            try {
                val finalStatus = if (isComplete) "COMPLETED" else "DRAFT"
                val invoiceToSave = invoice.copy(status = finalStatus)

                invoiceRepository.saveInvoice(invoiceToSave, items)

                userMessage.value = if (isComplete) {
                    "Invoice #${invoiceToSave.invoiceNumber} completed successfully!"
                } else {
                    "Invoice #${invoiceToSave.invoiceNumber} saved as draft."
                }

                isCreateInvoiceOpen.value = false
                selectedInvoiceForEdit.value = null
                loadNextInvoiceNumber()
            } catch (e: Exception) {
                userMessage.value = "Failed to save invoice: ${e.localizedMessage}"
            }
        }
    }

    fun completeInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                invoiceRepository.completeInvoice(invoiceId)
                userMessage.value = "Invoice status set to COMPLETED."
                selectedInvoiceForDetails.value = invoiceRepository.getInvoiceWithItemsById(invoiceId)
            } catch (e: Exception) {
                userMessage.value = "Error completing invoice: ${e.localizedMessage}"
            }
        }
    }

    fun cancelInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                invoiceRepository.cancelInvoice(invoiceId)
                userMessage.value = "Invoice marked as CANCELLED."
                selectedInvoiceForDetails.value = invoiceRepository.getInvoiceWithItemsById(invoiceId)
            } catch (e: Exception) {
                userMessage.value = "Error cancelling invoice: ${e.localizedMessage}"
            }
        }
    }

    fun duplicateInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                val newId = invoiceRepository.duplicateInvoice(invoiceId)
                if (newId != null) {
                    userMessage.value = "Invoice duplicated as new draft!"
                    loadNextInvoiceNumber()
                }
            } catch (e: Exception) {
                userMessage.value = "Error duplicating invoice: ${e.localizedMessage}"
            }
        }
    }

    fun deleteInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                invoiceRepository.deleteInvoice(invoiceId)
                userMessage.value = "Invoice deleted."
                selectedInvoiceForDetails.value = null
            } catch (e: Exception) {
                userMessage.value = "Error deleting invoice: ${e.localizedMessage}"
            }
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val inventoryRepository: InventoryRepository,
        private val customerRepository: CustomerRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InvoiceViewModel(
                invoiceRepository,
                inventoryRepository,
                customerRepository,
                settingsRepository
            ) as T
        }
    }
}
