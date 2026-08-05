package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionStatus = 'ACTIVE' ORDER BY loginTime DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE sessionStatus = 'ACTIVE' ORDER BY loginTime DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>

    @Query("UPDATE sessions SET sessionStatus = 'LOGGED_OUT' WHERE userId = :userId AND sessionStatus = 'ACTIVE'")
    suspend fun invalidateActiveSessions(userId: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
}
