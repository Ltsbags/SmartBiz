package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_policies")
data class SessionPolicyEntity(
    @PrimaryKey
    val policyId: String = "DEFAULT_SESSION_POLICY",
    val sessionTimeoutMinutes: Int = 30,
    val idleTimeoutMinutes: Int = 15,
    val maxConcurrentSessions: Int = 3,
    val rememberDeviceDays: Int = 30,
    val autoLogoutEnabled: Boolean = true,
    val forceReauthForSensitiveOps: Boolean = true,
    val enableAppLock: Boolean = true,
    val requireBiometric: Boolean = false,
    val requirePin: Boolean = true,
    val lockOnBackground: Boolean = true,
    val lockAfterIdleMinutes: Int = 5,
    val lockAfterRestart: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
