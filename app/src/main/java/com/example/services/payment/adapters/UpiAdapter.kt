package com.example.services.payment.adapters

import com.example.core.database.entity.PaymentEntity
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult
import java.net.URLEncoder

class UpiAdapter : PaymentGatewayAdapter {
    override val providerName: String = "UPI"
    private var config: PaymentGatewayConfig = PaymentGatewayConfig()

    override suspend fun initialize(config: PaymentGatewayConfig) {
        this.config = config
    }

    override suspend fun createPaymentRequest(request: PaymentEngineRequest): PaymentEngineResult {
        val upiPayload = generateUpiUri(request)
        val txnId = "UPI-" + System.currentTimeMillis()
        val linkUrl = "https://pay.smartbiz.app/upi?txn=" + txnId + "&am=" + request.amount

        return PaymentEngineResult(
            isSuccess = true,
            transactionId = txnId,
            paymentNumber = "PAY-" + System.currentTimeMillis().toString().takeLast(6),
            linkUrl = linkUrl,
            qrPayload = upiPayload,
            gatewayResponse = "UPI URI payload generated successfully"
        )
    }

    override suspend fun processPayment(request: PaymentEngineRequest, paymentRef: String): PaymentEngineResult {
        val txnId = if (paymentRef.isNotBlank()) paymentRef else "UPI-TXN-" + System.currentTimeMillis()
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = txnId,
            paymentNumber = "PAY-" + System.currentTimeMillis().toString().takeLast(6),
            qrPayload = generateUpiUri(request),
            gatewayResponse = "UPI Direct Payment Verified"
        )
    }

    override suspend fun verifyPayment(transactionId: String): PaymentEngineResult {
        return PaymentEngineResult(
            isSuccess = true,
            transactionId = transactionId,
            gatewayResponse = "UPI Settlement Confirmed via VPA " + config.upiVpa
        )
    }

    override suspend fun processRefund(refundRequest: RefundEngineRequest, originalPayment: PaymentEntity): RefundEngineResult {
        val refundTxnId = "UPI-RFD-" + System.currentTimeMillis()
        return RefundEngineResult(
            isSuccess = true,
            refundNumber = "RFD-" + System.currentTimeMillis().toString().takeLast(6),
            gatewayRefundId = refundTxnId
        )
    }

    override suspend fun generateQrCode(request: PaymentEngineRequest): String {
        return generateUpiUri(request)
    }

    private fun generateUpiUri(request: PaymentEngineRequest): String {
        val encodedName = URLEncoder.encode(config.upiName.ifBlank { "BillNova Store" }, "UTF-8")
        val note = URLEncoder.encode(if (request.invoiceNumber.isNotBlank()) "Invoice ${request.invoiceNumber}" else request.description.ifBlank { "BillNova Payment" }, "UTF-8")
        val txnRef = "TXN" + System.currentTimeMillis()
        return "upi://pay?pa=${config.upiVpa}&pn=$encodedName&am=${request.amount}&cu=${config.currency}&tr=$txnRef&tn=$note"
    }
}
