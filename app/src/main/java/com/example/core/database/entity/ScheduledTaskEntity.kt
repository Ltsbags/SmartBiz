package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scheduled_tasks",
    indices = [
        Index("id"),
        Index("taskType"),
        Index("nextRunTimestamp"),
        Index("isEnabled")
    ]
)
data class ScheduledTaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskName: String,
    val taskType: String, // DAILY_SUMMARY, WEEKLY_SUMMARY, MONTHLY_SUMMARY, DB_OPTIMIZATION, BACKUP_REMINDER, REPORT_REMINDER
    val cronOrFrequency: String, // DAILY, WEEKLY, MONTHLY
    val lastRunTimestamp: Long? = null,
    val nextRunTimestamp: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true,
    val status: String = "PENDING" // PENDING, RUNNING, COMPLETED, FAILED
)
