package com.example.services

import android.util.Log
import com.example.core.database.dao.RealtimeEventDao
import com.example.core.database.dao.RealtimeSessionDao
import com.example.core.database.entity.RealtimeEventEntity
import com.example.core.database.entity.RealtimeSessionEntity
import com.example.core.realtime.ConnectionState
import com.example.core.realtime.EventBus
import com.example.core.realtime.RealtimeEvent
import com.example.core.realtime.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealtimeService(
    private val realtimeEventDao: RealtimeEventDao,
    private val realtimeSessionDao: RealtimeSessionDao,
    private val eventBus: EventBus = EventBus.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "RealtimeService"
    private val webSocketClient = WebSocketClient(clientScope = scope)

    val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState

    init {
        webSocketClient.eventHandler = { event ->
            handleIncomingEvent(event)
        }

        // Monitor connection state to sync session status in SQLite
        scope.launch {
            connectionState.collect { state ->
                val session = RealtimeSessionEntity(
                    sessionId = "SESS_REALTIME_PRIMARY",
                    userId = "USER_PRIMARY",
                    userName = "Active Operator",
                    businessId = "BIZ_001",
                    branchId = "BRANCH_MAIN",
                    connectionState = state.name,
                    transportType = "WEBSOCKET",
                    connectedAt = System.currentTimeMillis(),
                    lastHeartbeatAt = System.currentTimeMillis()
                )
                realtimeSessionDao.insertOrUpdateSession(session)
            }
        }
    }

    fun startRealtimeSession(
        authToken: String = "MOCK_JWT_TOKEN_EXPRESS",
        businessId: String = "BIZ_001",
        branchId: String = "BRANCH_MAIN"
    ) {
        Log.i(TAG, "Starting Realtime Service session...")
        webSocketClient.connect(authToken, businessId, branchId)
    }

    fun stopRealtimeSession() {
        Log.i(TAG, "Stopping Realtime Service session...")
        webSocketClient.disconnect()
    }

    private fun handleIncomingEvent(event: RealtimeEvent) {
        scope.launch {
            // 1. Publish to central EventBus
            eventBus.publish(event)

            // 2. Persist event into SQLite for historical audit & offline access
            val entity = RealtimeEventEntity(
                eventId = event.eventId,
                eventType = event.eventType,
                module = event.module,
                entityId = event.eventId,
                payloadJson = parsePayloadJson(event),
                severity = event.severity,
                timestamp = event.timestamp,
                isProcessed = true
            )
            realtimeEventDao.insertEvent(entity)
        }
    }

    fun emitLocalEvent(event: RealtimeEvent) {
        scope.launch {
            handleIncomingEvent(event)
        }
    }

    fun observeEventsByModule(module: String): Flow<RealtimeEvent> {
        return eventBus.subscribeByModule(module)
    }

    fun observeEventsByType(eventType: String): Flow<RealtimeEvent> {
        return eventBus.subscribeByEventType(eventType)
    }

    val allHistoricalEventsFlow: Flow<List<RealtimeEventEntity>> = realtimeEventDao.getAllEventsFlow()

    val activeSessionsFlow: Flow<List<RealtimeSessionEntity>> = realtimeSessionDao.getAllSessionsFlow()

    private fun parsePayloadJson(event: RealtimeEvent): String {
        return when (event) {
            is RealtimeEvent.InvoiceCreated -> """{"invoiceNumber":"${event.invoiceNumber}","amount":${event.totalAmount},"customer":"${event.customerName}"}"""
            is RealtimeEvent.StockChanged -> """{"productId":"${event.productId}","prev":${event.previousStock},"new":${event.newStock},"reason":"${event.reason}"}"""
            is RealtimeEvent.NotificationCreated -> """{"title":"${event.title}","message":"${event.message}"}"""
            is RealtimeEvent.SecurityAlert -> """{"title":"${event.alertTitle}","message":"${event.message}"}"""
            is RealtimeEvent.PresenceChanged -> """{"userId":"${event.userId}","status":"${event.newStatus}"}"""
            else -> """{"timestamp":${event.timestamp}}"""
        }
    }
}
