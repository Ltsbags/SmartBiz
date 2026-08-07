package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.RealtimeEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RealtimeEventDao {
    @Query("SELECT * FROM realtime_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<RealtimeEventEntity>>

    @Query("SELECT * FROM realtime_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEventsFlow(limit: Int = 50): Flow<List<RealtimeEventEntity>>

    @Query("SELECT * FROM realtime_events WHERE module = :module ORDER BY timestamp DESC")
    fun getEventsByModuleFlow(module: String): Flow<List<RealtimeEventEntity>>

    @Query("SELECT * FROM realtime_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByTypeFlow(eventType: String): Flow<List<RealtimeEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: RealtimeEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<RealtimeEventEntity>)

    @Query("DELETE FROM realtime_events WHERE eventId = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM realtime_events")
    suspend fun clearAllEvents()
}
