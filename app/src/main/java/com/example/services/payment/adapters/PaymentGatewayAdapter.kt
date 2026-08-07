package com.example.services.payment.adapters

import com.example.core.database.entity.PaymentEntity
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult

interface PaymentGatewayAdapter {
    val providerName: String

    suspend fun initialize(config: PaymentGatewayConfig)

    suspend fun createPaymentRequest(request: PaymentEngineRequest): PaymentEngineResult

    suspend fun processPayment(request: PaymentEngineRequest, paymentRef: String): PaymentEngineResult

    suspend fun verifyPayment(transactionId: String): PaymentEngineResult

    suspend fun processRefund(refundRequest: RefundEngineRequest, originalPayment: PaymentEntity): RefundEngineResult

    suspend fun generateQrCode(request: PaymentEngineRequest): String
}
