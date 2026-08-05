package com.example.services

import com.example.core.database.entity.NotificationEntity
import com.example.repositories.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class NotificationEvent(
    val type: String, // SECURITY, SALES, PURCHASES, INVENTORY, CUSTOMERS, SUPPLIERS, FINANCE, SYSTEM, REPORTS, CUSTOM
    val title: String,
    val message: String,
    val severity: String = "INFO", // INFO, WARNING, HIGH, CRITICAL
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val businessId: String = "default_biz",
    val branchId: String = "main_branch",
    val userId: String = "system",
    val payloadJson: String = "{}"
)

class NotificationEngine(
    private val notificationRepository: NotificationRepository,
    private val preferenceService: NotificationPreferenceService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _eventFlow = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 64)
    val eventFlow: SharedFlow<NotificationEvent> = _eventFlow.asSharedFlow()

    fun publishEvent(event: NotificationEvent) {
        scope.launch {
            processBusinessEvent(event)
        }
    }

    suspend fun processBusinessEvent(event: NotificationEvent) {
        val prefKey = when (event.type) {
            "INVENTORY" -> "low_stock_alerts"
            "SALES", "PURCHASES", "CUSTOMERS", "SUPPLIERS", "FINANCE" -> "payment_alerts"
            "SECURITY" -> "security_alerts"
            "REPORTS" -> "business_summary"
            else -> "system_alerts"
        }

        val isEnabled = preferenceService.isCategoryEnabled(prefKey)
        if (!isEnabled) return

        val entity = NotificationEntity(
            businessId = event.businessId,
            branchId = event.branchId,
            userId = event.userId,
            type = event.type,
            title = event.title,
            message = event.message,
            severity = event.severity,
            priority = event.priority,
            status = "UNREAD",
            createdDate = System.currentTimeMillis(),
            deliveredDate = System.currentTimeMillis(),
            payloadJson = event.payloadJson
        )

        notificationRepository.addNotification(entity)
        _eventFlow.emit(event)

        // Ready for future Push, WhatsApp, and Email dispatchers without modifying business caller logic
        dispatchFutureChannels(entity)
    }

    private fun dispatchFutureChannels(notification: NotificationEntity) {
        // Foundation hooks for future push, WhatsApp, and email integration
    }

    val unreadNotificationsFlow: Flow<List<NotificationEntity>> = notificationRepository.unreadNotificationsFlow
    val unreadCountFlow: Flow<Int> = notificationRepository.unreadCountFlow
    val allNotificationsFlow: Flow<List<NotificationEntity>> = notificationRepository.allNotificationsFlow
}
