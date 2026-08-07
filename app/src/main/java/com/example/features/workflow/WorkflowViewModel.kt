package com.example.features.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.ApprovalRequestEntity
import com.example.core.database.entity.AutomationHistoryEntity
import com.example.core.database.entity.RuleEntity
import com.example.core.database.entity.WorkflowEntity
import com.example.core.database.entity.WorkflowExecutionEntity
import com.example.repositories.WorkflowRepository
import com.example.services.workflow.models.AiWorkflowSuggestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkflowViewModel(
    private val workflowRepository: WorkflowRepository
) : ViewModel() {

    val workflows: StateFlow<List<WorkflowEntity>> = workflowRepository.getAllWorkflows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val executions: StateFlow<List<WorkflowExecutionEntity>> = workflowRepository.getAllExecutions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<RuleEntity>> = workflowRepository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovals: StateFlow<List<ApprovalRequestEntity>> = workflowRepository.getPendingApprovals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApprovals: StateFlow<List<ApprovalRequestEntity>> = workflowRepository.getAllApprovals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationHistory: StateFlow<List<AutomationHistoryEntity>> = workflowRepository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiSuggestions: List<AiWorkflowSuggestion> = workflowRepository.getAiSuggestions()

    fun saveWorkflow(name: String, description: String, triggerType: String) {
        viewModelScope.launch {
            workflowRepository.saveWorkflow(
                name = name,
                description = description,
                triggerType = triggerType
            )
        }
    }

    fun toggleWorkflowStatus(id: String, isActive: Boolean) {
        viewModelScope.launch {
            workflowRepository.toggleWorkflowStatus(id, isActive)
        }
    }

    fun deleteWorkflow(id: String) {
        viewModelScope.launch {
            workflowRepository.deleteWorkflow(id)
        }
    }

    fun saveRule(name: String, field: String, operator: String, value: String) {
        viewModelScope.launch {
            workflowRepository.saveRule(
                name = name,
                field = field,
                operator = operator,
                value = value
            )
        }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch {
            workflowRepository.deleteRule(id)
        }
    }

    fun approveRequest(id: String, approverName: String, notes: String?) {
        viewModelScope.launch {
            workflowRepository.approveRequest(id, approverName, notes)
        }
    }

    fun rejectRequest(id: String, approverName: String, notes: String?) {
        viewModelScope.launch {
            workflowRepository.rejectRequest(id, approverName, notes)
        }
    }

    fun escalateRequest(id: String, targetRole: String) {
        viewModelScope.launch {
            workflowRepository.escalateRequest(id, targetRole)
        }
    }

    fun triggerTestEvent(eventType: String) {
        viewModelScope.launch {
            workflowRepository.triggerTestEvent(eventType, mapOf("amount" to "7500.00", "customerType" to "VIP"))
        }
    }

    class Factory(private val workflowRepository: WorkflowRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkflowViewModel(workflowRepository) as T
        }
    }
}
