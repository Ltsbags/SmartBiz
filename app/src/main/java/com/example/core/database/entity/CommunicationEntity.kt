package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communication_messages")
data class CommunicationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageGuid: String,
    val channel: String, // "WHATSAPP", "EMAIL", "SMS", "PUSH", "TELEGRAM", "SLACK"
    val recipient: String, // Phone, Email or Token
    val recipientName: String = "",
    val subject: String = "",
    val body: String,
    val templateId: String = "",
    val status: String = "PENDING", // "PENDING", "QUEUED", "SENDING", "DELIVERED", "FAILED", "RETRY", "CANCELLED"
    val deliveryStatusDetails: String = "",
    val relatedEntityType: String = "", // "INVOICE", "PAYMENT_REMINDER", "PURCHASE_ORDER", "CUSTOMER", "SYSTEM"
    val relatedEntityId: String = "",
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val scheduledTime: Long = System.currentTimeMillis(),
    val sentTime: Long? = null,
    val deliveredTime: Long? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "communication_templates")
data class CommunicationTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String, // e.g. "INV_SEND", "PAYMENT_REMINDER", "ORDER_CONFIRMATION"
    val name: String,
    val channel: String, // "WHATSAPP", "EMAIL", "SMS", "ALL"
    val subjectTemplate: String = "",
    val bodyTemplate: String,
    val category: String = "BILLING", // "BILLING", "REMINDER", "MARKETING", "ALERT"
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "communication_automation_rules")
data class CommunicationAutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val eventType: String, // "INVOICE_CREATED", "INVOICE_OVERDUE", "PAYMENT_RECEIVED", "LOW_STOCK_ALERT"
    val targetChannel: String, // "WHATSAPP", "EMAIL", "SMS"
    val templateId: String,
    val isEnabled: Boolean = true,
    val conditionsJson: String = "{}",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "communication_logs")
data class CommunicationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val eventType: String, // "QUEUED", "DISPATCHED", "DELIVERED", "FAILED_RETRY", "CANCELLED"
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
