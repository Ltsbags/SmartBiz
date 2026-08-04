package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_ledgers",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class CustomerLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val transactionType: String, // "INVOICE", "PAYMENT", "CREDIT_NOTE", "OPENING_BALANCE"
    val referenceNumber: String = "", // e.g. "INV-1001" or "PAY-2001"
    val amount: Double, // positive for invoice/debit, negative for payment/credit
    val balanceAfter: Double,
    val description: String = "",
    val transactionDate: Long = System.currentTimeMillis()
)
