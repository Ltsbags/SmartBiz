package com.example.features.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.PurchaseItemEntity
import com.example.core.database.entity.PurchaseWithItems
import com.example.repositories.InventoryRepository
import com.example.repositories.PurchaseRepository
import com.example.repositories.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState(isLoading = true))
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                purchaseRepository.allPurchasesWithItems,
                supplierRepository.activeSuppliers,
                inventoryRepository.allItems
            ) { purchases, suppliers, inventoryItems ->
                Triple(purchases, suppliers, inventoryItems)
            }.collect { (purchasesList, suppliersList, inventoryItems) ->
                val totalAmount = purchasesList.sumOf { it.purchase.totalAmount }
                val pendingAmount = purchasesList.sumOf { it.purchase.balanceAmount }

                _uiState.update { currentState ->
                    val filtered = applyFilterAndSort(
                        purchases = purchasesList,
                        query = currentState.searchQuery,
                        filter = currentState.filterState,
                        sort = currentState.sortOption
                    )
                    currentState.copy(
                        purchases = purchasesList,
                        filteredPurchases = filtered,
                        suppliers = suppliersList,
                        availableProducts = inventoryItems,
                        isLoading = false,
                        totalPurchasesCount = purchasesList.size,
                        totalPurchaseAmount = totalAmount,
                        totalPendingAmount = pendingAmount
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                purchases = currentState.purchases,
                query = query,
                filter = currentState.filterState,
                sort = currentState.sortOption
            )
            currentState.copy(searchQuery = query, filteredPurchases = filtered)
        }
    }

    fun onFilterChanged(filterState: PurchaseFilterState) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                purchases = currentState.purchases,
                query = currentState.searchQuery,
                filter = filterState,
                sort = currentState.sortOption
            )
            currentState.copy(filterState = filterState, filteredPurchases = filtered, showFilterSheet = false)
        }
    }

    fun onSortChanged(sortOption: PurchaseSortOption) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                purchases = currentState.purchases,
                query = currentState.searchQuery,
                filter = currentState.filterState,
                sort = sortOption
            )
            currentState.copy(sortOption = sortOption, filteredPurchases = filtered, showSortSheet = false)
        }
    }

    fun onAddPurchaseClicked() {
        _uiState.update { it.copy(showAddEditDialog = true, purchaseToEdit = null) }
    }

    fun onEditPurchaseClicked(purchaseWithItems: PurchaseWithItems) {
        _uiState.update { it.copy(showAddEditDialog = true, purchaseToEdit = purchaseWithItems) }
    }

    fun onPurchaseSelected(purchaseWithItems: PurchaseWithItems) {
        _uiState.update { it.copy(selectedPurchase = purchaseWithItems, showDetailsDialog = true) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, purchaseToEdit = null) }
    }

    fun dismissDetailsDialog() {
        _uiState.update { it.copy(showDetailsDialog = false, selectedPurchase = null) }
    }

    fun toggleFilterSheet(show: Boolean) {
        _uiState.update { it.copy(showFilterSheet = show) }
    }

    fun toggleSortSheet(show: Boolean) {
        _uiState.update { it.copy(showSortSheet = show) }
    }

    fun savePurchase(purchase: PurchaseEntity, items: List<PurchaseItemEntity>) {
        viewModelScope.launch {
            purchaseRepository.savePurchase(purchase, items)
            dismissAddEditDialog()
            _uiState.update { it.copy(userMessage = "Purchase order saved successfully!") }
        }
    }

    fun markAsReceived(purchaseWithItems: PurchaseWithItems) {
        viewModelScope.launch {
            val updatedPurchase = purchaseWithItems.purchase.copy(
                status = "RECEIVED",
                updatedDate = System.currentTimeMillis()
            )
            purchaseRepository.savePurchase(updatedPurchase, purchaseWithItems.items)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Stock Inward completed! Inventory updated automatically.") }
        }
    }

    fun cancelPurchase(purchaseWithItems: PurchaseWithItems) {
        viewModelScope.launch {
            val updatedPurchase = purchaseWithItems.purchase.copy(
                status = "CANCELLED",
                updatedDate = System.currentTimeMillis()
            )
            purchaseRepository.savePurchase(updatedPurchase, purchaseWithItems.items)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Purchase order cancelled") }
        }
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            purchaseRepository.deletePurchase(purchase)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Purchase order deleted") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSort(
        purchases: List<PurchaseWithItems>,
        query: String,
        filter: PurchaseFilterState,
        sort: PurchaseSortOption
    ): List<PurchaseWithItems> {
        return purchases
            .filter { item ->
                val p = item.purchase
                val matchesQuery = query.isBlank() ||
                        p.purchaseNumber.contains(query, ignoreCase = true) ||
                        p.supplierName.contains(query, ignoreCase = true) ||
                        item.items.any { it.productName.contains(query, ignoreCase = true) }

                val matchesStatus = filter.status == "ALL" || p.status.equals(filter.status, ignoreCase = true)
                val matchesPayment = filter.paymentStatus == "ALL" || p.paymentStatus.equals(filter.paymentStatus, ignoreCase = true)
                val matchesSupplier = filter.supplierId == null || p.supplierId == filter.supplierId

                matchesQuery && matchesStatus && matchesPayment && matchesSupplier
            }
            .sortedWith { p1, p2 ->
                when (sort) {
                    PurchaseSortOption.DATE_DESC -> p2.purchase.purchaseDate.compareTo(p1.purchase.purchaseDate)
                    PurchaseSortOption.DATE_ASC -> p1.purchase.purchaseDate.compareTo(p2.purchase.purchaseDate)
                    PurchaseSortOption.AMOUNT_HIGH -> p2.purchase.totalAmount.compareTo(p1.purchase.totalAmount)
                    PurchaseSortOption.AMOUNT_LOW -> p1.purchase.totalAmount.compareTo(p2.purchase.totalAmount)
                    PurchaseSortOption.SUPPLIER_NAME -> p1.purchase.supplierName.compareTo(p2.purchase.supplierName, ignoreCase = true)
                }
            }
    }

    class Factory(
        private val purchaseRepository: PurchaseRepository,
        private val supplierRepository: SupplierRepository,
        private val inventoryRepository: InventoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PurchaseViewModel(purchaseRepository, supplierRepository, inventoryRepository) as T
        }
    }
}
