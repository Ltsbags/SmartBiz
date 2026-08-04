package com.example.features.expenses

import com.example.core.database.entity.ExpenseCategoryEntity
import com.example.core.database.entity.ExpenseEntity

enum class ExpenseSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_HIGH("Highest Amount"),
    AMOUNT_LOW("Lowest Amount"),
    CATEGORY("Category Name")
}

data class ExpenseFilterState(
    val categoryId: Long? = null,
    val paymentStatus: String = "ALL", // ALL, PAID, UNPAID, PARTIAL
    val paymentMode: String = "ALL"
)

data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val categories: List<ExpenseCategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterState: ExpenseFilterState = ExpenseFilterState(),
    val sortOption: ExpenseSortOption = ExpenseSortOption.DATE_DESC,
    val showAddEditDialog: Boolean = false,
    val expenseToEdit: ExpenseEntity? = null,
    val showDetailsDialog: Boolean = false,
    val selectedExpense: ExpenseEntity? = null,
    val showCategoryDialog: Boolean = false,
    val totalExpensesCount: Int = 0,
    val totalExpensesAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val totalPendingAmount: Double = 0.0,
    val userMessage: String? = null
)
