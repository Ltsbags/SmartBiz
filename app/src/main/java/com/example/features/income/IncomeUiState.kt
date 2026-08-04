package com.example.features.income

import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.IncomeEntity

enum class IncomeSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_HIGH("Highest Amount"),
    AMOUNT_LOW("Lowest Amount"),
    CATEGORY("Source Category")
}

data class IncomeUiState(
    val incomeList: List<IncomeEntity> = emptyList(),
    val filteredIncome: List<IncomeEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val categoryFilter: String = "ALL",
    val sortOption: IncomeSortOption = IncomeSortOption.DATE_DESC,
    val showAddEditDialog: Boolean = false,
    val incomeToEdit: IncomeEntity? = null,
    val showDetailsDialog: Boolean = false,
    val selectedIncome: IncomeEntity? = null,
    val totalIncomeCount: Int = 0,
    val totalIncomeAmount: Double = 0.0,
    val userMessage: String? = null
)
