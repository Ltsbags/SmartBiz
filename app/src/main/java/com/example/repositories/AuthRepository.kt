package com.example.repositories

import com.example.core.database.dao.SessionDao
import com.example.core.database.dao.UserDao
import com.example.core.database.entity.SessionEntity
import com.example.core.database.entity.UserEntity
import com.example.core.services.SharedPreferencesService
import com.example.core.utils.SecurityUtils
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity, val session: SessionEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class Lockout(val remainingSeconds: Int) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val prefsService: SharedPreferencesService
) {

    suspend fun hasConfiguredUser(): Boolean {
        return userDao.getUserCount() > 0
    }

    suspend fun isAppLockActive(): Boolean {
        if (!hasConfiguredUser()) return false
        if (!prefsService.isAppLockEnabled) return false
        val activeSession = sessionDao.getActiveSession() ?: return false
        return activeSession.sessionStatus == "ACTIVE" && activeSession.expiryTime > System.currentTimeMillis()
    }

    fun getPrimaryUserFlow(): Flow<UserEntity?> {
        return userDao.getPrimaryUserFlow()
    }

    suspend fun getPrimaryUser(): UserEntity? {
        return userDao.getPrimaryUser()
    }

    fun getActiveSessionFlow(): Flow<SessionEntity?> {
        return sessionDao.getActiveSessionFlow()
    }

    suspend fun registerOwner(
        fullName: String,
        businessName: String,
        mobileNumber: String,
        email: String,
        pin: String
    ): AuthResult {
        if (pin.length !in 4..6) {
            return AuthResult.Error("PIN must be 4 to 6 digits")
        }

        val pinHash = SecurityUtils.hashPin(pin)
        val userId = "USR-${System.currentTimeMillis().toString().takeLast(6)}"

        val newUser = UserEntity(
            userId = userId,
            fullName = fullName.ifBlank { "Business Owner" },
            businessName = businessName.ifBlank { "My SmartBiz Store" },
            mobileNumber = mobileNumber.trim(),
            email = email.trim(),
            roleId = "ROLE_OWNER",
            pinHash = pinHash,
            status = "ACTIVE",
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis()
        )

        val insertedId = userDao.insertUser(newUser)

        // Save business settings to preferences for quick access
        if (businessName.isNotBlank()) prefsService.businessName = businessName
        if (mobileNumber.isNotBlank()) prefsService.businessPhone = mobileNumber
        if (email.isNotBlank()) prefsService.businessEmail = email

        // Create initial active session
        val session = createSession(userId)
        val user = newUser.copy(id = insertedId)

        return AuthResult.Success(user, session)
    }

    suspend fun loginWithPin(mobileNumber: String, pin: String): AuthResult {
        // Check lockout
        val now = System.currentTimeMillis()
        val lockoutUntil = prefsService.lockoutUntilTimestamp
        if (lockoutUntil > now) {
            val remainingSecs = ((lockoutUntil - now) / 1000).toInt()
            return AuthResult.Lockout(remainingSecs)
        }

        val user = if (mobileNumber.isNotBlank()) {
            userDao.getUserByMobile(mobileNumber.trim()) ?: userDao.getPrimaryUser()
        } else {
            userDao.getPrimaryUser()
        } ?: return AuthResult.Error("No registered user found on this device.")

        if (!SecurityUtils.verifyPin(pin, user.pinHash)) {
            val attempts = prefsService.failedPinAttempts + 1
            prefsService.failedPinAttempts = attempts

            if (attempts >= 5) {
                val lockoutTime = now + (5 * 60 * 1000) // 5 minutes cooldown
                prefsService.lockoutUntilTimestamp = lockoutTime
                prefsService.failedPinAttempts = 0
                return AuthResult.Lockout(300)
            } else {
                val remainingAttempts = 5 - attempts
                return AuthResult.Error("Invalid PIN. $remainingAttempts attempts remaining.")
            }
        }

        // Reset failed attempts upon successful login
        prefsService.failedPinAttempts = 0
        prefsService.lockoutUntilTimestamp = 0L

        // Update user last login
        userDao.updateLastLogin(user.userId, now)

        // Invalidate old active sessions and create new active session
        sessionDao.invalidateActiveSessions(user.userId)
        val newSession = createSession(user.userId)

        return AuthResult.Success(user, newSession)
    }

    suspend fun loginWithBiometrics(): AuthResult {
        if (!prefsService.isBiometricsEnabled) {
            return AuthResult.Error("Biometric login is disabled in settings.")
        }

        val user = userDao.getPrimaryUser() ?: return AuthResult.Error("No registered user found.")

        sessionDao.invalidateActiveSessions(user.userId)
        val newSession = createSession(user.userId)

        return AuthResult.Success(user, newSession)
    }

    suspend fun logout(): Boolean {
        val activeSession = sessionDao.getActiveSession()
        if (activeSession != null) {
            sessionDao.invalidateActiveSessions(activeSession.userId)
        }
        return true
    }

    suspend fun updateUserPin(userId: String, newPinHash: String): Boolean {
        return try {
            userDao.updatePinHash(userId, newPinHash)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePin(oldPin: String, newPin: String): AuthResult {
        val user = userDao.getPrimaryUser() ?: return AuthResult.Error("User not found.")

        if (!SecurityUtils.verifyPin(oldPin, user.pinHash)) {
            return AuthResult.Error("Incorrect current PIN.")
        }

        if (newPin.length !in 4..6) {
            return AuthResult.Error("New PIN must be 4 to 6 digits.")
        }

        val newPinHash = SecurityUtils.hashPin(newPin)
        userDao.updatePinHash(user.userId, newPinHash)

        return AuthResult.Success(user, sessionDao.getActiveSession() ?: createSession(user.userId))
    }

    fun isBiometricsEnabled(): Boolean = prefsService.isBiometricsEnabled

    fun setBiometricsEnabled(enabled: Boolean) {
        prefsService.isBiometricsEnabled = enabled
    }

    fun isAppLockEnabled(): Boolean = prefsService.isAppLockEnabled

    fun setAppLockEnabled(enabled: Boolean) {
        prefsService.isAppLockEnabled = enabled
    }

    private suspend fun createSession(userId: String): SessionEntity {
        val sessionId = SecurityUtils.generateSecureToken()
        val session = SessionEntity(
            sessionId = sessionId,
            userId = userId,
            loginTime = System.currentTimeMillis(),
            expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
            sessionStatus = "ACTIVE"
        )
        sessionDao.insertSession(session)
        return session
    }
}
