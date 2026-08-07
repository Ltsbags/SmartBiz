package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refunds")
data class RefundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val refundNumber: String, // e.g. "RFD-2026-001"
    val paymentId: Long,
    val paymentNumber: String = "",
    val invoiceId: Long? = null,
    val customerId: Long? = null,
    val customerName: String = "",
    val amount: Double,
    val reason: String = "",
    val status: String = "INITIATED", // "INITIATED", "PROCESSING", "COMPLETED", "FAILED"
    val gatewayRefundId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
