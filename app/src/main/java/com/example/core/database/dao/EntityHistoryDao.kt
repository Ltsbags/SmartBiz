package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.EntityHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: EntityHistoryEntity): Long

    @Query("SELECT * FROM entity_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<EntityHistoryEntity>>

    @Query("SELECT * FROM entity_history WHERE entityName = :entityName AND entityId = :entityId ORDER BY timestamp DESC")
    fun getHistoryForEntityFlow(entityName: String, entityId: String): Flow<List<EntityHistoryEntity>>

    @Query("SELECT * FROM entity_history WHERE entityName = :entityName ORDER BY timestamp DESC")
    fun getHistoryByEntityNameFlow(entityName: String): Flow<List<EntityHistoryEntity>>

    @Query("DELETE FROM entity_history WHERE timestamp < :thresholdTimestamp")
    suspend fun deleteHistoryOlderThan(thresholdTimestamp: Long): Int
}
