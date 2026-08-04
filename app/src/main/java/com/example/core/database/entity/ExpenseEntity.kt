package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseNumber: String,
    val expenseDate: Long,
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMode: String = "CASH", // CASH, BANK_TRANSFER, UPI, CREDIT_CARD, CHEQUE
    val paymentStatus: String = "PAID", // PAID, UNPAID, PARTIAL
    val paidAmount: Double = 0.0,
    val referenceNumber: String = "",
    val payeeName: String = "",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
