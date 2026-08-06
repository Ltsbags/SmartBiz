package com.example.services

import com.example.core.database.entity.SessionPolicyEntity
import com.example.repositories.SessionPolicyRepository

class SessionPolicyService(
    private val sessionPolicyRepository: SessionPolicyRepository,
    private val auditService: AuditService
) {

    suspend fun getSessionPolicy(): SessionPolicyEntity {
        return sessionPolicyRepository.getDefaultSessionPolicy()
    }

    suspend fun updateSessionPolicy(policy: SessionPolicyEntity) {
        sessionPolicyRepository.saveSessionPolicy(policy)
        auditService.logAuditEvent(
            userName = "ADMIN",
            module = "SESSION_POLICIES",
            action = "UPDATE_SESSION_POLICY",
            description = "Session policies updated (Timeout: ${policy.sessionTimeoutMinutes}m, Idle: ${policy.idleTimeoutMinutes}m, Max Concurrent: ${policy.maxConcurrentSessions})",
            severity = "INFO"
        )
    }

    suspend fun checkIdleTimeout(lastActivityTimestamp: Long): Boolean {
        val policy = getSessionPolicy()
        val elapsedMinutes = (System.currentTimeMillis() - lastActivityTimestamp) / (1000 * 60)
        val isTimedOut = elapsedMinutes >= policy.idleTimeoutMinutes
        if (isTimedOut && policy.autoLogoutEnabled) {
            auditService.logAuditEvent(
                userName = "USER",
                module = "SESSION_POLICIES",
                action = "SESSION_IDLE_TIMEOUT_VIOLATION",
                description = "User session auto-terminated due to idle timeout ($elapsedMinutes mins >= ${policy.idleTimeoutMinutes} mins)",
                severity = "WARNING"
            )
        }
        return isTimedOut
    }

    suspend fun validateConcurrentSessions(currentActiveCount: Int): Boolean {
        val policy = getSessionPolicy()
        val isExceeded = currentActiveCount > policy.maxConcurrentSessions
        if (isExceeded) {
            auditService.logAuditEvent(
                userName = "USER",
                module = "SESSION_POLICIES",
                action = "CONCURRENT_SESSION_LIMIT_EXCEEDED",
                description = "Active sessions count ($currentActiveCount) exceeded max allowed limit (${policy.maxConcurrentSessions})",
                severity = "WARNING"
            )
        }
        return !isExceeded
    }
}
