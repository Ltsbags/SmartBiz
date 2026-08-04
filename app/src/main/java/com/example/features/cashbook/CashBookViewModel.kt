package com.example.features.cashbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CashBookEntryEntity
import com.example.repositories.CashBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CashBookViewModel(
    private val cashBookRepository: CashBookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashBookUiState(isLoading = true))
    val uiState: StateFlow<CashBookUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                cashBookRepository.allEntries,
                cashBookRepository.totalCashIn,
                cashBookRepository.totalCashOut
            ) { entries, cashIn, cashOut ->
                Triple(entries, cashIn ?: 0.0, cashOut ?: 0.0)
            }.collect { (entriesList, cashInVal, cashOutVal) ->
                val netBalance = cashInVal - cashOutVal

                _uiState.update { currentState ->
                    val filtered = applyFilter(
                        entries = entriesList,
                        query = currentState.searchQuery,
                        typeFilter = currentState.typeFilter,
                        modeFilter = currentState.modeFilter
                    )
                    currentState.copy(
                        entries = entriesList,
                        filteredEntries = filtered,
                        isLoading = false,
                        totalCashIn = cashInVal,
                        totalCashOut = cashOutVal,
                        netCashBalance = netBalance
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilter(
                entries = currentState.entries,
                query = query,
                typeFilter = currentState.typeFilter,
                modeFilter = currentState.modeFilter
            )
            currentState.copy(searchQuery = query, filteredEntries = filtered)
        }
    }

    fun onTypeFilterChanged(type: String) {
        _uiState.update { currentState ->
            val filtered = applyFilter(
                entries = currentState.entries,
                query = currentState.searchQuery,
                typeFilter = type,
                modeFilter = currentState.modeFilter
            )
            currentState.copy(typeFilter = type, filteredEntries = filtered)
        }
    }

    fun onModeFilterChanged(mode: String) {
        _uiState.update { currentState ->
            val filtered = applyFilter(
                entries = currentState.entries,
                query = currentState.searchQuery,
                typeFilter = currentState.typeFilter,
                modeFilter = mode
            )
            currentState.copy(modeFilter = mode, filteredEntries = filtered)
        }
    }

    fun toggleAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

    fun addManualEntry(
        entryType: String,
        amount: Double,
        entityName: String,
        description: String,
        paymentMode: String
    ) {
        viewModelScope.launch {
            cashBookRepository.addManualCashEntry(
                entryType = entryType,
                amount = amount,
                entityName = entityName,
                description = description,
                paymentMode = paymentMode
            )
            toggleAddDialog(false)
            _uiState.update { it.copy(userMessage = "Manual cash entry saved!") }
        }
    }

    fun deleteEntry(entry: CashBookEntryEntity) {
        viewModelScope.launch {
            cashBookRepository.deleteEntry(entry)
            _uiState.update { it.copy(userMessage = "Cash book entry removed") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilter(
        entries: List<CashBookEntryEntity>,
        query: String,
        typeFilter: String,
        modeFilter: String
    ): List<CashBookEntryEntity> {
        return entries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                    entry.referenceNumber.contains(query, ignoreCase = true) ||
                    entry.entityName.contains(query, ignoreCase = true) ||
                    entry.description.contains(query, ignoreCase = true) ||
                    entry.sourceType.contains(query, ignoreCase = true)

            val matchesType = typeFilter == "ALL" || entry.entryType.equals(typeFilter, ignoreCase = true)
            val matchesMode = modeFilter == "ALL" || entry.paymentMode.equals(modeFilter, ignoreCase = true)

            matchesQuery && matchesType && matchesMode
        }
    }

    class Factory(
        private val cashBookRepository: CashBookRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CashBookViewModel(cashBookRepository) as T
        }
    }
}
