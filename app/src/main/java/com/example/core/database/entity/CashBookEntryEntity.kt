package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_book_entries")
data class CashBookEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryDate: Long,
    val entryType: String, // CASH_IN, CASH_OUT
    val sourceType: String, // EXPENSE, INCOME, SALES_INVOICE, PURCHASE, MANUAL_ADJUSTMENT, OPENING_BALANCE
    val referenceId: Long? = null,
    val referenceNumber: String = "",
    val entityName: String = "",
    val description: String = "",
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, BANK, UPI, CHEQUE, CARD
    val balanceAfter: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis()
)
