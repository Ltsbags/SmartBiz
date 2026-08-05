package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notifications",
    indices = [
        Index("id"),
        Index("businessId"),
        Index("type"),
        Index("severity"),
        Index("priority"),
        Index("status"),
        Index("createdDate")
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "default_biz",
    val branchId: String = "main_branch",
    val userId: String = "system",
    val type: String, // SECURITY, SALES, PURCHASES, INVENTORY, CUSTOMERS, SUPPLIERS, FINANCE, SYSTEM, REPORTS, CUSTOM
    val title: String,
    val message: String,
    val severity: String = "INFO", // INFO, WARNING, HIGH, CRITICAL
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val status: String = "UNREAD", // UNREAD, READ, ARCHIVED
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val scheduledDate: Long? = null,
    val deliveredDate: Long? = null,
    val readDate: Long? = null,
    val payloadJson: String = "{}"
)
