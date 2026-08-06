package com.example.features.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.SessionPolicyEntity
import com.example.repositories.AppRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionPolicyUiState(
    val isLoading: Boolean = true,
    val policy: SessionPolicyEntity = SessionPolicyEntity(),
    val isSuccessMessageVisible: Boolean = false,
    val errorMessage: String? = null
)

class SessionPolicyViewModel : ViewModel() {
    private val appProvider = AppRepositoryProvider.getInstance()
    private val sessionPolicyService = appProvider.sessionPolicyService
    private val sessionPolicyRepository = appProvider.sessionPolicyRepository

    private val _uiState = MutableStateFlow(SessionPolicyUiState())
    val uiState: StateFlow<SessionPolicyUiState> = _uiState.asStateFlow()

    init {
        loadPolicy()
    }

    fun loadPolicy() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                sessionPolicyRepository.getDefaultSessionPolicyFlow().collect { policy ->
                    val current = policy ?: SessionPolicyEntity()
                    _uiState.value = SessionPolicyUiState(
                        isLoading = false,
                        policy = current
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

    fun updatePolicy(updatedPolicy: SessionPolicyEntity) {
        viewModelScope.launch {
            try {
                sessionPolicyService.updateSessionPolicy(updatedPolicy)
                _uiState.value = _uiState.value.copy(
                    policy = updatedPolicy,
                    isSuccessMessageVisible = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(isSuccessMessageVisible = false)
    }
}
