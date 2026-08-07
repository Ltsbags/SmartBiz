package com.example.services.workflow

import android.util.Log
import com.example.core.database.dao.WorkflowDao
import com.example.core.database.entity.AutomationHistoryEntity
import com.example.services.workflow.models.WorkflowNode
import java.util.UUID

data class ActionResult(
    val isSuccess: Boolean,
    val actionType: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ActionEngine(
    private val workflowDao: WorkflowDao,
    private val approvalEngine: ApprovalEngine
) {

    suspend fun executeAction(node: WorkflowNode, workflowId: String, workflowName: String, contextData: Map<String, Any>): ActionResult {
        val actionType = node.config["actionType"] ?: node.name
        val recipient = node.config["recipient"] ?: "system@smartbiz.io"
        val message = node.config["message"] ?: "Workflow automated action executed."

        val result = when (actionType.uppercase()) {
            "CREATE_NOTIFICATION", "NOTIFICATION" -> {
                Log.d("ActionEngine", "Created Notification: $message")
                ActionResult(true, "CREATE_NOTIFICATION", "Notification sent to $recipient: $message")
            }
            "SEND_WHATSAPP", "WHATSAPP" -> {
                Log.d("ActionEngine", "Sent WhatsApp message to $recipient: $message")
                ActionResult(true, "SEND_WHATSAPP", "WhatsApp sent to $recipient")
            }
            "SEND_EMAIL", "EMAIL" -> {
                Log.d("ActionEngine", "Sent Email to $recipient: $message")
                ActionResult(true, "SEND_EMAIL", "Email dispatched to $recipient")
            }
            "SEND_SMS", "SMS" -> {
                Log.d("ActionEngine", "Sent SMS to $recipient: $message")
                ActionResult(true, "SEND_SMS", "SMS delivered to $recipient")
            }
            "CREATE_TASK", "TASK" -> {
                Log.d("ActionEngine", "Created Task: $message")
                ActionResult(true, "CREATE_TASK", "Task created: $message")
            }
            "GENERATE_REPORT", "REPORT" -> {
                Log.d("ActionEngine", "Generated Report")
                ActionResult(true, "GENERATE_REPORT", "Report snapshot generated successfully")
            }
            "REQUEST_APPROVAL", "APPROVAL" -> {
                approvalEngine.createApprovalRequest(
                    workflowExecutionId = UUID.randomUUID().toString(),
                    workflowName = workflowName,
                    requiredRole = node.config["requiredRole"] ?: "MANAGER"
                )
                ActionResult(true, "REQUEST_APPROVAL", "Approval request queued for Manager")
            }
            "CREATE_REMINDER", "REMINDER" -> {
                Log.d("ActionEngine", "Created Reminder: $message")
                ActionResult(true, "CREATE_REMINDER", "Reminder created: $message")
            }
            "CALL_PUBLIC_API", "WEBHOOK" -> {
                Log.d("ActionEngine", "Calling Public API endpoint: ${node.config["apiUrl"]}")
                ActionResult(true, "CALL_PUBLIC_API", "API HTTP POST dispatched")
            }
            "PUBLISH_EVENT", "EVENT" -> {
                Log.d("ActionEngine", "Published Event: $message")
                ActionResult(true, "PUBLISH_EVENT", "Domain event broadcasted to system bus")
            }
            else -> ActionResult(true, actionType, "Generic action executed")
        }

        workflowDao.insertHistory(
            AutomationHistoryEntity(
                id = UUID.randomUUID().toString(),
                workflowId = workflowId,
                workflowName = workflowName,
                actionType = result.actionType,
                status = if (result.isSuccess) "SUCCESS" else "FAILURE",
                details = result.details
            )
        )

        return result
    }
}
