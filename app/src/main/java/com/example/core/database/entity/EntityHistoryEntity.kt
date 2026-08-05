package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "entity_history",
    indices = [
        Index("historyId"),
        Index("entityName"),
        Index("entityId"),
        Index("userId"),
        Index("timestamp")
    ]
)
data class EntityHistoryEntity(
    @PrimaryKey
    val historyId: String = UUID.randomUUID().toString(),
    val entityName: String,
    val entityId: String,
    val action: String, // CREATED, UPDATED, DELETED, RESTORED, ADJUSTED
    val oldValueJson: String? = null,
    val newValueJson: String? = null,
    val modifiedFieldsJson: String? = null, // JSON array of field names e.g. ["price", "quantity"]
    val userId: String = "system",
    val userName: String = "System User",
    val businessId: String = "default_biz",
    val branchId: String = "main_branch",
    val timestamp: Long = System.currentTimeMillis()
)
