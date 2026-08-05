package com.example.features.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.TaskCenterEntity
import com.example.services.TaskEngineService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskCenterViewModel(
    private val taskEngineService: TaskEngineService
) : ViewModel() {

    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    private val _severityFilter = MutableStateFlow<String?>(null)
    val severityFilter: StateFlow<String?> = _severityFilter.asStateFlow()

    val pendingCount: StateFlow<Int> = taskEngineService.pendingTaskCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val tasks: StateFlow<List<TaskCenterEntity>> = combine(
        taskEngineService.allTasksFlow,
        _showCompleted,
        _severityFilter
    ) { allTasks, showDone, severity ->
        allTasks.filter { task ->
            val matchesCompletion = showDone || !task.isCompleted
            val matchesSeverity = severity == null || task.severity.equals(severity, ignoreCase = true)
            matchesCompletion && matchesSeverity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun setSeverityFilter(severity: String?) {
        _severityFilter.value = severity
    }

    fun completeTask(id: String) {
        viewModelScope.launch {
            taskEngineService.completeTask(id)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            taskEngineService.deleteTask(id)
        }
    }

    fun triggerTaskEvaluation() {
        viewModelScope.launch {
            taskEngineService.evaluateAndGenerateTasks()
        }
    }

    class Factory(
        private val taskEngineService: TaskEngineService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskCenterViewModel(taskEngineService) as T
        }
    }
}
