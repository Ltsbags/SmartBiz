package com.example.services

import com.example.core.database.entity.SecurityEventEntity
import com.example.core.database.entity.SecurityPolicyEntity
import com.example.repositories.SecurityPolicyRepository
import com.example.repositories.SecurityRepository
import kotlinx.coroutines.flow.Flow

class SecurityService(
    val encryptionService: EncryptionService,
    val secureStorageService: SecureStorageService,
    val integrityService: IntegrityService,
    val appLockService: AppLockService,
    val screenSecurityService: ScreenSecurityService,
    val clipboardSecurityService: ClipboardSecurityService,
    val inputSanitizationService: InputSanitizationService,
    val rootDetectionService: RootDetectionService,
    private val securityRepository: SecurityRepository,
    private val securityPolicyRepository: SecurityPolicyRepository
) {

    suspend fun logSecurityEvent(
        eventType: String,
        description: String,
        severity: String = "INFO",
        module: String = "APP_SECURITY",
        userId: String = "system",
        userName: String = "System User"
    ) {
        val event = SecurityEventEntity(
            eventType = eventType,
            severity = severity,
            description = description,
            module = module,
            userId = userId,
            userName = userName
        )
        securityRepository.recordSecurityEvent(event)
    }

    fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>> {
        return securityRepository.getAllSecurityEventsFlow()
    }

    fun getRecentSecurityEventsFlow(limit: Int = 100): Flow<List<SecurityEventEntity>> {
        return securityRepository.getRecentSecurityEventsFlow(limit)
    }

    fun getAllPoliciesFlow(): Flow<List<SecurityPolicyEntity>> {
        return securityPolicyRepository.getAllPoliciesFlow()
    }

    suspend fun savePolicy(policyKey: String, policyName: String, description: String, isEnabled: Boolean, rulesJson: String) {
        val policy = SecurityPolicyEntity(
            policyKey = policyKey,
            policyName = policyName,
            description = description,
            isEnabled = isEnabled,
            rulesJson = rulesJson,
            updatedAt = System.currentTimeMillis()
        )
        securityPolicyRepository.savePolicy(policy)
        logSecurityEvent(
            eventType = "POLICY_UPDATED",
            description = "Security policy '$policyName' was updated.",
            severity = "WARNING"
        )
    }

    suspend fun initializeDefaultPoliciesIfEmpty() {
        val existing = securityPolicyRepository.getAllPoliciesList()
        if (existing.isEmpty()) {
            savePolicy("PIN_POLICY", "PIN Security Policy", "Requires minimum 4-6 digit numerical PIN with auto-lock", true, "{\"minLength\":4,\"maxLength\":6}")
            savePolicy("SESSION_POLICY", "Session Timeout Policy", "Automatically locks app after inactivity period", true, "{\"timeoutMinutes\":5}")
            savePolicy("BACKUP_POLICY", "Encrypted Backup Policy", "Mandatory AES-256 encryption on all exported database backups", true, "{\"encryptionRequired\":true}")
            savePolicy("SCREEN_POLICY", "Screen Protection Policy", "Prevents screenshots and recents preview exposure", true, "{\"flagSecure\":true}")
            savePolicy("ROOT_POLICY", "Root & Integrity Detection Policy", "Detects su binaries and untrusted custom ROM keys", true, "{\"logEventsOnly\":true}")
        }
    }

    suspend fun performStartupSecurityCheck(): IntegrityService.IntegrityCheckResult {
        val integrityResult = integrityService.verifyDatabaseIntegrity()
        if (!integrityResult.isHealthy) {
            logSecurityEvent(
                eventType = "INTEGRITY_FAILURE",
                description = integrityResult.tableCheckSummary,
                severity = "CRITICAL",
                module = "DB_SECURITY"
            )
        }

        val rootResult = rootDetectionService.checkDeviceRootStatus()
        if (rootResult.isRooted) {
            logSecurityEvent(
                eventType = "ROOT_DETECTED",
                description = "Rooted system environment detected. SU Binary: ${rootResult.isSuBinaryPresent}, Test Keys: ${rootResult.isTestKeysPresent}",
                severity = "WARNING",
                module = "APP_SECURITY"
            )
        }

        return integrityResult
    }
}
