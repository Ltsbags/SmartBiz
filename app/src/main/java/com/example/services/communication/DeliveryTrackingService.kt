package com.example.services.communication

import com.example.core.database.entity.CommunicationLogEntity
import com.example.repositories.CommunicationRepository

enum class DeliveryStatus {
    PENDING,
    QUEUED,
    SENDING,
    DELIVERED,
    FAILED,
    RETRY,
    CANCELLED
}

class DeliveryTrackingService(
    private val repository: CommunicationRepository
) {
    suspend fun recordStatusChange(
        messageId: Long,
        newStatus: DeliveryStatus,
        details: String = ""
    ) {
        val now = System.currentTimeMillis()
        repository.updateMessageStatus(
            id = messageId,
            status = newStatus.name,
            details = details,
            now = now
        )

        repository.addLog(
            messageId = messageId,
            eventType = newStatus.name,
            details = details.ifBlank { "Status updated to ${newStatus.name}" }
        )
    }

    suspend fun processWebhookDeliveryReceipt(
        messageId: Long,
        providerStatus: String,
        providerMessageId: String,
        errorMessage: String = ""
    ): Boolean {
        val mappedStatus = when (providerStatus.uppercase()) {
            "DELIVERED", "READ", "SENT", "SUCCESS", "200" -> DeliveryStatus.DELIVERED
            "FAILED", "UNDELIVERABLE", "ERROR", "REJECTED" -> DeliveryStatus.FAILED
            "QUEUED", "PENDING" -> DeliveryStatus.QUEUED
            else -> DeliveryStatus.SENDING
        }

        val details = if (errorMessage.isNotBlank()) {
            "Webhook: $providerStatus | Provider ID: $providerMessageId | Error: $errorMessage"
        } else {
            "Webhook: $providerStatus | Provider ID: $providerMessageId"
        }

        recordStatusChange(messageId, mappedStatus, details)
        return mappedStatus == DeliveryStatus.DELIVERED
    }

    suspend fun cancelPendingMessage(messageId: Long): Boolean {
        val message = repository.getMessageById(messageId) ?: return false
        if (message.status in listOf("PENDING", "QUEUED", "RETRY")) {
            recordStatusChange(messageId, DeliveryStatus.CANCELLED, "Message cancelled by user or system policy")
            return true
        }
        return false
    }
}
