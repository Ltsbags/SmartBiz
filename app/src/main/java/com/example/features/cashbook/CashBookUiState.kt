package com.example.features.cashbook

import com.example.core.database.entity.CashBookEntryEntity

data class CashBookUiState(
    val entries: List<CashBookEntryEntity> = emptyList(),
    val filteredEntries: List<CashBookEntryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val typeFilter: String = "ALL", // ALL, CASH_IN, CASH_OUT
    val modeFilter: String = "ALL", // ALL, CASH, BANK, UPI, CARD, CHEQUE
    val showAddDialog: Boolean = false,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netCashBalance: Double = 0.0,
    val userMessage: String? = null
)
