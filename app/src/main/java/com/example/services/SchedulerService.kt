package com.example.services

import com.example.core.database.entity.ScheduledTaskEntity
import com.example.repositories.SchedulerRepository
import kotlinx.coroutines.flow.Flow

class SchedulerService(
    private val schedulerRepository: SchedulerRepository,
    private val notificationEngine: NotificationEngine
) {

    val activeTasksFlow: Flow<List<ScheduledTaskEntity>> = schedulerRepository.activeTasksFlow
    val allTasksFlow: Flow<List<ScheduledTaskEntity>> = schedulerRepository.allTasksFlow

    suspend fun initializeDefaultTasksIfEmpty() {
        val defaults = listOf(
            ScheduledTaskEntity(
                taskName = "Daily Business Summary",
                taskType = "DAILY_SUMMARY",
                cronOrFrequency = "DAILY",
                isEnabled = true
            ),
            ScheduledTaskEntity(
                taskName = "Weekly Business Summary",
                taskType = "WEEKLY_SUMMARY",
                cronOrFrequency = "WEEKLY",
                isEnabled = true
            ),
            ScheduledTaskEntity(
                taskName = "Database Optimization Check",
                taskType = "DB_OPTIMIZATION",
                cronOrFrequency = "WEEKLY",
                isEnabled = true
            ),
            ScheduledTaskEntity(
                taskName = "Encrypted Backup Reminder",
                taskType = "BACKUP_REMINDER",
                cronOrFrequency = "DAILY",
                isEnabled = true
            )
        )

        val existing = schedulerRepository.getDueTasks(Long.MAX_VALUE)
        if (existing.isEmpty()) {
            for (task in defaults) {
                schedulerRepository.addTask(task)
            }
        }
    }

    suspend fun runPendingScheduledTasks() {
        val dueTasks = schedulerRepository.getDueTasks()
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        for (task in dueTasks) {
            schedulerRepository.updateTaskExecution(task.id, "RUNNING", now, now)

            when (task.taskType) {
                "DAILY_SUMMARY" -> {
                    notificationEngine.publishEvent(
                        NotificationEvent(
                            type = "REPORTS",
                            title = "Daily Summary Available",
                            message = "Your daily sales and expense totals have been compiled.",
                            severity = "INFO"
                        )
                    )
                }
                "WEEKLY_SUMMARY" -> {
                    notificationEngine.publishEvent(
                        NotificationEvent(
                            type = "REPORTS",
                            title = "Weekly Performance Report",
                            message = "Review your weekly financial indicators and customer growth.",
                            severity = "INFO"
                        )
                    )
                }
                "DB_OPTIMIZATION" -> {
                    notificationEngine.publishEvent(
                        NotificationEvent(
                            type = "SYSTEM",
                            title = "Database Maintenance Suggested",
                            message = "Database health check recommends index reindexing for high responsiveness.",
                            severity = "INFO"
                        )
                    )
                }
                "BACKUP_REMINDER" -> {
                    notificationEngine.publishEvent(
                        NotificationEvent(
                            type = "SYSTEM",
                            title = "Encrypted Backup Due",
                            message = "Ensure your local data is safely exported and backed up today.",
                            severity = "WARNING",
                            priority = "HIGH"
                        )
                    )
                }
            }

            val nextRun = when (task.cronOrFrequency) {
                "DAILY" -> now + dayMs
                "WEEKLY" -> now + (7 * dayMs)
                "MONTHLY" -> now + (30 * dayMs)
                else -> now + dayMs
            }

            schedulerRepository.updateTaskExecution(task.id, "COMPLETED", now, nextRun)
        }
    }
}
