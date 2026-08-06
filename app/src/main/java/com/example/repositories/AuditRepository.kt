package com.example.repositories

import com.example.core.database.dao.AuditLogDao
import com.example.core.database.entity.AuditLogEntity
import com.example.core.services.SharedPreferencesService
import com.example.features.audit.model.AuditFilterState
import com.example.features.audit.model.RetentionPolicy
import kotlinx.coroutines.flow.Flow

class AuditRepository(
    private val auditLogDao: AuditLogDao,
    private val prefsService: SharedPreferencesService
) {
    companion object {
        const val KEY_RETENTION_POLICY = "audit_retention_policy"
        const val KEY_AUTO_RETENTION_ENABLED = "auto_retention_enabled"
    }

    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditsFlow()

    suspend fun recordAudit(audit: AuditLogEntity): Long {
        return auditLogDao.insertAudit(audit)
    }

    suspend fun recordAudits(audits: List<AuditLogEntity>) {
        auditLogDao.insertAudits(audits)
    }

    fun getAllAuditsFlow(): Flow<List<AuditLogEntity>> {
        return auditLogDao.getAllAuditsFlow()
    }

    fun getRecentAuditsFlow(limit: Int = 100): Flow<List<AuditLogEntity>> {
        return auditLogDao.getRecentAuditsFlow(limit)
    }

    fun getFilteredAuditsFlow(filterState: AuditFilterState): Flow<List<AuditLogEntity>> {
        return auditLogDao.getFilteredAuditsFlow(
            module = filterState.selectedModule,
            severity = filterState.selectedSeverity,
            userId = filterState.selectedUserId,
            businessId = filterState.selectedBusinessId,
            branchId = filterState.selectedBranchId,
            action = filterState.selectedAction,
            startTime = filterState.startTime,
            endTime = filterState.endTime,
            searchQuery = if (filterState.searchQuery.isBlank()) null else filterState.searchQuery
        )
    }

    fun getAuditsByModuleFlow(module: String): Flow<List<AuditLogEntity>> {
        return auditLogDao.getAuditsByModuleFlow(module)
    }

    fun getAuditsBySeverityFlow(severity: String): Flow<List<AuditLogEntity>> {
        return auditLogDao.getAuditsBySeverityFlow(severity)
    }

    fun getAuditsForEntityFlow(entityName: String, entityId: String): Flow<List<AuditLogEntity>> {
        return auditLogDao.getAuditsForEntityFlow(entityName, entityId)
    }

    fun getAuditCountFlow(): Flow<Int> {
        return auditLogDao.getAuditCountFlow()
    }

    fun getCriticalAuditCountFlow(): Flow<Int> {
        return auditLogDao.getCriticalAuditCountFlow()
    }

    fun getRetentionPolicy(): RetentionPolicy {
        val label = prefsService.getString(KEY_RETENTION_POLICY, RetentionPolicy.NINETY_DAYS.label)
        return RetentionPolicy.fromLabel(label)
    }

    fun setRetentionPolicy(policy: RetentionPolicy) {
        prefsService.saveString(KEY_RETENTION_POLICY, policy.label)
    }

    fun isAutoRetentionEnabled(): Boolean {
        return prefsService.getBoolean(KEY_AUTO_RETENTION_ENABLED, false)
    }

    fun setAutoRetentionEnabled(enabled: Boolean) {
        prefsService.saveBoolean(KEY_AUTO_RETENTION_ENABLED, enabled)
    }

    suspend fun executeRetentionCleanup(): Int {
        val policy = getRetentionPolicy()
        val threshold = policy.getThresholdTimestamp() ?: return 0
        return auditLogDao.deleteAuditsOlderThan(threshold)
    }
}
