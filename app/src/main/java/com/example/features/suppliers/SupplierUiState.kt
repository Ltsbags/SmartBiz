package com.example.features.suppliers

import com.example.core.database.entity.SupplierEntity

enum class SupplierSortOption {
    NAME_ASC, NAME_DESC, OUTSTANDING_HIGH, OUTSTANDING_LOW, RECENT
}

data class SupplierFilterState(
    val status: String = "ALL", // ALL, ACTIVE, INACTIVE, ARCHIVED
    val hasOutstanding: Boolean = false
)

data class SupplierUiState(
    val suppliers: List<SupplierEntity> = emptyList(),
    val filteredSuppliers: List<SupplierEntity> = emptyList(),
    val searchQuery: String = "",
    val filterState: SupplierFilterState = SupplierFilterState(),
    val sortOption: SupplierSortOption = SupplierSortOption.NAME_ASC,
    val isLoading: Boolean = false,
    val selectedSupplier: SupplierEntity? = null,
    val showAddEditDialog: Boolean = false,
    val supplierToEdit: SupplierEntity? = null,
    val showDetailsDialog: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showSortSheet: Boolean = false,
    val totalSuppliersCount: Int = 0,
    val activeSuppliersCount: Int = 0,
    val totalOutstanding: Double = 0.0,
    val userMessage: String? = null
)
