package com.example.repositories

import com.example.core.database.dao.ScheduledTaskDao
import com.example.core.database.entity.ScheduledTaskEntity
import kotlinx.coroutines.flow.Flow

class SchedulerRepository(
    private val taskDao: ScheduledTaskDao
) {

    val allTasksFlow: Flow<List<ScheduledTaskEntity>> = taskDao.getAllTasksFlow()
    val activeTasksFlow: Flow<List<ScheduledTaskEntity>> = taskDao.getActiveTasksFlow()

    suspend fun addTask(task: ScheduledTaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: ScheduledTaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun getDueTasks(currentTime: Long = System.currentTimeMillis()): List<ScheduledTaskEntity> {
        return taskDao.getDueTasks(currentTime)
    }

    suspend fun updateTaskExecution(id: String, status: String, lastRun: Long, nextRun: Long) {
        taskDao.updateTaskExecution(id, status, lastRun, nextRun)
    }

    suspend fun deleteTask(id: String) {
        taskDao.deleteTask(id)
    }
}
