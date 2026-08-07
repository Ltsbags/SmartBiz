package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.PresenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresenceDao {
    @Query("SELECT * FROM presence ORDER BY lastSeenAt DESC")
    fun getAllPresenceFlow(): Flow<List<PresenceEntity>>

    @Query("SELECT * FROM presence WHERE status != 'OFFLINE' ORDER BY userName ASC")
    fun getOnlineUsersFlow(): Flow<List<PresenceEntity>>

    @Query("SELECT * FROM presence WHERE userId = :userId LIMIT 1")
    suspend fun getPresenceByUserId(userId: String): PresenceEntity?

    @Query("SELECT COUNT(*) FROM presence WHERE status != 'OFFLINE'")
    fun getOnlineUsersCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePresence(presence: PresenceEntity)

    @Query("UPDATE presence SET status = :status, lastSeenAt = :lastSeenAt WHERE userId = :userId")
    suspend fun updatePresenceStatus(userId: String, status: String, lastSeenAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM presence WHERE userId = :userId")
    suspend fun deletePresence(userId: String)

    @Query("DELETE FROM presence")
    suspend fun clearAll()
}
