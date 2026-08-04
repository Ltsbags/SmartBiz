package com.example.features.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.IncomeEntity
import com.example.repositories.CustomerRepository
import com.example.repositories.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IncomeViewModel(
    private val incomeRepository: IncomeRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState(isLoading = true))
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                incomeRepository.allIncome,
                customerRepository.allCustomers
            ) { incomeList, customersList ->
                Pair(incomeList, customersList)
            }.collect { (incomeList, customersList) ->
                val totalAmount = incomeList.sumOf { it.amount }

                _uiState.update { currentState ->
                    val filtered = applyFilterAndSort(
                        income = incomeList,
                        query = currentState.searchQuery,
                        categoryFilter = currentState.categoryFilter,
                        sort = currentState.sortOption
                    )
                    currentState.copy(
                        incomeList = incomeList,
                        filteredIncome = filtered,
                        customers = customersList,
                        isLoading = false,
                        totalIncomeCount = incomeList.size,
                        totalIncomeAmount = totalAmount
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                income = currentState.incomeList,
                query = query,
                categoryFilter = currentState.categoryFilter,
                sort = currentState.sortOption
            )
            currentState.copy(searchQuery = query, filteredIncome = filtered)
        }
    }

    fun onCategoryFilterSelected(category: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                income = currentState.incomeList,
                query = currentState.searchQuery,
                categoryFilter = category,
                sort = currentState.sortOption
            )
            currentState.copy(categoryFilter = category, filteredIncome = filtered)
        }
    }

    fun onSortOptionChanged(sortOption: IncomeSortOption) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSort(
                income = currentState.incomeList,
                query = currentState.searchQuery,
                categoryFilter = currentState.categoryFilter,
                sort = sortOption
            )
            currentState.copy(sortOption = sortOption, filteredIncome = filtered)
        }
    }

    fun onAddIncomeClicked() {
        _uiState.update { it.copy(showAddEditDialog = true, incomeToEdit = null) }
    }

    fun onEditIncomeClicked(income: IncomeEntity) {
        _uiState.update { it.copy(showAddEditDialog = true, incomeToEdit = income) }
    }

    fun onIncomeSelected(income: IncomeEntity) {
        _uiState.update { it.copy(selectedIncome = income, showDetailsDialog = true) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, incomeToEdit = null) }
    }

    fun dismissDetailsDialog() {
        _uiState.update { it.copy(showDetailsDialog = false, selectedIncome = null) }
    }

    fun saveIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.saveIncome(income)
            dismissAddEditDialog()
            _uiState.update { it.copy(userMessage = "Income recorded & Cash Book updated!") }
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
            dismissDetailsDialog()
            _uiState.update { it.copy(userMessage = "Income entry deleted") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSort(
        income: List<IncomeEntity>,
        query: String,
        categoryFilter: String,
        sort: IncomeSortOption
    ): List<IncomeEntity> {
        return income
            .filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.incomeNumber.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.customerName.contains(query, ignoreCase = true) ||
                        item.notes.contains(query, ignoreCase = true) ||
                        item.referenceNumber.contains(query, ignoreCase = true)

                val matchesCat = categoryFilter == "ALL" || item.category.equals(categoryFilter, ignoreCase = true)

                matchesQuery && matchesCat
            }
            .sortedWith { i1, i2 ->
                when (sort) {
                    IncomeSortOption.DATE_DESC -> i2.incomeDate.compareTo(i1.incomeDate)
                    IncomeSortOption.DATE_ASC -> i1.incomeDate.compareTo(i2.incomeDate)
                    IncomeSortOption.AMOUNT_HIGH -> i2.amount.compareTo(i1.amount)
                    IncomeSortOption.AMOUNT_LOW -> i1.amount.compareTo(i2.amount)
                    IncomeSortOption.CATEGORY -> i1.category.compareTo(i2.category, ignoreCase = true)
                }
            }
    }

    class Factory(
        private val incomeRepository: IncomeRepository,
        private val customerRepository: CustomerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IncomeViewModel(incomeRepository, customerRepository) as T
        }
    }
}
