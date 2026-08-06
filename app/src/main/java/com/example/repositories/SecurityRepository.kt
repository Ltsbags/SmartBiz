package com.example.repositories

import com.example.core.database.dao.SecurityEventDao
import com.example.core.database.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(
    private val securityEventDao: SecurityEventDao
) {

    val allEvents: Flow<List<SecurityEventEntity>> = securityEventDao.getAllEventsFlow()

    suspend fun recordSecurityEvent(event: SecurityEventEntity): Long {
        return securityEventDao.insertEvent(event)
    }

    suspend fun recordSecurityEvents(events: List<SecurityEventEntity>) {
        securityEventDao.insertEvents(events)
    }

    fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>> {
        return securityEventDao.getAllEventsFlow()
    }

    fun getRecentSecurityEventsFlow(limit: Int = 100): Flow<List<SecurityEventEntity>> {
        return securityEventDao.getRecentEventsFlow(limit)
    }

    fun getEventsBySeverityFlow(severity: String): Flow<List<SecurityEventEntity>> {
        return securityEventDao.getEventsBySeverityFlow(severity)
    }

    fun getEventsByTypeFlow(eventType: String): Flow<List<SecurityEventEntity>> {
        return securityEventDao.getEventsByTypeFlow(eventType)
    }

    fun getEventCountFlow(): Flow<Int> {
        return securityEventDao.getEventCountFlow()
    }

    fun getCriticalEventCountFlow(): Flow<Int> {
        return securityEventDao.getCriticalEventCountFlow()
    }

    suspend fun cleanupOldSecurityEvents(thresholdTimestamp: Long): Int {
        return securityEventDao.deleteEventsOlderThan(thresholdTimestamp)
    }
}
