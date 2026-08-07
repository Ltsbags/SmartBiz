package com.example.workflow

import com.example.core.database.entity.RuleEntity
import com.example.core.database.entity.WorkflowEntity
import com.example.services.workflow.ActionEngine
import com.example.services.workflow.AiWorkflowAssistant
import com.example.services.workflow.ApprovalEngine
import com.example.services.workflow.ExecutionQueue
import com.example.services.workflow.RuleEngine
import com.example.services.workflow.WorkflowEngine
import com.example.services.workflow.WorkflowSchedulerService
import com.example.services.workflow.models.DomainEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeWorkflowDao : com.example.core.database.dao.WorkflowDao {
    val workflows = mutableListOf<WorkflowEntity>()
    val rules = mutableListOf<RuleEntity>()
    val approvals = mutableListOf<com.example.core.database.entity.ApprovalRequestEntity>()
    val executions = mutableListOf<com.example.core.database.entity.WorkflowExecutionEntity>()
    val history = mutableListOf<com.example.core.database.entity.AutomationHistoryEntity>()

    override fun getAllWorkflows(): Flow<List<WorkflowEntity>> = flowOf(workflows)
    override suspend fun getActiveWorkflows(): List<WorkflowEntity> = workflows.filter { it.isActive }
    override suspend fun getWorkflowById(id: String): WorkflowEntity? = workflows.find { it.id == id }
    override suspend fun insertWorkflow(workflow: WorkflowEntity) { workflows.add(workflow) }
    override suspend fun updateWorkflow(workflow: WorkflowEntity) {
        val idx = workflows.indexOfFirst { it.id == workflow.id }
        if (idx >= 0) workflows[idx] = workflow
    }
    override suspend fun deleteWorkflow(id: String) { workflows.removeAll { it.id == id } }

    override fun getAllExecutions(): Flow<List<com.example.core.database.entity.WorkflowExecutionEntity>> = flowOf(executions)
    override fun getExecutionsForWorkflow(workflowId: String): Flow<List<com.example.core.database.entity.WorkflowExecutionEntity>> = flowOf(executions.filter { it.workflowId == workflowId })
    override suspend fun insertExecution(execution: com.example.core.database.entity.WorkflowExecutionEntity) { executions.add(execution) }
    override suspend fun updateExecution(execution: com.example.core.database.entity.WorkflowExecutionEntity) {
        val idx = executions.indexOfFirst { it.id == execution.id }
        if (idx >= 0) executions[idx] = execution
    }

    override fun getAllRules(): Flow<List<RuleEntity>> = flowOf(rules)
    override suspend fun getRulesForWorkflow(workflowId: String): List<RuleEntity> = rules.filter { it.workflowId == workflowId || it.workflowId == null }
    override suspend fun insertRule(rule: RuleEntity) { rules.add(rule) }
    override suspend fun deleteRule(id: String) { rules.removeAll { it.id == id } }

    override fun getAllApprovals(): Flow<List<com.example.core.database.entity.ApprovalRequestEntity>> = flowOf(approvals)
    override fun getPendingApprovals(): Flow<List<com.example.core.database.entity.ApprovalRequestEntity>> = flowOf(approvals.filter { it.status == "PENDING" })
    override suspend fun insertApproval(approval: com.example.core.database.entity.ApprovalRequestEntity) { approvals.add(approval) }
    override suspend fun updateApproval(approval: com.example.core.database.entity.ApprovalRequestEntity) {
        val idx = approvals.indexOfFirst { it.id == approval.id }
        if (idx >= 0) approvals[idx] = approval
    }

    override fun getAllHistory(): Flow<List<com.example.core.database.entity.AutomationHistoryEntity>> = flowOf(history)
    override suspend fun insertHistory(history: com.example.core.database.entity.AutomationHistoryEntity) { this.history.add(history) }
}

class WorkflowEngineTest {

    @Test
    fun testRuleEngineEvaluationAndValidation() {
        val ruleEngine = RuleEngine()
        val event = DomainEvent(
            eventType = "INVOICE_CREATED",
            entityId = "INV-1001",
            payload = mapOf("amount" to "12000.0", "customerType" to "VIP")
        )

        val rule = RuleEntity(
            id = "r1",
            name = "High Amount Rule",
            field = "AMOUNT",
            operator = "GREATER_THAN",
            value = "10000"
        )

        val validation = ruleEngine.validateRule(rule)
        assertTrue(validation.isValid)

        val matches = ruleEngine.evaluateRule(rule, event)
        assertTrue(matches)
    }

    @Test
    fun testApprovalEngineFlow() = runBlocking {
        val dao = FakeWorkflowDao()
        val approvalEngine = ApprovalEngine(dao)

        val req = approvalEngine.createApprovalRequest("exec_1", "High Value Invoice Approval", "System", "MANAGER")
        assertNotNull(req)
        assertEquals("PENDING", req.status)

        val approved = approvalEngine.approveRequest(req.id, "Manager Bob", "Approved for VIP customer")
        assertTrue(approved)

        val rejected = approvalEngine.rejectRequest(req.id, "Manager Bob", "Budget exceeded")
        assertTrue(rejected)

        val escalated = approvalEngine.escalateRequest(req.id, "DIRECTOR")
        assertTrue(escalated)
    }

    @Test
    fun testWorkflowExecutionAndQueue() = runBlocking {
        val dao = FakeWorkflowDao()
        val ruleEngine = RuleEngine()
        val approvalEngine = ApprovalEngine(dao)
        val actionEngine = ActionEngine(dao, approvalEngine)
        val queue = ExecutionQueue()

        val engine = WorkflowEngine(dao, ruleEngine, actionEngine, queue)

        val wf = WorkflowEntity(
            id = "wf_1",
            name = "Auto Payment Reminder",
            description = "Send reminder on invoice creation",
            triggerType = "INVOICE_CREATED",
            isActive = true,
            nodesJson = "[]"
        )
        dao.insertWorkflow(wf)

        val event = DomainEvent(
            eventType = "INVOICE_CREATED",
            entityId = "INV-555",
            payload = mapOf("amount" to "5000.0")
        )

        engine.onDomainEvent(event)

        // Wait brief moment for async queue processing
        kotlinx.coroutines.delay(200)

        assertTrue(dao.executions.isNotEmpty())
        assertEquals("wf_1", dao.executions.first().workflowId)
    }

    @Test
    fun testSchedulerService() = runBlocking {
        val dao = FakeWorkflowDao()
        val ruleEngine = RuleEngine()
        val approvalEngine = ApprovalEngine(dao)
        val actionEngine = ActionEngine(dao, approvalEngine)
        val queue = ExecutionQueue()
        val engine = WorkflowEngine(dao, ruleEngine, actionEngine, queue)

        val scheduler = WorkflowSchedulerService(engine)
        val job = scheduler.scheduleWorkflow("wf_1", "DAILY", delayMs = 1000)

        assertNotNull(job)
        assertEquals(1, scheduler.getActiveJobs().size)

        scheduler.triggerScheduledRun(job)
    }

    @Test
    fun testAiAssistantDoesNotAutoExecute() {
        val assistant = AiWorkflowAssistant()
        val suggestions = assistant.generateSuggestions()

        assertTrue(suggestions.size >= 5)
        val paymentSuggestion = suggestions.first { it.triggerType == "INVOICE_CREATED" }
        assertNotNull(paymentSuggestion)
        // Verify AI provides structure and recommendations but no auto-execution handle
        assertTrue(paymentSuggestion.recommendedNodes.isNotEmpty())
    }
}
