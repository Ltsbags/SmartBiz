package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SecurityEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<SecurityEventEntity>)

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEventsFlow(limit: Int = 100): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE severity = :severity ORDER BY timestamp DESC")
    fun getEventsBySeverityFlow(severity: String): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByTypeFlow(eventType: String): Flow<List<SecurityEventEntity>>

    @Query("SELECT COUNT(*) FROM security_events")
    fun getEventCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM security_events WHERE severity = 'CRITICAL'")
    fun getCriticalEventCountFlow(): Flow<Int>

    @Query("DELETE FROM security_events WHERE timestamp < :thresholdTimestamp")
    suspend fun deleteEventsOlderThan(thresholdTimestamp: Long): Int
}
