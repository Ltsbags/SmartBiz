package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "audit_logs",
    indices = [
        Index("auditId"),
        Index("userId"),
        Index("module"),
        Index("timestamp"),
        Index("severity"),
        Index("entityName"),
        Index("entityId"),
        Index("businessId"),
        Index("branchId"),
        Index("action")
    ]
)
data class AuditLogEntity(
    @PrimaryKey
    val auditId: String = UUID.randomUUID().toString(),
    val businessId: String = "default_biz",
    val branchId: String = "main_branch",
    val userId: String = "system",
    val userName: String = "System User",
    val sessionId: String = "",
    val module: String, // AUTH, PRODUCT, CUSTOMER, SUPPLIER, INVOICE, PURCHASE, EXPENSE, INCOME, BUSINESS, BRANCH, SETTINGS, BACKUP, RBAC, SYSTEM
    val entityName: String = "",
    val entityId: String = "",
    val action: String,
    val severity: String = "INFO", // INFO, WARNING, CRITICAL
    val oldValueJson: String? = null,
    val newValueJson: String? = null,
    val description: String,
    val ipAddress: String = "127.0.0.1",
    val deviceName: String = "Android Device",
    val appVersion: String = "1.0.0",
    val timestamp: Long = System.currentTimeMillis()
)
