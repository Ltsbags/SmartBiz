package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paymentNumber: String, // e.g., "PAY-2026-0001"
    val paymentRequestId: Long? = null,
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val customerId: Long? = null,
    val customerName: String = "",
    val amount: Double,
    val paymentMethod: String, // "UPI", "RAZORPAY", "STRIPE", "QR_CODE", "PAYMENT_LINK", "CASH", "BANK_TRANSFER"
    val transactionRef: String = "",
    val status: String = "SUCCESS", // "PENDING", "SUCCESS", "FAILED", "REFUNDED", "PARTIALLY_REFUNDED"
    val gatewayProvider: String = "UPI", // "UPI", "RAZORPAY", "STRIPE", "OFFLINE_CASH", "BANK_API"
    val gatewayTransactionId: String = "",
    val gatewayFee: Double = 0.0,
    val currency: String = "INR",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isOfflineProcessed: Boolean = false,
    val failureReason: String = ""
)
