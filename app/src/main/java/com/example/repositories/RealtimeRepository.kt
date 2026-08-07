package com.example.repositories

import com.example.core.database.entity.RealtimeEventEntity
import com.example.core.database.entity.RealtimeSessionEntity
import com.example.core.realtime.ConnectionState
import com.example.core.realtime.RealtimeEvent
import com.example.services.RealtimeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RealtimeRepository(
    private val realtimeService: RealtimeService
) {
    val connectionState: StateFlow<ConnectionState> = realtimeService.connectionState

    val allHistoricalEventsFlow: Flow<List<RealtimeEventEntity>> = realtimeService.allHistoricalEventsFlow

    val activeSessionsFlow: Flow<List<RealtimeSessionEntity>> = realtimeService.activeSessionsFlow

    fun observeModuleEvents(module: String): Flow<RealtimeEvent> {
        return realtimeService.observeEventsByModule(module)
    }

    fun observeEventTypes(eventType: String): Flow<RealtimeEvent> {
        return realtimeService.observeEventsByType(eventType)
    }

    fun startRealtimeService(authToken: String = "JWT_BEARER_MOCK_TOKEN", businessId: String = "BIZ_001", branchId: String = "BRANCH_MAIN") {
        realtimeService.startRealtimeSession(authToken, businessId, branchId)
    }

    fun stopRealtimeService() {
        realtimeService.stopRealtimeSession()
    }

    fun broadcastLocalRealtimeEvent(event: RealtimeEvent) {
        realtimeService.emitLocalEvent(event)
    }
}
