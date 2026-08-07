package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.ApprovalRequestEntity
import com.example.core.database.entity.AutomationHistoryEntity
import com.example.core.database.entity.RuleEntity
import com.example.core.database.entity.WorkflowEntity
import com.example.core.database.entity.WorkflowExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {

    // Workflows
    @Query("SELECT * FROM workflows ORDER BY updatedAt DESC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflows WHERE isActive = 1")
    suspend fun getActiveWorkflows(): List<WorkflowEntity>

    @Query("SELECT * FROM workflows WHERE id = :id")
    suspend fun getWorkflowById(id: String): WorkflowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun deleteWorkflow(id: String)

    // Executions
    @Query("SELECT * FROM workflow_executions ORDER BY startedAt DESC")
    fun getAllExecutions(): Flow<List<WorkflowExecutionEntity>>

    @Query("SELECT * FROM workflow_executions WHERE workflowId = :workflowId ORDER BY startedAt DESC")
    fun getExecutionsForWorkflow(workflowId: String): Flow<List<WorkflowExecutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: WorkflowExecutionEntity)

    @Update
    suspend fun updateExecution(execution: WorkflowExecutionEntity)

    // Rules
    @Query("SELECT * FROM workflow_rules ORDER BY name ASC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM workflow_rules WHERE workflowId = :workflowId")
    suspend fun getRulesForWorkflow(workflowId: String): List<RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity)

    @Query("DELETE FROM workflow_rules WHERE id = :id")
    suspend fun deleteRule(id: String)

    // Approvals
    @Query("SELECT * FROM approval_requests ORDER BY createdAt DESC")
    fun getAllApprovals(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approval_requests WHERE status = 'PENDING'")
    fun getPendingApprovals(): Flow<List<ApprovalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalRequestEntity)

    @Update
    suspend fun updateApproval(approval: ApprovalRequestEntity)

    // History
    @Query("SELECT * FROM automation_history ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<AutomationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AutomationHistoryEntity)
}
