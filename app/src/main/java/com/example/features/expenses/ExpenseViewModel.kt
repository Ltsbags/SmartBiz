package com.example.features.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.ExpenseCategoryEntity
import com.example.core.database.entity.ExpenseEntity
import com.example.repositories.ExpenseCategoryRepository
import com.example.repositories.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState(isLoading = true))
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                expenseRepository.allExpenses,
                expenseCategoryRepository.allCategories
            ) { expenses, categories ->
                Pair(expenses, categories)
            }.collect { (expensesList, categoriesList) ->
                val totalAmount = expensesList.sumOf { it.totalAmount }
                val paidAmount = expensesList.sumOf { it.paidAmount }
                val pendingAmount = expensesList.sumOf { (it.totalAmount - it.paidAmount).coerceAtLeast(0.0) }

                _uiState.update { currentState ->
                    val filtered = applyFilterAndSort(
                        expenses = expensesList,
                        query = currentState.searchQuery,
                        filter = currentState.filterState,
                        sort = currentState.sortOption
                    )
                    currentState.copy(
                        expenses = expensesList,
                        filteredExpenses = filtered,
                        categories = categoriesList,
                        isLoading = false,
                        totalExpensesCount = expensesList.size,
                        totalExpensesAmount = totalAmount,
                        totalPaidAmount = paidAmount,
                        totalPendingAmount = pendingAmount
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                expenses = currentState.expenses,
                query = query,
                filter = currentState.filterState,
                sort = currentState.sortOption
            )
            currentState.copy(searchQuery = query, filteredExpenses = filtered)
        }
    }

    fun onCategoryFilterSelected(categoryId: Long?) {
        _uiState.update { currentState ->
            val newFilter = currentState.filterState.copy(categoryId = categoryId)
            val filtered = applyFilterAndSort(
                expenses = currentState.expenses,
                query = currentState.searchQuery,
                filter = newFilter,
                sort = currentState.sortOption
            )
            currentState.copy(filterState = newFilter, filteredExpenses = filtered)
        }
    }

    fun onSortOptionChanged(sortOption: ExpenseSortOption) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                expenses = currentState.expenses,
                query = currentState.searchQuery,
                filter = currentState.filterState,
                sort = sortOption
            )
            currentState.copy(sortOption = sortOption, filteredExpenses = filtered)
        }
    }

    fun onAddExpenseClicked() {
        _uiState.update { it.copy(showAddEditDialog = true, expenseToEdit = null) }
    }

    fun onEditExpenseClicked(expense: ExpenseEntity) {
        _uiState.update { it.copy(showAddEditDialog = true, expenseToEdit = expense) }
    }

    fun onExpenseSelected(expense: ExpenseEntity) {
        _uiState.update { it.copy(selectedExpense = expense, showDetailsDialog = true) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, expenseToEdit = null) }
    }

    fun dismissDetailsDialog() {
        _uiState.update { it.copy(showDetailsDialog = false, selectedExpense = null) }
    }

    fun toggleCategoryDialog(show: Boolean) {
        _uiState.update { it.copy(showCategoryDialog = show) }
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.saveExpense(expense)
            dismissAddEditDialog()
            _uiState.update { it.copy(userMessage = "Expense saved successfully & Cash Book updated") }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Expense deleted") }
        }
    }

    fun saveCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            expenseCategoryRepository.saveCategory(category)
            _uiState.update { it.copy(userMessage = "Expense category saved") }
        }
    }

    fun deleteCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            expenseCategoryRepository.deleteCategory(category)
            _uiState.update { it.copy(userMessage = "Expense category deleted") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSort(
        expenses: List<ExpenseEntity>,
        query: String,
        filter: ExpenseFilterState,
        sort: ExpenseSortOption
    ): List<ExpenseEntity> {
        return expenses
            .filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.expenseNumber.contains(query, ignoreCase = true) ||
                        item.categoryName.contains(query, ignoreCase = true) ||
                        item.payeeName.contains(query, ignoreCase = true) ||
                        item.notes.contains(query, ignoreCase = true) ||
                        item.referenceNumber.contains(query, ignoreCase = true)

                val matchesCategory = filter.categoryId == null || item.categoryId == filter.categoryId
                val matchesStatus = filter.paymentStatus == "ALL" || item.paymentStatus.equals(filter.paymentStatus, ignoreCase = true)
                val matchesMode = filter.paymentMode == "ALL" || item.paymentMode.equals(filter.paymentMode, ignoreCase = true)

                matchesQuery && matchesCategory && matchesStatus && matchesMode
            }
            .sortedWith { e1, e2 ->
                when (sort) {
                    ExpenseSortOption.DATE_DESC -> e2.expenseDate.compareTo(e1.expenseDate)
                    ExpenseSortOption.DATE_ASC -> e1.expenseDate.compareTo(e2.expenseDate)
                    ExpenseSortOption.AMOUNT_HIGH -> e2.totalAmount.compareTo(e1.totalAmount)
                    ExpenseSortOption.AMOUNT_LOW -> e1.totalAmount.compareTo(e2.totalAmount)
                    ExpenseSortOption.CATEGORY -> e1.categoryName.compareTo(e2.categoryName, ignoreCase = true)
                }
            }
    }

    class Factory(
        private val expenseRepository: ExpenseRepository,
        private val expenseCategoryRepository: ExpenseCategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(expenseRepository, expenseCategoryRepository) as T
        }
    }
}
