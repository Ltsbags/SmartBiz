package com.example.features.purchases

import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.PurchaseWithItems
import com.example.core.database.entity.SupplierEntity

enum class PurchaseSortOption {
    DATE_DESC, DATE_ASC, AMOUNT_HIGH, AMOUNT_LOW, SUPPLIER_NAME
}

data class PurchaseFilterState(
    val status: String = "ALL", // ALL, DRAFT, ORDERED, RECEIVED, CANCELLED
    val paymentStatus: String = "ALL", // ALL, UNPAID, PARTIAL, PAID
    val supplierId: Long? = null
)

data class PurchaseUiState(
    val purchases: List<PurchaseWithItems> = emptyList(),
    val filteredPurchases: List<PurchaseWithItems> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val availableProducts: List<InventoryItemEntity> = emptyList(),
    val searchQuery: String = "",
    val filterState: PurchaseFilterState = PurchaseFilterState(),
    val sortOption: PurchaseSortOption = PurchaseSortOption.DATE_DESC,
    val isLoading: Boolean = false,
    val selectedPurchase: PurchaseWithItems? = null,
    val showAddEditDialog: Boolean = false,
    val purchaseToEdit: PurchaseWithItems? = null,
    val showDetailsDialog: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showSortSheet: Boolean = false,
    val totalPurchasesCount: Int = 0,
    val totalPurchaseAmount: Double = 0.0,
    val totalPendingAmount: Double = 0.0,
    val userMessage: String? = null
)
