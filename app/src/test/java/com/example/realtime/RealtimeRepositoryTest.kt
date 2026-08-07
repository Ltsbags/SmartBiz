package com.example.realtime

import com.example.core.database.dao.RealtimeEventDao
import com.example.core.database.dao.RealtimeSessionDao
import com.example.core.database.entity.RealtimeEventEntity
import com.example.core.database.entity.RealtimeSessionEntity
import com.example.core.realtime.RealtimeEvent
import com.example.repositories.RealtimeRepository
import com.example.services.RealtimeService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

private class FakeRealtimeEventDao : RealtimeEventDao {
    private val eventsList = mutableListOf(
        RealtimeEventEntity(
            eventId = "EVENT_001",
            eventType = "INVOICE_CREATED",
            module = "SALES",
            payloadJson = """{"amount": 100}""",
            severity = "INFO"
        )
    )
    private val eventsFlow = MutableStateFlow(eventsList.toList())

    override fun getAllEventsFlow(): Flow<List<RealtimeEventEntity>> = eventsFlow
    override fun getRecentEventsFlow(limit: Int): Flow<List<RealtimeEventEntity>> = eventsFlow
    override fun getEventsByModuleFlow(module: String): Flow<List<RealtimeEventEntity>> = flowOf(eventsList.filter { it.module == module })
    override fun getEventsByTypeFlow(eventType: String): Flow<List<RealtimeEventEntity>> = flowOf(eventsList.filter { it.eventType == eventType })
    override suspend fun insertEvent(event: RealtimeEventEntity) {
        eventsList.add(0, event)
        eventsFlow.value = eventsList.toList()
    }
    override suspend fun insertEvents(events: List<RealtimeEventEntity>) {
        eventsList.addAll(0, events)
        eventsFlow.value = eventsList.toList()
    }
    override suspend fun deleteEvent(eventId: String) {
        eventsList.removeAll { it.eventId == eventId }
        eventsFlow.value = eventsList.toList()
    }
    override suspend fun clearAllEvents() {
        eventsList.clear()
        eventsFlow.value = emptyList()
    }
}

private class FakeRealtimeSessionDao : RealtimeSessionDao {
    private val sessions = mutableMapOf<String, RealtimeSessionEntity>()
    override fun getAllSessionsFlow(): Flow<List<RealtimeSessionEntity>> = flowOf(sessions.values.toList())
    override suspend fun getSessionByUserId(userId: String): RealtimeSessionEntity? = sessions.values.find { it.userId == userId }
    override fun getActiveSessionsFlow(): Flow<List<RealtimeSessionEntity>> = flowOf(sessions.values.filter { it.connectionState == "CONNECTED" })
    override suspend fun insertOrUpdateSession(session: RealtimeSessionEntity) { sessions[session.sessionId] = session }
    override suspend fun updateConnectionState(sessionId: String, state: String, timestamp: Long) {
        sessions[sessionId]?.let { sessions[sessionId] = it.copy(connectionState = state, lastHeartbeatAt = timestamp) }
    }
    override suspend fun deleteSession(sessionId: String) { sessions.remove(sessionId) }
    override suspend fun clearAllSessions() { sessions.clear() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeRepositoryTest {

    private lateinit var fakeEventDao: RealtimeEventDao
    private lateinit var fakeSessionDao: RealtimeSessionDao
    private lateinit var realtimeService: RealtimeService
    private lateinit var realtimeRepository: RealtimeRepository

    @Before
    fun setUp() {
        fakeEventDao = FakeRealtimeEventDao()
        fakeSessionDao = FakeRealtimeSessionDao()

        realtimeService = RealtimeService(
            realtimeEventDao = fakeEventDao,
            realtimeSessionDao = fakeSessionDao
        )
        realtimeRepository = RealtimeRepository(realtimeService)
    }

    @Test
    fun testRealtimeRepositoryConnectionState() = runTest {
        val state = realtimeRepository.connectionState.value
        assertNotNull(state)
    }

    @Test
    fun testBroadcastLocalEvent() = runTest {
        val stockEvent = RealtimeEvent.StockChanged(
            productId = "PROD_999",
            productName = "Barcode Scanner",
            previousStock = 20,
            newStock = 2,
            reason = "Sale"
        )

        realtimeRepository.broadcastLocalRealtimeEvent(stockEvent)
        // Verify method executed without throwing exception
    }
}

