package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "task_center",
    indices = [
        Index("id"),
        Index("taskType"),
        Index("severity"),
        Index("priority"),
        Index("isCompleted"),
        Index("createdDate")
    ]
)
data class TaskCenterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val taskType: String, // BACKUP_REQUIRED, LOW_STOCK, OUTSTANDING_COLLECTION, SUPPLIER_PAYMENT_DUE, DB_OPTIMIZATION, PENDING_DRAFT_INVOICES, PENDING_DRAFT_PURCHASES, PENDING_EXPENSES
    val severity: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val isCompleted: Boolean = false,
    val actionUrl: String? = null,
    val referenceId: String? = null,
    val dueDate: Long? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null
)
