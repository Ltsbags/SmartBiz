package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val triggerType: String,
    val isActive: Boolean = true,
    val businessId: String = "BIZ-DEFAULT",
    val branchId: String = "BRANCH-MAIN",
    val nodesJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workflow_executions")
data class WorkflowExecutionEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val workflowName: String,
    val triggerEvent: String,
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED, WAITING_APPROVAL, CANCELLED
    val executionDataJson: String,
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "workflow_rules")
data class RuleEntity(
    @PrimaryKey val id: String,
    val workflowId: String? = null,
    val name: String,
    val field: String, // AMOUNT, BUSINESS, BRANCH, ROLE, CUSTOMER_TYPE, OUTSTANDING, STOCK_LEVEL, PAYMENT_STATUS, CUSTOM
    val operator: String, // EQUALS, GREATER_THAN, LESS_THAN, CONTAINS, AND, OR, NOT
    val value: String,
    val nestedRulesJson: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "approval_requests")
data class ApprovalRequestEntity(
    @PrimaryKey val id: String,
    val workflowExecutionId: String,
    val workflowName: String,
    val requesterName: String,
    val requiredRole: String,
    val status: String, // PENDING, APPROVED, REJECTED, ESCALATED, TIMEOUT
    val approverNotes: String? = null,
    val approvedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

@Entity(tableName = "automation_history")
data class AutomationHistoryEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val workflowName: String,
    val actionType: String,
    val status: String, // SUCCESS, FAILURE
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
