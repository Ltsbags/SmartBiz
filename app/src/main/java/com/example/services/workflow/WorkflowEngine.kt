package com.example.services.workflow

import com.example.core.database.dao.WorkflowDao
import com.example.core.database.entity.RuleEntity
import com.example.core.database.entity.WorkflowExecutionEntity
import com.example.services.workflow.models.DomainEvent
import com.example.services.workflow.models.WorkflowNode
import java.util.UUID

class WorkflowEngine(
    private val workflowDao: WorkflowDao,
    private val ruleEngine: RuleEngine,
    private val actionEngine: ActionEngine,
    private val executionQueue: ExecutionQueue
) {

    suspend fun onDomainEvent(event: DomainEvent) {
        val activeWorkflows = workflowDao.getActiveWorkflows()
            .filter { it.triggerType.equals(event.eventType, ignoreCase = true) }

        for (workflow in activeWorkflows) {
            executionQueue.enqueue {
                processWorkflowExecution(workflow.id, workflow.name, event)
            }
        }
    }

    private suspend fun processWorkflowExecution(workflowId: String, workflowName: String, event: DomainEvent) {
        val executionId = UUID.randomUUID().toString()
        val execution = WorkflowExecutionEntity(
            id = executionId,
            workflowId = workflowId,
            workflowName = workflowName,
            triggerEvent = event.eventType,
            status = "RUNNING",
            executionDataJson = event.payload.toString(),
            startedAt = System.currentTimeMillis()
        )
        workflowDao.insertExecution(execution)

        try {
            // Fetch associated rules
            val rules = workflowDao.getRulesForWorkflow(workflowId)
            val passesRules = rules.all { ruleEngine.evaluateRule(it, event) }

            if (!passesRules) {
                workflowDao.updateExecution(
                    execution.copy(
                        status = "CANCELLED",
                        errorMessage = "Condition rules evaluated to false",
                        completedAt = System.currentTimeMillis()
                    )
                )
                return
            }

            // Execute sample action node
            val actionNode = WorkflowNode(
                id = "action_1",
                nodeType = "ACTION",
                name = "Automated Task",
                config = mapOf("actionType" to "CREATE_NOTIFICATION", "message" to "Trigger ${event.eventType} processed successfully")
            )
            actionEngine.executeAction(actionNode, workflowId, workflowName, event.payload)

            workflowDao.updateExecution(
                execution.copy(
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            workflowDao.updateExecution(
                execution.copy(
                    status = "FAILED",
                    errorMessage = e.localizedMessage ?: "Execution error",
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
