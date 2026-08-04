package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 15 * 86400000L,
    val customerId: Long = 0,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerGst: String = "",
    val billingAddress: String = "",
    val status: String = "DRAFT", // "DRAFT", "COMPLETED", "CANCELLED"
    val paymentStatus: String = "UNPAID", // "PAID", "PARTIAL", "UNPAID"
    val subtotal: Double = 0.0,
    val discountType: String = "FLAT", // "FLAT", "PERCENTAGE"
    val discountValue: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val roundOff: Double = 0.0,
    val totalAmount: Double = 0.0, // Grand Total
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val itemsCount: Int = 0,
    val notes: String = "",
    val terms: String = "Goods once sold cannot be returned or exchanged. Payment due within 15 days.",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

