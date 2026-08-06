package com.example.features.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CompliancePolicyEntity
import com.example.repositories.AppRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComplianceUiState(
    val isLoading: Boolean = true,
    val policies: List<CompliancePolicyEntity> = emptyList(),
    val selectedFrameworkFilter: String = "ALL", // ALL, INTERNAL, GDPR, ISO27001, SOC2
    val errorMessage: String? = null
)

class ComplianceViewModel : ViewModel() {
    private val appProvider = AppRepositoryProvider.getInstance()
    private val complianceService = appProvider.complianceService
    private val complianceRepository = appProvider.complianceRepository

    private val _uiState = MutableStateFlow(ComplianceUiState())
    val uiState: StateFlow<ComplianceUiState> = _uiState.asStateFlow()

    init {
        loadPolicies()
    }

    fun loadPolicies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                complianceService.evaluateAllComplianceFrameworks()
                complianceRepository.getAllPoliciesFlow().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        policies = list
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun setFrameworkFilter(framework: String) {
        _uiState.value = _uiState.value.copy(selectedFrameworkFilter = framework)
    }

    fun toggleEnforcement(policyId: String, currentEnforced: Boolean) {
        viewModelScope.launch {
            try {
                complianceService.togglePolicyEnforcement(policyId, !currentEnforced)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }
}
