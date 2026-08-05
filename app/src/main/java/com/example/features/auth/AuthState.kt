package com.example.features.auth

import com.example.core.database.entity.SessionEntity
import com.example.core.database.entity.UserEntity

data class AuthState(
    val isCheckingSession: Boolean = true,
    val isConfigured: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: UserEntity? = null,
    val currentSession: SessionEntity? = null,
    val pinInput: String = "",
    val confirmPinInput: String = "",
    val fullNameInput: String = "",
    val businessNameInput: String = "",
    val mobileInput: String = "",
    val emailInput: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val lockoutRemainingSeconds: Int = 0,
    val isBiometricsAvailable: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val activeTab: AuthTab = AuthTab.PIN_LOGIN
)

enum class AuthTab {
    PIN_LOGIN,
    REGISTER_OWNER,
    SECURITY_SETTINGS
}
