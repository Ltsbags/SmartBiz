package com.example.services.payment.adapters

import com.example.core.database.entity.PaymentEntity
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult

class StripeAdapter : PaymentGatewayAdapter {
    override val providerName: String = "STRIPE"
    private var config: PaymentGatewayConfig = PaymentGatewayConfig(provider = "STRIPE")

    override suspend fun initialize(config: PaymentGatewayConfig) {
        this.config = config
    }

    override suspend fun createPaymentRequest(request: PaymentEngineRequest): PaymentEngineResult {
        val piId = "pi_stripe_" + System.currentTimeMillis().toString().takeLast(10)
        val checkoutUrl = "https://checkout.stripe.com/c/pay/" + piId

        return PaymentEngineResult(
            isSuccess = true,
            transactionId = piId,
            paymentNumber = "PAY-STP-" + System.currentTimeMillis().toString().takeLast(6),
            linkUrl = checkoutUrl,
            qrPayload = checkoutUrl,
            gatewayResponse = """{"id": "$piId", "object": "payment_intent", "amount": ${request.amount * 100}, "currency": "${request.currency.lowercase()}", "status": "requires_payment_method"}"""
        )
    }

    override suspend fun processPayment(request: PaymentEngineRequest, paymentRef: String): PaymentEngineResult {
        val txnId = if (paymentRef.startsWith("pi_")) paymentRef else "pi_" + System.currentTimeMillis().toString().takeLast(12)
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = txnId,
            paymentNumber = "PAY-STP-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayResponse = """{"id": "$txnId", "object": "payment_intent", "amount": ${request.amount * 100}, "status": "succeeded"}"""
        )
    }

    override suspend fun verifyPayment(transactionId: String): PaymentEngineResult {
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = transactionId,
            gatewayResponse = """{"id": "$transactionId", "status": "succeeded", "charges": {"total_count": 1}}"""
        )
    }

    override suspend fun processRefund(refundRequest: RefundEngineRequest, originalPayment: PaymentEntity): RefundEngineResult {
        val reId = "re_" + System.currentTimeMillis().toString().takeLast(12)
        return RefundEngineResult(
            isSuccess = true,
            refundNumber = "RFD-STP-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayRefundId = reId
        )
    }

    override suspend fun generateQrCode(request: PaymentEngineRequest): String {
        val res = createPaymentRequest(request)
        return res.linkUrl
    }
}
