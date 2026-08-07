package com.example.services.payment.models

data class PaymentGatewayConfig(
    val provider: String = "UPI", // UPI, RAZORPAY, STRIPE, OFFLINE_CASH, BANK_API
    val apiKey: String = "",
    val secretKey: String = "",
    val merchantId: String = "",
    val upiVpa: String = "store@upi",
    val upiName: String = "BillNova Store",
    val isTestMode: Boolean = true,
    val currency: String = "INR"
)

data class PaymentEngineRequest(
    val amount: Double,
    val currency: String = "INR",
    val customerId: Long? = null,
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val description: String = "",
    val preferredProvider: String = "UPI", // UPI, RAZORPAY, STRIPE, CASH, BANK_TRANSFER
    val paymentMethod: String = "UPI",
    val isOfflineProcessed: Boolean = false,
    val notes: String = ""
)

data class PaymentEngineResult(
    val isSuccess: Boolean,
    val transactionId: String = "",
    val paymentNumber: String = "",
    val paymentId: Long? = null,
    val linkUrl: String = "",
    val qrPayload: String = "",
    val gatewayResponse: String = "",
    val errorMessage: String = ""
)

data class RefundEngineRequest(
    val paymentId: Long,
    val amount: Double,
    val reason: String = "Customer Requested Refund",
    val initiatedBy: String = "System Admin"
)

data class RefundEngineResult(
    val isSuccess: Boolean,
    val refundId: Long? = null,
    val refundNumber: String = "",
    val gatewayRefundId: String = "",
    val errorMessage: String = ""
)

enum class DiscrepancyType {
    AMOUNT_MISMATCH,
    UNMATCHED_LOCAL,
    UNMATCHED_GATEWAY,
    STATUS_MISMATCH
}

data class ReconciliationDiscrepancy(
    val localPaymentId: Long?,
    val localPaymentNumber: String,
    val gatewayTxnId: String,
    val localAmount: Double,
    val gatewayAmount: Double,
    val discrepancyType: DiscrepancyType,
    val description: String
)

data class ReconciliationReport(
    val totalGatewayAmount: Double,
    val totalLocalAmount: Double,
    val matchedCount: Int,
    val unmatchedCount: Int,
    val discrepancies: List<ReconciliationDiscrepancy>,
    val timestamp: Long = System.currentTimeMillis()
)
