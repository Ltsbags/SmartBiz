package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.CompliancePolicyEntity
import com.example.core.database.entity.DataAccessPolicyEntity
import com.example.core.database.entity.PrivacySettingsEntity
import com.example.core.database.entity.SessionPolicyEntity
import com.example.core.services.SharedPreferencesService
import com.example.repositories.AuditRepository
import com.example.repositories.ComplianceRepository
import com.example.repositories.DataAccessPolicyRepository
import com.example.repositories.PrivacyRepository
import com.example.repositories.SecurityPolicyRepository
import com.example.repositories.SessionPolicyRepository
import com.example.repositories.TrustedDeviceRepository
import com.example.services.AuditService
import com.example.services.ComplianceService
import com.example.services.PolicyEngine
import com.example.services.PolicyEvaluationContext
import com.example.services.PrivacyService
import com.example.services.SessionPolicyService
import com.example.services.TrustedDeviceService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnterpriseIntegrationTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefsService: SharedPreferencesService
    private lateinit var auditRepository: AuditRepository
    private lateinit var auditService: AuditService
    private lateinit var privacyRepository: PrivacyRepository
    private lateinit var sessionPolicyRepository: SessionPolicyRepository
    private lateinit var dataAccessPolicyRepository: DataAccessPolicyRepository
    private lateinit var complianceRepository: ComplianceRepository
    private lateinit var trustedDeviceRepository: TrustedDeviceRepository
    private lateinit var securityPolicyRepository: SecurityPolicyRepository
    private lateinit var policyEngine: PolicyEngine
    private lateinit var privacyService: PrivacyService
    private lateinit var sessionPolicyService: SessionPolicyService
    private lateinit var complianceService: ComplianceService
    private lateinit var trustedDeviceService: TrustedDeviceService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)
        prefsService = SharedPreferencesService(context)

        auditRepository = AuditRepository(dbHelper.auditLogDao, prefsService)
        auditService = AuditService(auditRepository)

        privacyRepository = PrivacyRepository(dbHelper.privacySettingsDao)
        sessionPolicyRepository = SessionPolicyRepository(dbHelper.sessionPolicyDao)
        dataAccessPolicyRepository = DataAccessPolicyRepository(dbHelper.dataAccessPolicyDao)
        complianceRepository = ComplianceRepository(dbHelper.compliancePolicyDao)
        trustedDeviceRepository = TrustedDeviceRepository(dbHelper.deviceDao)
        securityPolicyRepository = SecurityPolicyRepository(dbHelper.securityPolicyDao)

        policyEngine = PolicyEngine(
            securityPolicyRepository = securityPolicyRepository,
            privacyRepository = privacyRepository,
            sessionPolicyRepository = sessionPolicyRepository,
            dataAccessPolicyRepository = dataAccessPolicyRepository,
            complianceRepository = complianceRepository,
            trustedDeviceRepository = trustedDeviceRepository,
            auditService = auditService
        )

        privacyService = PrivacyService(privacyRepository, auditService)
        sessionPolicyService = SessionPolicyService(sessionPolicyRepository, auditService)
        complianceService = ComplianceService(complianceRepository, auditService)
        trustedDeviceService = TrustedDeviceService(trustedDeviceRepository, auditService)
    }

    @Test
    fun testDatabaseHelperInitialization() {
        assertNotNull(dbHelper.privacySettingsDao)
        assertNotNull(dbHelper.sessionPolicyDao)
        assertNotNull(dbHelper.dataAccessPolicyDao)
        assertNotNull(dbHelper.compliancePolicyDao)
        assertNotNull(dbHelper.auditLogDao)
        assertNotNull(dbHelper.deviceDao)
    }

    @Test
    fun testAuditServiceLoggingAndRepository() = runBlocking {
        val auditId = auditService.logAuditEvent(
            module = "INTEGRATION_TEST",
            action = "TEST_AUDIT_LOG",
            description = "Verifying audit log entry creation",
            severity = "INFO",
            userId = "test_user_01"
        )
        assertTrue(auditId > 0)

        val audits = auditRepository.getRecentAuditsFlow(10).first()
        assertTrue(audits.isNotEmpty())
        assertEquals("INTEGRATION_TEST", audits.first().module)
        assertEquals("TEST_AUDIT_LOG", audits.first().action)
    }

    @Test
    fun testDataAccessPolicyEvaluation() = runBlocking {
        val contextAdmin = PolicyEvaluationContext(
            userId = "admin_01",
            roleId = "ADMIN"
        )
        val contextStaff = PolicyEvaluationContext(
            userId = "staff_01",
            roleId = "STAFF"
        )

        val adminExportAllowed = policyEngine.evaluateDataAccess(contextAdmin, "EXPORT")
        assertTrue(adminExportAllowed)

        val staffExportAllowed = policyEngine.evaluateDataAccess(contextStaff, "EXPORT")
        assertFalse(staffExportAllowed)
    }

    @Test
    fun testSecurityScoreCalculation() = runBlocking {
        val scoreReport = policyEngine.calculateSecurityScore("test_user_score")
        assertNotNull(scoreReport)
        assertTrue(scoreReport.totalScore in 0..100)
        assertNotNull(scoreReport.level)
        assertTrue(scoreReport.factors.isNotEmpty())
    }

    @Test
    fun testPrivacySettingsServiceAndMasking() = runBlocking {
        val defaultPrivacy = privacyRepository.getPrivacySettings("user_privacy_test")
        assertTrue(defaultPrivacy.maskMobileNumbers)

        val maskedMobile = privacyService.maskMobileNumber("+919876543210", "user_privacy_test")
        assertEquals("+91******3210", maskedMobile)

        val updated = privacyService.updatePrivacySettings(
            defaultPrivacy.copy(maskMobileNumbers = false),
            "user_privacy_test"
        )
        assertFalse(updated.maskMobileNumbers)

        val unmaskedMobile = privacyService.maskMobileNumber("+919876543210", "user_privacy_test")
        assertEquals("+919876543210", unmaskedMobile)
    }

    @Test
    fun testComplianceFrameworkService() = runBlocking {
        val policies = complianceService.evaluateAllComplianceFrameworks()
        assertTrue(policies.isNotEmpty())

        val targetPolicy = policies.first()
        complianceService.togglePolicyEnforcement(targetPolicy.policyId, false)

        val updatedPolicies = complianceService.evaluateAllComplianceFrameworks()
        val updated = updatedPolicies.first { it.policyId == targetPolicy.policyId }
        assertFalse(updated.isEnforced)
        assertEquals("PENDING_REVIEW", updated.status)
    }
}
