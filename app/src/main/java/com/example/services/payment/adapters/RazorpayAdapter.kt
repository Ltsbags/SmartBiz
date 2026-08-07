package com.example.services.payment.adapters

import com.example.core.database.entity.PaymentEntity
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult

class RazorpayAdapter : PaymentGatewayAdapter {
    override val providerName: String = "RAZORPAY"
    private var config: PaymentGatewayConfig = PaymentGatewayConfig(provider = "RAZORPAY")

    override suspend fun initialize(config: PaymentGatewayConfig) {
        this.config = config
    }

    override suspend fun createPaymentRequest(request: PaymentEngineRequest): PaymentEngineResult {
        val orderId = "order_rzp_" + System.currentTimeMillis().toString().takeLast(10)
        val linkUrl = "https://rzp.io/i/" + orderId
        val qrPayload = "https://api.razorpay.com/v1/payments/qr/$orderId"

        return PaymentEngineResult(
            isSuccess = true,
            transactionId = orderId,
            paymentNumber = "PAY-RZP-" + System.currentTimeMillis().toString().takeLast(6),
            linkUrl = linkUrl,
            qrPayload = qrPayload,
            gatewayResponse = """{"id": "$orderId", "entity": "order", "amount": ${request.amount * 100}, "currency": "${request.currency}", "status": "created"}"""
        )
    }

    override suspend fun processPayment(request: PaymentEngineRequest, paymentRef: String): PaymentEngineResult {
        val payId = if (paymentRef.startsWith("pay_")) paymentRef else "pay_" + System.currentTimeMillis().toString().takeLast(12)
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = payId,
            paymentNumber = "PAY-RZP-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayResponse = """{"id": "$payId", "entity": "payment", "amount": ${request.amount * 100}, "status": "captured", "method": "${request.paymentMethod.lowercase()}"}"""
        )
    }

    override suspend fun verifyPayment(transactionId: String): PaymentEngineResult {
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = transactionId,
            gatewayResponse = """{"id": "$transactionId", "status": "captured", "verified": true}"""
        )
    }

    override suspend fun processRefund(refundRequest: RefundEngineRequest, originalPayment: PaymentEntity): RefundEngineResult {
        val rfdId = "rfnd_" + System.currentTimeMillis().toString().takeLast(12)
        return RefundEngineResult(
            isSuccess = true,
            refundNumber = "RFD-RZP-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayRefundId = rfdId
        )
    }

    override suspend fun generateQrCode(request: PaymentEngineRequest): String {
        val res = createPaymentRequest(request)
        return res.qrPayload
    }
}
