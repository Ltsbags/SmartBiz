package com.example.features.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DeviceEntity
import com.example.repositories.AppRepositoryProvider
import com.example.services.PolicyEvaluationContext
import com.example.services.SecurityScoreReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SecurityDashboardUiState(
    val isLoading: Boolean = true,
    val securityScore: SecurityScoreReport = SecurityScoreReport(0, "LOADING", emptyList(), emptyList()),
    val trustedDevicesCount: Int = 0,
    val totalDevicesCount: Int = 0,
    val policyViolationsCount: Int = 0,
    val activeSessionStatus: String = "ACTIVE_SECURE",
    val complianceStatus: String = "COMPLIANT",
    val errorMessage: String? = null
)

class SecurityDashboardViewModel : ViewModel() {
    private val appProvider = AppRepositoryProvider.getInstance()
    private val policyEngine = appProvider.policyEngine
    private val trustedDeviceRepository = appProvider.trustedDeviceRepository
    private val auditRepository = appProvider.auditRepository

    private val _uiState = MutableStateFlow(SecurityDashboardUiState())
    val uiState: StateFlow<SecurityDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val scoreReport = policyEngine.calculateSecurityScore()
                
                trustedDeviceRepository.getAllDevicesFlow().collect { devices ->
                    val trustedCount = devices.count { it.isTrusted }
                    val totalCount = devices.size
                    
                    val auditLogs = auditRepository.getRecentAuditsFlow(50).first()
                    val violations = auditLogs.count { it.action.contains("VIOLATION") || it.action.contains("DENIED") }

                    _uiState.value = SecurityDashboardUiState(
                        isLoading = false,
                        securityScore = scoreReport,
                        trustedDevicesCount = trustedCount,
                        totalDevicesCount = totalCount,
                        policyViolationsCount = violations,
                        activeSessionStatus = "ACTIVE_PROTECTED",
                        complianceStatus = "COMPLIANT_100"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load security dashboard"
                )
            }
        }
    }
}
