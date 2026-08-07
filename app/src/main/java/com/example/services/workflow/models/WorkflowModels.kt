package com.example.services.workflow.models

data class DomainEvent(
    val eventType: String, // INVOICE_CREATED, INVOICE_PAID, CUSTOMER_CREATED, PAYMENT_RECEIVED, LOW_STOCK, PURCHASE_COMPLETED, EXPENSE_ADDED, BACKUP_COMPLETED, USER_LOGIN, SECURITY_ALERT, WEBHOOK_RECEIVED, SCHEDULE_TRIGGER
    val entityId: String,
    val businessId: String = "BIZ-DEFAULT",
    val branchId: String = "BRANCH-MAIN",
    val role: String = "ADMIN",
    val payload: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkflowNode(
    val id: String,
    val nodeType: String, // TRIGGER, CONDITION, BRANCH, LOOP, DELAY, ACTION, APPROVAL
    val name: String,
    val config: Map<String, String> = emptyMap(),
    val nextNodeIds: List<String> = emptyList()
)

data class AiWorkflowSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val triggerType: String,
    val recommendedNodes: List<WorkflowNode>,
    val category: String
)
