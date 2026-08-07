package com.example.repositories

import com.example.core.database.dao.CommunicationDao
import com.example.core.database.entity.CommunicationAutomationRuleEntity
import com.example.core.database.entity.CommunicationLogEntity
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.core.database.entity.CommunicationTemplateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CommunicationRepository(
    private val communicationDao: CommunicationDao
) {
    val allMessages: Flow<List<CommunicationMessageEntity>> = communicationDao.getAllMessages()
    val allTemplates: Flow<List<CommunicationTemplateEntity>> = communicationDao.getAllActiveTemplates()
    val allAutomationRules: Flow<List<CommunicationAutomationRuleEntity>> = communicationDao.getAllAutomationRules()

    fun getMessagesByStatus(status: String): Flow<List<CommunicationMessageEntity>> =
        communicationDao.getMessagesByStatus(status)

    fun getMessagesByChannel(channel: String): Flow<List<CommunicationMessageEntity>> =
        communicationDao.getMessagesByChannel(channel)

    suspend fun getMessageById(id: Long): CommunicationMessageEntity? =
        communicationDao.getMessageById(id)

    suspend fun saveMessage(message: CommunicationMessageEntity): Long =
        communicationDao.insertMessage(message)

    suspend fun updateMessageStatus(id: Long, status: String, details: String, now: Long = System.currentTimeMillis()) =
        communicationDao.updateMessageStatus(id, status, details, now)

    suspend fun saveTemplate(template: CommunicationTemplateEntity): Long =
        communicationDao.insertTemplate(template)

    suspend fun deleteTemplate(id: Long) =
        communicationDao.deleteTemplate(id)

    suspend fun saveAutomationRule(rule: CommunicationAutomationRuleEntity): Long =
        communicationDao.insertAutomationRule(rule)

    suspend fun toggleAutomationRule(id: Long, isEnabled: Boolean) =
        communicationDao.toggleAutomationRule(id, isEnabled)

    suspend fun addLog(messageId: Long, eventType: String, details: String): Long {
        return communicationDao.insertLog(
            CommunicationLogEntity(
                messageId = messageId,
                eventType = eventType,
                details = details
            )
        )
    }

    fun getLogsForMessage(messageId: Long): Flow<List<CommunicationLogEntity>> =
        communicationDao.getLogsForMessage(messageId)

    suspend fun getPendingDispatchMessages(): List<CommunicationMessageEntity> =
        communicationDao.getPendingDispatchMessages()

    suspend fun getActiveRulesForEvent(eventType: String): List<CommunicationAutomationRuleEntity> =
        communicationDao.getActiveRulesForEvent(eventType)

    suspend fun getTemplateById(templateId: String): CommunicationTemplateEntity? =
        communicationDao.getTemplateByTemplateId(templateId)

    suspend fun seedDefaultTemplatesIfEmpty() {
        val existing = allTemplates.first()
        if (existing.isNotEmpty()) return

        val defaults = listOf(
            CommunicationTemplateEntity(
                templateId = "INV_SEND",
                name = "Invoice Delivery",
                channel = "WHATSAPP",
                subjectTemplate = "Invoice #{{invoice_number}} from {{business_name}}",
                bodyTemplate = "Dear {{customer_name}},\n\nYour invoice #{{invoice_number}} for {{invoice_amount}} is ready. Due date: {{due_date}}.\n\nThank you for choosing {{business_name}}!",
                category = "BILLING"
            ),
            CommunicationTemplateEntity(
                templateId = "PAYMENT_REMINDER",
                name = "Payment Reminder",
                channel = "WHATSAPP",
                subjectTemplate = "Payment Due: Invoice #{{invoice_number}}",
                bodyTemplate = "Hi {{customer_name}},\n\nThis is a friendly reminder that invoice #{{invoice_number}} with outstanding balance {{outstanding_amount}} is due on {{due_date}}.\n\nPlease complete payment at your earliest convenience.",
                category = "REMINDER"
            ),
            CommunicationTemplateEntity(
                templateId = "LOW_STOCK_ALERT",
                name = "Low Stock Alert",
                channel = "SMS",
                subjectTemplate = "Low Stock: {{product_name}}",
                bodyTemplate = "Alert: Item {{product_name}} at branch {{branch_name}} is down to {{stock_quantity}} units. Reorder recommended.",
                category = "ALERT"
            ),
            CommunicationTemplateEntity(
                templateId = "MONTHLY_STATEMENT",
                name = "Monthly Customer Statement",
                channel = "EMAIL",
                subjectTemplate = "Monthly Account Statement for {{customer_name}}",
                bodyTemplate = "Dear {{customer_name}},\n\nPlease find attached your account statement for {{statement_period}} from {{business_name}}.\nTotal Outstanding: {{outstanding_amount}}.",
                category = "BILLING"
            ),
            CommunicationTemplateEntity(
                templateId = "BACKUP_REMINDER",
                name = "Database Backup Status",
                channel = "EMAIL",
                subjectTemplate = "System Backup Notification - {{business_name}}",
                bodyTemplate = "Hello Administrator,\n\nScheduled system backup for {{business_name}} completed successfully at {{due_date}}.",
                category = "SYSTEM"
            ),
            CommunicationTemplateEntity(
                templateId = "SECURITY_ALERT",
                name = "Security Alert Notification",
                channel = "SMS",
                subjectTemplate = "Security Notice",
                bodyTemplate = "Security Alert: New login detected for user account at branch {{branch_name}}. Contact admin if unrecognized.",
                category = "ALERT"
            )
        )

        defaults.forEach { communicationDao.insertTemplate(it) }

        val defaultRules = listOf(
            CommunicationAutomationRuleEntity(
                ruleName = "Auto Send WhatsApp on Invoice Completed",
                eventType = "INVOICE_CREATED",
                targetChannel = "WHATSAPP",
                templateId = "INV_SEND",
                isEnabled = true
            ),
            CommunicationAutomationRuleEntity(
                ruleName = "Auto Send SMS Reminder on Payment Due",
                eventType = "INVOICE_OVERDUE",
                targetChannel = "SMS",
                templateId = "PAYMENT_REMINDER",
                isEnabled = true
            ),
            CommunicationAutomationRuleEntity(
                ruleName = "Auto Notify Admin on Low Stock",
                eventType = "LOW_STOCK_ALERT",
                targetChannel = "SMS",
                templateId = "LOW_STOCK_ALERT",
                isEnabled = true
            ),
            CommunicationAutomationRuleEntity(
                ruleName = "Auto Dispatch Monthly Statement",
                eventType = "MONTHLY_STATEMENT",
                targetChannel = "EMAIL",
                templateId = "MONTHLY_STATEMENT",
                isEnabled = true
            ),
            CommunicationAutomationRuleEntity(
                ruleName = "Backup Status Email Notice",
                eventType = "BACKUP_REMINDER",
                targetChannel = "EMAIL",
                templateId = "BACKUP_REMINDER",
                isEnabled = true
            ),
            CommunicationAutomationRuleEntity(
                ruleName = "Security Event SMS Alert",
                eventType = "SECURITY_ALERT",
                targetChannel = "SMS",
                templateId = "SECURITY_ALERT",
                isEnabled = true
            )
        )

        defaultRules.forEach { communicationDao.insertAutomationRule(it) }
    }
}
