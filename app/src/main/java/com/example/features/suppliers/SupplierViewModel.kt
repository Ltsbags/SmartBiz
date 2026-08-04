package com.example.features.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.SupplierEntity
import com.example.repositories.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierUiState(isLoading = true))
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    init {
        loadSuppliers()
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            supplierRepository.allSuppliers.collect { suppliersList ->
                val activeCount = suppliersList.count { !it.isArchived && it.status == "ACTIVE" }
                val totalOutstanding = suppliersList.filter { !it.isArchived }.sumOf { it.outstandingBalance }

                _uiState.update { currentState ->
                    val filtered = applyFilterAndSort(
                        suppliers = suppliersList,
                        query = currentState.searchQuery,
                        filter = currentState.filterState,
                        sort = currentState.sortOption
                    )
                    currentState.copy(
                        suppliers = suppliersList,
                        filteredSuppliers = filtered,
                        isLoading = false,
                        totalSuppliersCount = suppliersList.size,
                        activeSuppliersCount = activeCount,
                        totalOutstanding = totalOutstanding
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                suppliers = currentState.suppliers,
                query = query,
                filter = currentState.filterState,
                sort = currentState.sortOption
            )
            currentState.copy(searchQuery = query, filteredSuppliers = filtered)
        }
    }

    fun onFilterChanged(filterState: SupplierFilterState) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                suppliers = currentState.suppliers,
                query = currentState.searchQuery,
                filter = filterState,
                sort = currentState.sortOption
            )
            currentState.copy(filterState = filterState, filteredSuppliers = filtered, showFilterSheet = false)
        }
    }

    fun onSortChanged(sortOption: SupplierSortOption) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                suppliers = currentState.suppliers,
                query = currentState.searchQuery,
                filter = currentState.filterState,
                sort = sortOption
            )
            currentState.copy(sortOption = sortOption, filteredSuppliers = filtered, showSortSheet = false)
        }
    }

    fun onAddSupplierClicked() {
        _uiState.update { it.copy(showAddEditDialog = true, supplierToEdit = null) }
    }

    fun onEditSupplierClicked(supplier: SupplierEntity) {
        _uiState.update { it.copy(showAddEditDialog = true, supplierToEdit = supplier) }
    }

    fun onSupplierSelected(supplier: SupplierEntity) {
        _uiState.update { it.copy(selectedSupplier = supplier, showDetailsDialog = true) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, supplierToEdit = null) }
    }

    fun dismissDetailsDialog() {
        _uiState.update { it.copy(showDetailsDialog = false, selectedSupplier = null) }
    }

    fun toggleFilterSheet(show: Boolean) {
        _uiState.update { it.copy(showFilterSheet = show) }
    }

    fun toggleSortSheet(show: Boolean) {
        _uiState.update { it.copy(showSortSheet = show) }
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            supplierRepository.saveSupplier(supplier)
            dismissAddEditDialog()
            _uiState.update { it.copy(userMessage = "Supplier saved successfully") }
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            supplierRepository.deleteSupplier(supplier)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Supplier deleted") }
        }
    }

    fun archiveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            supplierRepository.setArchiveStatus(supplier.id, true)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Supplier archived") }
        }
    }

    fun restoreSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            supplierRepository.setArchiveStatus(supplier.id, false)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Supplier restored") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSort(
        suppliers: List<SupplierEntity>,
        query: String,
        filter: SupplierFilterState,
        sort: SupplierSortOption
    ): List<SupplierEntity> {
        return suppliers
            .filter { supplier ->
                // Query search
                val matchesQuery = query.isBlank() ||
                        supplier.supplierName.contains(query, ignoreCase = true) ||
                        supplier.businessName.contains(query, ignoreCase = true) ||
                        supplier.phone.contains(query, ignoreCase = true) ||
                        supplier.supplierCode.contains(query, ignoreCase = true)

                // Status filter
                val matchesStatus = when (filter.status) {
                    "ACTIVE" -> !supplier.isArchived && supplier.status == "ACTIVE"
                    "INACTIVE" -> !supplier.isArchived && supplier.status == "INACTIVE"
                    "ARCHIVED" -> supplier.isArchived
                    else -> !supplier.isArchived // ALL non-archived
                }

                // Outstanding filter
                val matchesOutstanding = !filter.hasOutstanding || supplier.outstandingBalance > 0

                matchesQuery && matchesStatus && matchesOutstanding
            }
            .sortedWith { s1, s2 ->
                when (sort) {
                    SupplierSortOption.NAME_ASC -> s1.supplierName.compareTo(s2.supplierName, ignoreCase = true)
                    SupplierSortOption.NAME_DESC -> s2.supplierName.compareTo(s1.supplierName, ignoreCase = true)
                    SupplierSortOption.OUTSTANDING_HIGH -> s2.outstandingBalance.compareTo(s1.outstandingBalance)
                    SupplierSortOption.OUTSTANDING_LOW -> s1.outstandingBalance.compareTo(s2.outstandingBalance)
                    SupplierSortOption.RECENT -> s2.updatedDate.compareTo(s1.updatedDate)
                }
            }
    }

    class Factory(
        private val supplierRepository: SupplierRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SupplierViewModel(supplierRepository) as T
        }
    }
}
