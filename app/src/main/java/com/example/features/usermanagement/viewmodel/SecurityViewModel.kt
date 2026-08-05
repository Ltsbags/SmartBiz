package com.example.features.usermanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.UserEntity
import com.example.core.services.SharedPreferencesService
import com.example.repositories.AuthRepository
import com.example.repositories.UserRepository
import com.example.services.PinManagementService
import com.example.services.SessionHistoryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val currentUser: UserEntity? = null,
    val isBiometricsEnabled: Boolean = false,
    val isRememberDeviceEnabled: Boolean = true,
    val isAppLockEnabled: Boolean = true,
    val recoveryEmail: String = "",
    val securityQuestionsConfigured: Boolean = false,
    // Change PIN state
    val currentPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val pinErrorMessage: String? = null,
    val pinSuccessMessage: String? = null,
    val isChangingPin: Boolean = false
)

class SecurityViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val pinService: PinManagementService,
    private val prefsService: SharedPreferencesService,
    private val sessionHistoryService: SessionHistoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        loadSecuritySettings()
    }

    fun loadSecuritySettings() {
        viewModelScope.launch {
            userRepository.getPrimaryUserFlow().collect { user ->
                if (user != null) {
                    _uiState.update { state ->
                        state.copy(
                            currentUser = user,
                            recoveryEmail = user.email,
                            isBiometricsEnabled = prefsService.getEncrypted("biometric_enabled", "false") == "true",
                            isRememberDeviceEnabled = prefsService.getEncrypted("remember_device", "true") == "true",
                            isAppLockEnabled = prefsService.getEncrypted("app_lock", "true") == "true"
                        )
                    }
                }
            }
        }
    }

    fun updatePinInput(current: String? = null, new: String? = null, confirm: String? = null) {
        _uiState.update { state ->
            state.copy(
                currentPin = current ?: state.currentPin,
                newPin = new ?: state.newPin,
                confirmPin = confirm ?: state.confirmPin,
                pinErrorMessage = null,
                pinSuccessMessage = null
            )
        }
    }

    fun changePin() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (state.currentPin.isBlank()) {
            _uiState.update { it.copy(pinErrorMessage = "Current PIN is required") }
            return
        }

        if (!pinService.verifyPin(state.currentPin, user.pinHash)) {
            _uiState.update { it.copy(pinErrorMessage = "Current PIN is incorrect") }
            return
        }

        if (state.newPin != state.confirmPin) {
            _uiState.update { it.copy(pinErrorMessage = "New PIN and Confirm PIN do not match") }
            return
        }

        val (isValid, strengthMsg) = pinService.validateNewPin(state.newPin)
        if (!isValid) {
            _uiState.update { it.copy(pinErrorMessage = strengthMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPin = true, pinErrorMessage = null) }
            val newHash = pinService.hashPin(state.newPin)
            val success = authRepository.updateUserPin(user.userId, newHash)

            if (success) {
                sessionHistoryService.recordAuditLog(
                    userId = user.userId,
                    action = "PIN_CHANGED",
                    details = "Security PIN changed successfully"
                )
                _uiState.update {
                    it.copy(
                        isChangingPin = false,
                        currentPin = "",
                        newPin = "",
                        confirmPin = "",
                        pinSuccessMessage = "Security PIN updated successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isChangingPin = false, pinErrorMessage = "Failed to update PIN in local database")
                }
            }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        val user = _uiState.value.currentUser ?: return
        prefsService.saveEncrypted("biometric_enabled", enabled.toString())
        _uiState.update { it.copy(isBiometricsEnabled = enabled) }

        viewModelScope.launch {
            sessionHistoryService.recordAuditLog(
                userId = user.userId,
                action = if (enabled) "BIOMETRIC_ENABLED" else "BIOMETRIC_DISABLED",
                details = if (enabled) "Biometric authentication enabled" else "Biometric authentication disabled"
            )
        }
    }

    fun toggleRememberDevice(enabled: Boolean) {
        prefsService.saveEncrypted("remember_device", enabled.toString())
        _uiState.update { it.copy(isRememberDeviceEnabled = enabled) }
    }

    fun toggleAppLock(enabled: Boolean) {
        prefsService.saveEncrypted("app_lock", enabled.toString())
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
    }

    fun clearPinMessages() {
        _uiState.update { it.copy(pinErrorMessage = null, pinSuccessMessage = null) }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val authRepository: AuthRepository,
        private val pinService: PinManagementService,
        private val prefsService: SharedPreferencesService,
        private val sessionHistoryService: SessionHistoryService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SecurityViewModel(userRepository, authRepository, pinService, prefsService, sessionHistoryService) as T
        }
    }
}
