package com.example.services.payment.adapters

import com.example.core.database.entity.PaymentEntity
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult

class OfflineCashAdapter : PaymentGatewayAdapter {
    override val providerName: String = "OFFLINE_CASH"
    private var config: PaymentGatewayConfig = PaymentGatewayConfig(provider = "OFFLINE_CASH")

    override suspend fun initialize(config: PaymentGatewayConfig) {
        this.config = config
    }

    override suspend fun createPaymentRequest(request: PaymentEngineRequest): PaymentEngineResult {
        val txnId = "CASH-REQ-" + System.currentTimeMillis()
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = txnId,
            paymentNumber = "PAY-CSH-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayResponse = "Offline cash payment request recorded"
        )
    }

    override suspend fun processPayment(request: PaymentEngineRequest, paymentRef: String): PaymentEngineResult {
        val txnId = if (paymentRef.isNotBlank()) paymentRef else "CASH-TXN-" + System.currentTimeMillis()
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = txnId,
            paymentNumber = "PAY-CSH-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayResponse = "Offline cash payment received in store drawer"
        )
    }

    override suspend fun verifyPayment(transactionId: String): PaymentEngineResult {
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = transactionId,
            gatewayResponse = "Offline cash verified by cashier"
        )
    }

    override suspend fun processRefund(refundRequest: RefundEngineRequest, originalPayment: PaymentEntity): RefundEngineResult {
        val rfdTxnId = "CASH-RFD-" + System.currentTimeMillis()
        return RefundEngineResult(
            isSuccess = true,
            refundNumber = "RFD-CSH-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayRefundId = rfdTxnId
        )
    }

    override suspend fun generateQrCode(request: PaymentEngineRequest): String {
        return "CASH-PAYMENT-${request.amount}-${request.invoiceNumber}"
    }
}
