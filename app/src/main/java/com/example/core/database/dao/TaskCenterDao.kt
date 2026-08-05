package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.TaskCenterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCenterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskCenterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskCenterEntity>)

    @Update
    suspend fun updateTask(task: TaskCenterEntity)

    @Query("SELECT * FROM task_center ORDER BY isCompleted ASC, createdDate DESC")
    fun getAllTasksFlow(): Flow<List<TaskCenterEntity>>

    @Query("SELECT * FROM task_center WHERE isCompleted = 0 ORDER BY createdDate DESC")
    fun getPendingTasksFlow(): Flow<List<TaskCenterEntity>>

    @Query("SELECT COUNT(*) FROM task_center WHERE isCompleted = 0")
    fun getPendingTaskCountFlow(): Flow<Int>

    @Query("SELECT * FROM task_center WHERE taskType = :taskType AND isCompleted = 0 LIMIT 1")
    suspend fun getPendingTaskByType(taskType: String): TaskCenterEntity?

    @Query("UPDATE task_center SET isCompleted = 1, completedDate = :completedAt WHERE id = :id")
    suspend fun completeTask(id: String, completedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM task_center WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM task_center WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()
}
