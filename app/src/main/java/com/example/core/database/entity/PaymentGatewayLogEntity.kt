package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_gateway_logs")
data class PaymentGatewayLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paymentId: Long? = null,
    val paymentRequestId: Long? = null,
    val provider: String, // "RAZORPAY", "STRIPE", "UPI", "PAYPAL", "BANK_API"
    val eventType: String, // "REQUEST", "RESPONSE", "WEBHOOK", "CALLBACK", "ERROR", "RECONCILIATION"
    val requestPayload: String = "",
    val responsePayload: String = "",
    val statusCode: Int = 200,
    val errorMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
