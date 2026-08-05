package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.ScheduledTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ScheduledTaskEntity): Long

    @Update
    suspend fun updateTask(task: ScheduledTaskEntity)

    @Query("SELECT * FROM scheduled_tasks ORDER BY nextRunTimestamp ASC")
    fun getAllTasksFlow(): Flow<List<ScheduledTaskEntity>>

    @Query("SELECT * FROM scheduled_tasks WHERE isEnabled = 1 ORDER BY nextRunTimestamp ASC")
    fun getActiveTasksFlow(): Flow<List<ScheduledTaskEntity>>

    @Query("SELECT * FROM scheduled_tasks WHERE isEnabled = 1 AND nextRunTimestamp <= :currentTime")
    suspend fun getDueTasks(currentTime: Long = System.currentTimeMillis()): List<ScheduledTaskEntity>

    @Query("UPDATE scheduled_tasks SET status = :status, lastRunTimestamp = :lastRun, nextRunTimestamp = :nextRun WHERE id = :id")
    suspend fun updateTaskExecution(id: String, status: String, lastRun: Long, nextRun: Long)

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}
