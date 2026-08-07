package com.example.repositories

import com.example.core.database.dao.WorkflowDao
import com.example.core.database.entity.ApprovalRequestEntity
import com.example.core.database.entity.AutomationHistoryEntity
import com.example.core.database.entity.RuleEntity
import com.example.core.database.entity.WorkflowEntity
import com.example.core.database.entity.WorkflowExecutionEntity
import com.example.services.workflow.AiWorkflowAssistant
import com.example.services.workflow.ApprovalEngine
import com.example.services.workflow.RuleEngine
import com.example.services.workflow.WorkflowEngine
import com.example.services.workflow.models.AiWorkflowSuggestion
import com.example.services.workflow.models.DomainEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WorkflowRepository(
    private val workflowDao: WorkflowDao,
    val workflowEngine: WorkflowEngine,
    val ruleEngine: RuleEngine,
    val approvalEngine: ApprovalEngine,
    val aiAssistant: AiWorkflowAssistant
) {

    fun getAllWorkflows(): Flow<List<WorkflowEntity>> = workflowDao.getAllWorkflows()

    fun getAllExecutions(): Flow<List<WorkflowExecutionEntity>> = workflowDao.getAllExecutions()

    fun getAllRules(): Flow<List<RuleEntity>> = workflowDao.getAllRules()

    fun getAllApprovals(): Flow<List<ApprovalRequestEntity>> = workflowDao.getAllApprovals()

    fun getPendingApprovals(): Flow<List<ApprovalRequestEntity>> = workflowDao.getPendingApprovals()

    fun getAllHistory(): Flow<List<AutomationHistoryEntity>> = workflowDao.getAllHistory()

    suspend fun saveWorkflow(
        id: String = UUID.randomUUID().toString(),
        name: String,
        description: String,
        triggerType: String,
        nodesJson: String = "[]",
        isActive: Boolean = true,
        businessId: String = "BIZ-DEFAULT",
        branchId: String = "BRANCH-MAIN"
    ): WorkflowEntity {
        val entity = WorkflowEntity(
            id = id,
            name = name,
            description = description,
            triggerType = triggerType,
            isActive = isActive,
            businessId = businessId,
            branchId = branchId,
            nodesJson = nodesJson,
            updatedAt = System.currentTimeMillis()
        )
        workflowDao.insertWorkflow(entity)
        return entity
    }

    suspend fun toggleWorkflowStatus(id: String, isActive: Boolean) {
        val existing = workflowDao.getWorkflowById(id) ?: return
        workflowDao.updateWorkflow(existing.copy(isActive = isActive, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteWorkflow(id: String) {
        workflowDao.deleteWorkflow(id)
    }

    suspend fun saveRule(
        id: String = UUID.randomUUID().toString(),
        workflowId: String? = null,
        name: String,
        field: String,
        operator: String,
        value: String,
        isActive: Boolean = true
    ) {
        val rule = RuleEntity(
            id = id,
            workflowId = workflowId,
            name = name,
            field = field,
            operator = operator,
            value = value,
            isActive = isActive
        )
        workflowDao.insertRule(rule)
    }

    suspend fun deleteRule(id: String) {
        workflowDao.deleteRule(id)
    }

    suspend fun approveRequest(id: String, approverName: String, notes: String? = null) {
        approvalEngine.approveRequest(id, approverName, notes)
    }

    suspend fun rejectRequest(id: String, approverName: String, notes: String? = null) {
        approvalEngine.rejectRequest(id, approverName, notes)
    }

    suspend fun escalateRequest(id: String, targetRole: String) {
        approvalEngine.escalateRequest(id, targetRole)
    }

    fun getAiSuggestions(): List<AiWorkflowSuggestion> {
        return aiAssistant.generateSuggestions()
    }

    suspend fun triggerTestEvent(eventType: String, payload: Map<String, Any> = emptyMap()) {
        val event = DomainEvent(
            eventType = eventType,
            entityId = UUID.randomUUID().toString(),
            payload = payload
        )
        workflowEngine.onDomainEvent(event)
    }
}
