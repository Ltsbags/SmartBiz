package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.LoginHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LoginHistoryEntity): Long

    @Query("SELECT * FROM login_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryForUser(userId: String): Flow<List<LoginHistoryEntity>>

    @Query("SELECT * FROM login_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistory(userId: String, limit: Int = 20): List<LoginHistoryEntity>

    @Query("DELETE FROM login_history WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)
}
