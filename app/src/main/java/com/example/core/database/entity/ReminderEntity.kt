package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reminders",
    indices = [
        Index("id"),
        Index("module"),
        Index("repeatType"),
        Index("nextTrigger"),
        Index("isEnabled")
    ]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val module: String, // PAYMENT_DUE, CUSTOMER_OUTSTANDING, SUPPLIER_PAYMENT, LOW_STOCK, OUT_OF_STOCK, DAILY_CLOSING, MONTHLY_CLOSING, BACKUP_REMINDER, BUSINESS_HEALTH, CUSTOM
    val referenceId: String? = null,
    val repeatType: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    val nextTrigger: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
