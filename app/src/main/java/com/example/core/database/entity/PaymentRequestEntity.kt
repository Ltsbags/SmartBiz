package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_requests")
data class PaymentRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestNumber: String, // e.g. "REQ-2026-001"
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val amount: Double,
    val description: String = "",
    val expiryTimestamp: Long = System.currentTimeMillis() + 86400000L * 3, // 3 days
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "EXPIRED", "CANCELLED"
    val paymentLinkUrl: String = "",
    val qrCodePayload: String = "",
    val preferredProvider: String = "UPI",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
