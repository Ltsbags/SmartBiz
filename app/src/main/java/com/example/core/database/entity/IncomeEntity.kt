package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incomeNumber: String,
    val incomeDate: Long,
    val category: String, // Sales Revenue, Consulting Services, Interest Income, Rental Income, Other
    val customerId: Long? = null,
    val customerName: String = "",
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, BANK_TRANSFER, UPI, CHEQUE, CARD
    val referenceNumber: String = "",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
