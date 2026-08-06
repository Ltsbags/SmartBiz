package com.example.services

import com.example.core.database.entity.CompliancePolicyEntity
import com.example.repositories.ComplianceRepository

class ComplianceService(
    private val complianceRepository: ComplianceRepository,
    private val auditService: AuditService
) {

    suspend fun evaluateAllComplianceFrameworks(): List<CompliancePolicyEntity> {
        val policies = complianceRepository.getAllPoliciesList()
        if (policies.isEmpty()) {
            seedDefaultCompliancePolicies()
            return complianceRepository.getAllPoliciesList()
        }
        return policies
    }

    suspend fun seedDefaultCompliancePolicies() {
        val defaults = listOf(
            CompliancePolicyEntity(
                policyId = "COMP_INTERNAL_01",
                framework = "INTERNAL",
                title = "Internal Data Encryption & Access Control Policy",
                description = "Requires AES-256 database encryption, strict biometric/PIN authentication, and audit logging of all sensitive transactions.",
                isEnforced = true,
                status = "COMPLIANT"
            ),
            CompliancePolicyEntity(
                policyId = "COMP_GDPR_01",
                framework = "GDPR",
                title = "GDPR Data Privacy & Right to Erasure Framework",
                description = "Provides user data anonymization, consent management, data masking for PII, and export mechanisms.",
                isEnforced = true,
                status = "COMPLIANT"
            ),
            CompliancePolicyEntity(
                policyId = "COMP_ISO27001_01",
                framework = "ISO 27001",
                title = "ISO/IEC 27001 Information Security Controls",
                description = "Mandates role-based access control (RBAC), multi-device trust verification, and session timeout enforcement.",
                isEnforced = true,
                status = "COMPLIANT"
            ),
            CompliancePolicyEntity(
                policyId = "COMP_SOC2_01",
                framework = "SOC 2",
                title = "SOC 2 Trust Services Criteria (Security & Confidentiality)",
                description = "Monitors real-time security events, automated threat detection, policy violation auditing, and encrypted local storage.",
                isEnforced = true,
                status = "COMPLIANT"
            )
        )
        defaults.forEach { complianceRepository.savePolicy(it) }
    }

    suspend fun togglePolicyEnforcement(policyId: String, isEnforced: Boolean) {
        val existing = complianceRepository.getAllPoliciesList().firstOrNull { it.policyId == policyId }
        if (existing != null) {
            val updated = existing.copy(
                isEnforced = isEnforced,
                status = if (isEnforced) "COMPLIANT" else "PENDING_REVIEW",
                lastEvaluatedAt = System.currentTimeMillis()
            )
            complianceRepository.savePolicy(updated)
            auditService.logAuditEvent(
                userName = "ADMIN",
                module = "COMPLIANCE_MANAGEMENT",
                action = "TOGGLE_COMPLIANCE_ENFORCEMENT",
                description = "Compliance policy '${existing.title}' enforcement set to $isEnforced",
                severity = "INFO"
            )
        }
    }
}
