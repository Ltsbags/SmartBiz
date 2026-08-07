package com.example.services.workflow

import com.example.services.workflow.models.AiWorkflowSuggestion
import com.example.services.workflow.models.WorkflowNode
import java.util.UUID

class AiWorkflowAssistant {

    fun generateSuggestions(): List<AiWorkflowSuggestion> {
        return listOf(
            AiWorkflowSuggestion(
                id = UUID.randomUUID().toString(),
                title = "Payment Reminder Automation",
                description = "Automatically send a polite WhatsApp & Email reminder 3 days after invoice issue if unpaid.",
                triggerType = "INVOICE_CREATED",
                category = "Finance",
                recommendedNodes = listOf(
                    WorkflowNode("n1", "TRIGGER", "Invoice Created", mapOf("eventType" to "INVOICE_CREATED")),
                    WorkflowNode("n2", "CONDITION", "Check Payment Status", mapOf("field" to "PAYMENT_STATUS", "operator" to "EQUALS", "value" to "UNPAID")),
                    WorkflowNode("n3", "DELAY", "Wait 3 Days", mapOf("delayDays" to "3")),
                    WorkflowNode("n4", "ACTION", "Send Reminders", mapOf("actionType" to "SEND_WHATSAPP", "recipient" to "Customer"))
                )
            ),
            AiWorkflowSuggestion(
                id = UUID.randomUUID().toString(),
                title = "Low Stock Reorder Alert",
                description = "When inventory quantity drops below threshold, notify the purchasing department and draft a Purchase Order.",
                triggerType = "LOW_STOCK",
                category = "Inventory",
                recommendedNodes = listOf(
                    WorkflowNode("n1", "TRIGGER", "Low Stock Event", mapOf("eventType" to "LOW_STOCK")),
                    WorkflowNode("n2", "CONDITION", "Stock Below 10 Units", mapOf("field" to "STOCK_LEVEL", "operator" to "LESS_THAN", "value" to "10")),
                    WorkflowNode("n3", "ACTION", "Notify Purchase Dept", mapOf("actionType" to "CREATE_TASK", "message" to "Reorder low stock items"))
                )
            ),
            AiWorkflowSuggestion(
                id = UUID.randomUUID().toString(),
                title = "High-Value Invoice Approval",
                description = "Require Manager approval for any sales invoice created with an amount exceeding $5,000.",
                triggerType = "INVOICE_CREATED",
                category = "Compliance",
                recommendedNodes = listOf(
                    WorkflowNode("n1", "TRIGGER", "Invoice Created", mapOf("eventType" to "INVOICE_CREATED")),
                    WorkflowNode("n2", "CONDITION", "Amount > $5,000", mapOf("field" to "AMOUNT", "operator" to "GREATER_THAN", "value" to "5000")),
                    WorkflowNode("n3", "APPROVAL", "Manager Approval", mapOf("requiredRole" to "MANAGER"))
                )
            ),
            AiWorkflowSuggestion(
                id = UUID.randomUUID().toString(),
                title = "Daily Financial Summary Report",
                description = "Generate and email daily sales and revenue reports to executives every evening.",
                triggerType = "SCHEDULE_TRIGGER",
                category = "Analytics",
                recommendedNodes = listOf(
                    WorkflowNode("n1", "TRIGGER", "Daily 6:00 PM Cron", mapOf("scheduleType" to "DAILY")),
                    WorkflowNode("n2", "ACTION", "Generate PDF Report", mapOf("actionType" to "GENERATE_REPORT")),
                    WorkflowNode("n3", "ACTION", "Email Executives", mapOf("actionType" to "SEND_EMAIL", "recipient" to "execs@smartbiz.io"))
                )
            ),
            AiWorkflowSuggestion(
                id = UUID.randomUUID().toString(),
                title = "Nightly Cloud Backup Reminder",
                description = "Verify data backup integrity every midnight and issue security alert if backup fails.",
                triggerType = "BACKUP_COMPLETED",
                category = "Security",
                recommendedNodes = listOf(
                    WorkflowNode("n1", "TRIGGER", "Backup Completed", mapOf("eventType" to "BACKUP_COMPLETED")),
                    WorkflowNode("n2", "ACTION", "Log Integrity Check", mapOf("actionType" to "PUBLISH_EVENT"))
                )
            )
        )
    }
}
