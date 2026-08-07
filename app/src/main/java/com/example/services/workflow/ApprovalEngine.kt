package com.example.services.workflow

import com.example.core.database.dao.WorkflowDao
import com.example.core.database.entity.ApprovalRequestEntity
import com.example.core.database.entity.AutomationHistoryEntity
import java.util.UUID

class ApprovalEngine(
    private val workflowDao: WorkflowDao
) {

    suspend fun createApprovalRequest(
        workflowExecutionId: String,
        workflowName: String,
        requesterName: String = "Workflow Engine",
        requiredRole: String = "MANAGER"
    ): ApprovalRequestEntity {
        val request = ApprovalRequestEntity(
            id = UUID.randomUUID().toString(),
            workflowExecutionId = workflowExecutionId,
            workflowName = workflowName,
            requesterName = requesterName,
            requiredRole = requiredRole,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        workflowDao.insertApproval(request)
        return request
    }

    suspend fun approveRequest(id: String, approverName: String, notes: String? = null): Boolean {
        val request = workflowDao.getAllApprovals()
        // Query database via dao or update directly
        val updated = ApprovalRequestEntity(
            id = id,
            workflowExecutionId = "",
            workflowName = "",
            requesterName = "",
            requiredRole = "",
            status = "APPROVED",
            approverNotes = notes,
            approvedBy = approverName,
            updatedAt = System.currentTimeMillis()
        )
        // Insert history log
        workflowDao.insertHistory(
            AutomationHistoryEntity(
                id = UUID.randomUUID().toString(),
                workflowId = id,
                workflowName = "Approval Request",
                actionType = "REQUEST_APPROVAL",
                status = "SUCCESS",
                details = "Approved by $approverName with notes: ${notes ?: "None"}"
            )
        )
        return true
    }

    suspend fun rejectRequest(id: String, approverName: String, notes: String? = null): Boolean {
        workflowDao.insertHistory(
            AutomationHistoryEntity(
                id = UUID.randomUUID().toString(),
                workflowId = id,
                workflowName = "Approval Request",
                actionType = "REQUEST_APPROVAL",
                status = "FAILURE",
                details = "Rejected by $approverName with notes: ${notes ?: "None"}"
            )
        )
        return true
    }

    suspend fun escalateRequest(id: String, targetRole: String = "DIRECTOR"): Boolean {
        workflowDao.insertHistory(
            AutomationHistoryEntity(
                id = UUID.randomUUID().toString(),
                workflowId = id,
                workflowName = "Approval Request",
                actionType = "ESCALATE_APPROVAL",
                status = "SUCCESS",
                details = "Escalated approval request $id to role: $targetRole"
            )
        )
        return true
    }
}
