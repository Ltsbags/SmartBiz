package com.example.repositories

import com.example.core.database.dao.TaskCenterDao
import com.example.core.database.entity.TaskCenterEntity
import kotlinx.coroutines.flow.Flow

class TaskCenterRepository(
    private val taskDao: TaskCenterDao
) {

    val allTasksFlow: Flow<List<TaskCenterEntity>> = taskDao.getAllTasksFlow()
    val pendingTasksFlow: Flow<List<TaskCenterEntity>> = taskDao.getPendingTasksFlow()
    val pendingTaskCountFlow: Flow<Int> = taskDao.getPendingTaskCountFlow()

    suspend fun addTask(task: TaskCenterEntity): Long = taskDao.insertTask(task)

    suspend fun addTasks(tasks: List<TaskCenterEntity>) = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: TaskCenterEntity) = taskDao.updateTask(task)

    suspend fun getPendingTaskByType(taskType: String): TaskCenterEntity? = taskDao.getPendingTaskByType(taskType)

    suspend fun completeTask(id: String) = taskDao.completeTask(id)

    suspend fun deleteTask(id: String) = taskDao.deleteTask(id)

    suspend fun clearCompletedTasks() = taskDao.clearCompletedTasks()
}
