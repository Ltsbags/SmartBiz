package com.example.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.utils.BiometricAuthHelper
import com.example.repositories.AuthRepository
import com.example.repositories.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        checkSession()
        checkBiometricsAvailability()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingSession = true, errorMessage = null) }
            val configured = authRepository.hasConfiguredUser()
            val user = authRepository.getPrimaryUser()
            val isAppLockActive = authRepository.isAppLockActive()
            val bioEnabled = authRepository.isBiometricsEnabled()
            val lockEnabled = authRepository.isAppLockEnabled()

            _uiState.update {
                it.copy(
                    isCheckingSession = false,
                    isConfigured = configured,
                    isAuthenticated = if (!configured) false else isAppLockActive,
                    currentUser = user,
                    isBiometricsEnabled = bioEnabled,
                    isAppLockEnabled = lockEnabled,
                    activeTab = if (!configured) AuthTab.REGISTER_OWNER else AuthTab.PIN_LOGIN,
                    mobileInput = user?.mobileNumber ?: "",
                    fullNameInput = user?.fullName ?: "",
                    businessNameInput = user?.businessName ?: "",
                    emailInput = user?.email ?: ""
                )
            }
        }
    }

    private fun checkBiometricsAvailability() {
        val available = BiometricAuthHelper.isBiometricAvailable(context)
        _uiState.update { it.copy(isBiometricsAvailable = available) }
    }

    fun onPinDigitEntered(digit: Char) {
        val currentPin = _uiState.value.pinInput
        if (currentPin.length < 6) {
            val updatedPin = currentPin + digit
            _uiState.update { it.copy(pinInput = updatedPin, errorMessage = null) }

            // Auto-submit on 4 or 6 digits if on login tab
            if (_uiState.value.activeTab == AuthTab.PIN_LOGIN && (updatedPin.length == 4 || updatedPin.length == 6)) {
                submitLoginPin(updatedPin)
            }
        }
    }

    fun onConfirmPinDigitEntered(digit: Char) {
        val currentPin = _uiState.value.confirmPinInput
        if (currentPin.length < 6) {
            _uiState.update { it.copy(confirmPinInput = currentPin + digit, errorMessage = null) }
        }
    }

    fun onPinBackspace() {
        val currentPin = _uiState.value.pinInput
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(pinInput = currentPin.dropLast(1), errorMessage = null) }
        }
    }

    fun onConfirmPinBackspace() {
        val currentPin = _uiState.value.confirmPinInput
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(confirmPinInput = currentPin.dropLast(1), errorMessage = null) }
        }
    }

    fun onPinCleared() {
        _uiState.update { it.copy(pinInput = "", confirmPinInput = "", errorMessage = null) }
    }

    fun updateInputFields(
        fullName: String? = null,
        businessName: String? = null,
        mobile: String? = null,
        email: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                fullNameInput = fullName ?: state.fullNameInput,
                businessNameInput = businessName ?: state.businessNameInput,
                mobileInput = mobile ?: state.mobileInput,
                emailInput = email ?: state.emailInput,
                errorMessage = null
            )
        }
    }

    fun switchTab(tab: AuthTab) {
        _uiState.update {
            it.copy(
                activeTab = tab,
                pinInput = "",
                confirmPinInput = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun submitLoginPin(pinToValidate: String = _uiState.value.pinInput) {
        if (pinToValidate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your security PIN") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithPin(_uiState.value.mobileInput, pinToValidate)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = result.user,
                            currentSession = result.session,
                            pinInput = "",
                            errorMessage = null,
                            successMessage = "Authentication successful"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pinInput = "",
                            errorMessage = result.message
                        )
                    }
                }
                is AuthResult.Lockout -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pinInput = "",
                            lockoutRemainingSeconds = result.remainingSeconds,
                            errorMessage = "Security Lockout: Too many failed attempts. Try again in ${result.remainingSeconds / 60} min ${result.remainingSeconds % 60} sec."
                        )
                    }
                }
            }
        }
    }

    fun submitRegisterOwner() {
        val state = _uiState.value
        if (state.fullNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your Full Name") }
            return
        }
        if (state.mobileInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Mobile Number") }
            return
        }
        if (state.pinInput.length !in 4..6) {
            _uiState.update { it.copy(errorMessage = "Security PIN must be 4 to 6 digits") }
            return
        }
        if (state.pinInput != state.confirmPinInput) {
            _uiState.update { it.copy(errorMessage = "PINs do not match. Please re-enter.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.registerOwner(
                fullName = state.fullNameInput,
                businessName = state.businessNameInput,
                mobileNumber = state.mobileInput,
                email = state.emailInput,
                pin = state.pinInput
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigured = true,
                            isAuthenticated = true,
                            currentUser = result.user,
                            currentSession = result.session,
                            pinInput = "",
                            confirmPinInput = "",
                            successMessage = "Owner account registered securely"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is AuthResult.Lockout -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "System locked") }
                }
            }
        }
    }

    fun onBiometricsAuthenticated() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithBiometrics()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = result.user,
                            currentSession = result.session,
                            successMessage = "Biometrics unlocked"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    pinInput = "",
                    confirmPinInput = "",
                    activeTab = AuthTab.PIN_LOGIN,
                    successMessage = "Locked session successfully"
                )
            }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        authRepository.setBiometricsEnabled(enabled)
        _uiState.update { it.copy(isBiometricsEnabled = enabled) }
    }

    fun toggleAppLock(enabled: Boolean) {
        authRepository.setAppLockEnabled(enabled)
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository, context) as T
        }
    }
}
