package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val supplierPhone: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val expectedDeliveryDate: Long = System.currentTimeMillis() + (7 * 86400000L),
    val status: String = "DRAFT", // DRAFT, ORDERED, RECEIVED, CANCELLED
    val paymentStatus: String = "UNPAID", // UNPAID, PARTIAL, PAID
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val itemsCount: Int = 0,
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
