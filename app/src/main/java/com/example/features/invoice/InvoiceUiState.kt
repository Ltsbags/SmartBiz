package com.example.features.invoice

import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceWithItems

data class InvoiceUiState(
    val invoices: List<InvoiceWithItems> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val products: List<InventoryItemEntity> = emptyList(),
    val currencySymbol: String = "$",
    val searchQuery: String = "",
    val filterState: InvoiceFilterState = InvoiceFilterState(),
    val sortOption: InvoiceSortOption = InvoiceSortOption.NEWEST,
    val totalRevenue: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val totalInvoicesCount: Int = 0,
    val isLoading: Boolean = false,
    val selectedInvoiceForDetails: InvoiceWithItems? = null,
    val selectedInvoiceForEdit: InvoiceWithItems? = null,
    val isCreateInvoiceOpen: Boolean = false,
    val userMessage: String? = null
)
