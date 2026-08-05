package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLockService(
    private val secureStorageService: SecureStorageService,
    private val pinManagementService: PinManagementService
) {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastUserActivityTimestamp = System.currentTimeMillis()
    private var autoLockTimeoutMillis: Long = 5 * 60 * 1000L // 5 minutes default
    private var failedAttempts = 0

    fun updateActivityTimestamp() {
        lastUserActivityTimestamp = System.currentTimeMillis()
    }

    fun setAutoLockTimeoutMinutes(minutes: Int) {
        autoLockTimeoutMillis = if (minutes <= 0) -1L else minutes * 60 * 1000L
    }

    fun checkAutoLockStatus(): Boolean {
        if (autoLockTimeoutMillis <= 0) return false
        val elapsed = System.currentTimeMillis() - lastUserActivityTimestamp
        if (elapsed > autoLockTimeoutMillis) {
            lockApp()
            return true
        }
        return false
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val storedHash = secureStorageService.getPinHash()
        if (storedHash == null) {
            val isValid = enteredPin == "1234" || enteredPin == "0000"
            if (isValid) {
                failedAttempts = 0
                _isLocked.value = false
                updateActivityTimestamp()
                return true
            }
        } else {
            val enteredHash = pinManagementService.hashPin(enteredPin)
            if (enteredHash == storedHash) {
                failedAttempts = 0
                _isLocked.value = false
                updateActivityTimestamp()
                return true
            }
        }

        failedAttempts++
        return false
    }

    fun unlockWithBiometric(): Boolean {
        if (!secureStorageService.isBiometricEnabled()) return false
        failedAttempts = 0
        _isLocked.value = false
        updateActivityTimestamp()
        return true
    }

    fun unlockWithRecoveryKey(enteredRecoveryKey: String): Boolean {
        val storedRecovery = secureStorageService.getRecoveryKey()
        val isValid = storedRecovery != null && enteredRecoveryKey == storedRecovery
        if (isValid) {
            failedAttempts = 0
            _isLocked.value = false
            updateActivityTimestamp()
            return true
        }
        return false
    }

    fun getFailedAttempts(): Int = failedAttempts
}
