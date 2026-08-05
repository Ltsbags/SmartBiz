package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "security_events",
    indices = [
        Index("eventId"),
        Index("eventType"),
        Index("severity"),
        Index("module"),
        Index("userId"),
        Index("timestamp")
    ]
)
data class SecurityEventEntity(
    @PrimaryKey
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String,
    val severity: String = "INFO",
    val description: String,
    val module: String = "APP_SECURITY",
    val userId: String = "system",
    val userName: String = "System User",
    val deviceName: String = "Android POS",
    val ipAddress: String = "127.0.0.1",
    val timestamp: Long = System.currentTimeMillis()
)
