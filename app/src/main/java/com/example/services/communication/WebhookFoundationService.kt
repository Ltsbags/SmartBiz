package com.example.services.communication

data class WebhookPayload(
    val provider: String, // "WHATSAPP", "TWILIO", "SENDGRID", "GENERIC"
    val externalMessageId: String,
    val internalMessageId: Long,
    val deliveryStatus: String, // "DELIVERED", "FAILED", "QUEUED", "READ"
    val timestamp: Long = System.currentTimeMillis(),
    val rawPayloadJson: String = "{}"
)

class WebhookFoundationService(
    private val deliveryTrackingService: DeliveryTrackingService
) {
    suspend fun handleInboundWebhook(payload: WebhookPayload): Boolean {
        return deliveryTrackingService.processWebhookDeliveryReceipt(
            messageId = payload.internalMessageId,
            providerStatus = payload.deliveryStatus,
            providerMessageId = payload.externalMessageId,
            errorMessage = ""
        )
    }

    fun parseWhatsAppWebhook(jsonString: String): WebhookPayload? {
        // Mock parsing logic for WhatsApp Cloud API DLR webhook
        return WebhookPayload(
            provider = "WHATSAPP",
            externalMessageId = "wamid.HBgLMTIzNDU2Nzg5MA==",
            internalMessageId = 1L,
            deliveryStatus = "DELIVERED",
            rawPayloadJson = jsonString
        )
    }

    fun parseSendGridWebhook(jsonString: String): WebhookPayload? {
        // Mock parsing logic for SendGrid Event Webhook
        return WebhookPayload(
            provider = "SENDGRID",
            externalMessageId = "sg_msg_987654321",
            internalMessageId = 1L,
            deliveryStatus = "DELIVERED",
            rawPayloadJson = jsonString
        )
    }
}
