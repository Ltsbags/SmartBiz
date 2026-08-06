package com.example.services

import com.example.core.database.entity.DataAccessPolicyEntity
import com.example.core.database.entity.PrivacySettingsEntity
import com.example.core.database.entity.SecurityPolicyEntity
import com.example.core.database.entity.SessionPolicyEntity
import com.example.repositories.ComplianceRepository
import com.example.repositories.DataAccessPolicyRepository
import com.example.repositories.PrivacyRepository
import com.example.repositories.SecurityPolicyRepository
import com.example.repositories.SessionPolicyRepository
import com.example.repositories.TrustedDeviceRepository

data class PolicyEvaluationContext(
    val userId: String = "DEFAULT_USER",
    val roleId: String = "ADMIN",
    val permissions: List<String> = emptyList(),
    val businessId: String = "MAIN_BIZ",
    val branchId: String = "HEADQUARTERS",
    val deviceId: String = "LOCAL_DEVICE",
    val isTrustedDevice: Boolean = true
)

data class SecurityScoreReport(
    val totalScore: Int,
    val level: String, // CRITICAL, WEAK, GOOD, ENTERPRISE_EXCELLENT
    val factors: List<String>,
    val recommendations: List<String>
)

class PolicyEngine(
    private val securityPolicyRepository: SecurityPolicyRepository,
    private val privacyRepository: PrivacyRepository,
    private val sessionPolicyRepository: SessionPolicyRepository,
    private val dataAccessPolicyRepository: DataAccessPolicyRepository,
    private val complianceRepository: ComplianceRepository,
    private val trustedDeviceRepository: TrustedDeviceRepository,
    private val auditService: AuditService
) {

    /**
     * Dynamically evaluates whether an operation is allowed based on Policy Context, Data Access Policy, and RBAC.
     */
    suspend fun evaluateDataAccess(
        context: PolicyEvaluationContext,
        action: String // EXPORT, BACKUP, RESTORE, SCREENSHOT, PRINT, SHARE_PDF
    ): Boolean {
        val policy = dataAccessPolicyRepository.getPolicyForRole(context.roleId)
        val isAllowed = when (action.uppercase()) {
            "EXPORT" -> policy.allowExport
            "BACKUP" -> policy.allowBackup
            "RESTORE" -> policy.allowRestore
            "SCREENSHOT" -> policy.allowScreenshot
            "PRINT" -> policy.allowPrinting
            "SHARE_PDF" -> policy.allowPdfSharing
            else -> false
        }

        if (!isAllowed) {
            auditService.logAuditEvent(
                userName = context.userId,
                module = "SECURITY_POLICY",
                action = "ACCESS_DENIED",
                description = "Data access violation for action '$action' under role '${context.roleId}'",
                severity = "WARNING"
            )
        }
        return isAllowed
    }

    /**
     * Dynamically computes the enterprise security score (0 to 100) based on configured policies, trusted devices, session limits, and privacy settings.
     */
    suspend fun calculateSecurityScore(userId: String = "DEFAULT_USER"): SecurityScoreReport {
        var score = 0
        val factors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // 1. Session Policy Evaluation (Max 25 pts)
        val sessionPolicy = sessionPolicyRepository.getDefaultSessionPolicy()
        if (sessionPolicy.enableAppLock) {
            score += 10
            factors.add("App Lock Enabled (+10)")
        } else {
            recommendations.add("Enable App Lock to prevent unauthorized access.")
        }

        if (sessionPolicy.requireBiometric || sessionPolicy.requirePin) {
            score += 10
            factors.add("Biometric/PIN Authentication Required (+10)")
        }

        if (sessionPolicy.idleTimeoutMinutes in 1..15) {
            score += 5
            factors.add("Strict Idle Timeout (${sessionPolicy.idleTimeoutMinutes} mins) (+5)")
        }

        // 2. Privacy Settings Evaluation (Max 25 pts)
        val privacy = privacyRepository.getPrivacySettings(userId)
        if (privacy.maskMobileNumbers) {
            score += 5
            factors.add("Mobile Masking (+5)")
        }
        if (privacy.maskGstNumbers) {
            score += 5
            factors.add("GST Masking (+5)")
        }
        if (privacy.secureClipboard) {
            score += 5
            factors.add("Clipboard Clearing Enabled (+5)")
        }
        if (privacy.hideFinancialValues) {
            score += 5
            factors.add("Financial Privacy Protection (+5)")
        }
        if (privacy.blurSensitiveScreens) {
            score += 5
            factors.add("Background Screen Blurring (+5)")
        }

        // 3. Trusted Devices (Max 25 pts)
        val devices = trustedDeviceRepository.getDeviceById("LOCAL_DEVICE")
        if (devices?.isTrusted == true) {
            score += 25
            factors.add("Primary Device Verified & Trusted (+25)")
        } else {
            recommendations.add("Verify and trust your primary active mobile device.")
        }

        // 4. Compliance & Policy Enforcements (Max 25 pts)
        val complianceList = complianceRepository.getAllPoliciesList()
        val enforcedCount = complianceList.count { it.isEnforced && it.status == "COMPLIANT" }
        if (complianceList.isNotEmpty()) {
            val complianceScore = ((enforcedCount.toDouble() / complianceList.size) * 25).toInt()
            score += complianceScore
            factors.add("Compliance Alignment ($enforcedCount/${complianceList.size}) (+$complianceScore)")
        } else {
            score += 15
            factors.add("Default Internal Security Policies Active (+15)")
        }

        val level = when {
            score >= 85 -> "ENTERPRISE_EXCELLENT"
            score >= 65 -> "GOOD"
            score >= 45 -> "WEAK"
            else -> "CRITICAL"
        }

        return SecurityScoreReport(
            totalScore = score.coerceAtMost(100),
            level = level,
            factors = factors,
            recommendations = recommendations
        )
    }

    /**
     * Architecture hook for remote policy synchronization with future Node.js / enterprise server.
     */
    suspend fun syncRemotePolicies(serverUrl: String, authToken: String): Boolean {
        // Architecture hook for remote policy sync without modifying business logic
        auditService.logAuditEvent(
            userName = "SYSTEM",
            module = "SECURITY_POLICY",
            action = "REMOTE_POLICY_SYNC",
            description = "Attempted remote policy synchronization with endpoint: $serverUrl",
            severity = "INFO"
        )
        return true
    }
}
