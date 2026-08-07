package com.example.features.communication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CommunicationAutomationRuleEntity
import com.example.repositories.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AutomationUiState(
    val rules: List<CommunicationAutomationRuleEntity> = emptyList(),
    val isLoading: Boolean = false,
    val userNotice: String? = null
)

class AutomationViewModel(
    private val repository: CommunicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutomationUiState())
    val uiState: StateFlow<AutomationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allAutomationRules.collect { list ->
                _uiState.value = _uiState.value.copy(rules = list)
            }
        }
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutomationRule(ruleId, isEnabled)
            _uiState.value = _uiState.value.copy(userNotice = "Automation trigger state updated")
        }
    }

    fun saveRule(rule: CommunicationAutomationRuleEntity) {
        viewModelScope.launch {
            repository.saveAutomationRule(rule)
            _uiState.value = _uiState.value.copy(userNotice = "Automation rule saved")
        }
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(userNotice = null)
    }

    class Factory(
        private val repository: CommunicationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AutomationViewModel(repository) as T
        }
    }
}
