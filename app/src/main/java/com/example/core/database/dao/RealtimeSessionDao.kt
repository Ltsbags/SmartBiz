package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.RealtimeSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RealtimeSessionDao {
    @Query("SELECT * FROM realtime_sessions ORDER BY lastHeartbeatAt DESC")
    fun getAllSessionsFlow(): Flow<List<RealtimeSessionEntity>>

    @Query("SELECT * FROM realtime_sessions WHERE userId = :userId LIMIT 1")
    suspend fun getSessionByUserId(userId: String): RealtimeSessionEntity?

    @Query("SELECT * FROM realtime_sessions WHERE connectionState = 'CONNECTED'")
    fun getActiveSessionsFlow(): Flow<List<RealtimeSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: RealtimeSessionEntity)

    @Query("UPDATE realtime_sessions SET connectionState = :state, lastHeartbeatAt = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateConnectionState(sessionId: String, state: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM realtime_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM realtime_sessions")
    suspend fun clearAllSessions()
}
