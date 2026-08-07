package com.example.services.communication

import android.content.Context
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.repositories.CommunicationRepository

class CommunicationRetryService(
    private val repository: CommunicationRepository,
    private val deliveryTrackingService: DeliveryTrackingService
) {
    suspend fun processPendingRetryQueue(
        context: Context,
        dispatchFunction: suspend (Context, CommunicationMessageEntity) -> Unit
    ): Int {
        val pendingMessages = repository.getPendingDispatchMessages()
        var retriedCount = 0

        for (msg in pendingMessages) {
            if (msg.status == "RETRY" || msg.status == "QUEUED" || msg.status == "PENDING") {
                if (msg.retryCount <= msg.maxRetries) {
                    deliveryTrackingService.recordStatusChange(
                        msg.id,
                        DeliveryStatus.SENDING,
                        "Processing retry attempt ${msg.retryCount + 1}/${msg.maxRetries}"
                    )
                    dispatchFunction(context, msg)
                    retriedCount++
                } else {
                    deliveryTrackingService.recordStatusChange(
                        msg.id,
                        DeliveryStatus.FAILED,
                        "Max retry threshold (${msg.maxRetries}) exceeded"
                    )
                }
            }
        }
        return retriedCount
    }

    fun calculateNextBackoffMs(retryAttempt: Int): Long {
        val baseBackoffMs = 5000L // 5 seconds
        val maxBackoffMs = 300000L // 5 minutes
        val backoff = baseBackoffMs * (1 shl (retryAttempt - 1).coerceAtLeast(0))
        return backoff.coerceAtMost(maxBackoffMs)
    }

    suspend fun scheduleManualRetry(
        context: Context,
        messageId: Long,
        dispatchFunction: suspend (Context, CommunicationMessageEntity) -> Unit
    ): Boolean {
        val message = repository.getMessageById(messageId) ?: return false
        val resetMessage = message.copy(
            status = "QUEUED",
            retryCount = 0,
            deliveryStatusDetails = "Manual user override retry initiated"
        )
        repository.saveMessage(resetMessage)
        deliveryTrackingService.recordStatusChange(messageId, DeliveryStatus.QUEUED, "Manual retry queued")
        dispatchFunction(context, resetMessage)
        return true
    }
}
