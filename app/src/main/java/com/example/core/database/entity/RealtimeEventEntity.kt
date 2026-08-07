package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "realtime_events",
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["eventType"]),
        Index(value = ["module"]),
        Index(value = ["timestamp"]),
        Index(value = ["isProcessed"])
    ]
)
data class RealtimeEventEntity(
    @PrimaryKey
    val eventId: String,
    val eventType: String, // INVOICE_CREATED, STOCK_CHANGED, NOTIFICATION_CREATED, PRESENCE_CHANGED, etc.
    val module: String, // SALES, INVENTORY, AUTH, NOTIFICATION, AUDIT
    val entityId: String = "",
    val payloadJson: String,
    val severity: String = "INFO", // INFO, WARNING, ERROR, CRITICAL
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = true
)
