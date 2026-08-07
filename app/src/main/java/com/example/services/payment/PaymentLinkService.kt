package com.example.services.payment.PaymentLinkService

import com.example.core.database.dao.PaymentRequestDao
import com.example.core.database.entity.PaymentRequestEntity
import com.example.services.payment.models.PaymentEngineRequest
import java.net.URLEncoder

class PaymentLinkService(
    private val paymentRequestDao: PaymentRequestDao
) {
    suspend fun generatePaymentLink(
        request: PaymentEngineRequest,
        expiryHours: Int = 72
    ): PaymentRequestEntity {
        val requestNo = "REQ-" + System.currentTimeMillis().toString().takeLast(8)
        val expiryTime = System.currentTimeMillis() + (expiryHours * 3600000L)
        
        val effectiveProvider = if (request.preferredProvider.isNotBlank() && request.preferredProvider.uppercase() != "UPI") {
            request.preferredProvider
        } else if (request.paymentMethod.isNotBlank()) {
            request.paymentMethod
        } else {
            request.preferredProvider
        }

        val baseUrl = when (effectiveProvider.uppercase()) {
            "RAZORPAY" -> "https://rzp.io/i/"
            "STRIPE" -> "https://checkout.stripe.com/pay/"
            else -> "https://pay.smartbiz.app/pay/"
        }
        val paymentLink = baseUrl + requestNo

        val encodedDesc = URLEncoder.encode(request.description.ifBlank { "BillNova Store Invoice ${request.invoiceNumber}" }, "UTF-8")
        val upiVpa = "store@upi"
        val upiQrPayload = "upi://pay?pa=$upiVpa&pn=BillNova&am=${request.amount}&cu=${request.currency}&tr=$requestNo&tn=$encodedDesc"

        val entity = PaymentRequestEntity(
            requestNumber = requestNo,
            invoiceId = request.invoiceId,
            invoiceNumber = request.invoiceNumber,
            customerId = request.customerId,
            customerName = request.customerName,
            customerPhone = request.customerPhone,
            amount = request.amount,
            description = request.description,
            expiryTimestamp = expiryTime,
            status = "ACTIVE",
            paymentLinkUrl = paymentLink,
            qrCodePayload = upiQrPayload,
            preferredProvider = request.preferredProvider,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val insertedId = paymentRequestDao.insertPaymentRequest(entity)
        return entity.copy(id = insertedId)
    }

    fun buildShareableText(requestEntity: PaymentRequestEntity): String {
        return """
            *BillNova Payment Request*
            Dear ${requestEntity.customerName.ifBlank { "Customer" }},
            Invoice #${requestEntity.invoiceNumber.ifBlank { "N/A" }}
            Amount Due: ${requestEntity.amount} INR
            
            Pay via UPI or Card here:
            ${requestEntity.paymentLinkUrl}
            
            UPI QR String:
            ${requestEntity.qrCodePayload}
            
            Link expires on: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(requestEntity.expiryTimestamp))}
        """.trimIndent()
    }
}
