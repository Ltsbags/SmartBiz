package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey
    val key: String, // e.g. low_stock_alerts, payment_alerts, business_summary, security_alerts, system_alerts, reminder_sounds, vibration
    val title: String,
    val description: String,
    val category: String = "SYSTEM",
    val isEnabled: Boolean = true,
    val updatedDate: Long = System.currentTimeMillis()
)
