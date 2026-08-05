package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudits(audits: List<AuditLogEntity>)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditsFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditsFlow(limit: Int = 100): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE module = :module ORDER BY timestamp DESC")
    fun getAuditsByModuleFlow(module: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE severity = :severity ORDER BY timestamp DESC")
    fun getAuditsBySeverityFlow(severity: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAuditsByUserFlow(userId: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entityName = :entityName AND entityId = :entityId ORDER BY timestamp DESC")
    fun getAuditsForEntityFlow(entityName: String, entityId: String): Flow<List<AuditLogEntity>>

    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:module IS NULL OR module = :module)
        AND (:severity IS NULL OR severity = :severity)
        AND (:userId IS NULL OR userId = :userId)
        AND (:businessId IS NULL OR businessId = :businessId)
        AND (:branchId IS NULL OR branchId = :branchId)
        AND (:action IS NULL OR action = :action)
        AND (:startTime IS NULL OR timestamp >= :startTime)
        AND (:endTime IS NULL OR timestamp <= :endTime)
        AND (:searchQuery IS NULL OR description LIKE '%' || :searchQuery || '%' OR entityName LIKE '%' || :searchQuery || '%' OR userName LIKE '%' || :searchQuery || '%' OR action LIKE '%' || :searchQuery || '%')
        ORDER BY timestamp DESC
    """)
    fun getFilteredAuditsFlow(
        module: String? = null,
        severity: String? = null,
        userId: String? = null,
        businessId: String? = null,
        branchId: String? = null,
        action: String? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        searchQuery: String? = null
    ): Flow<List<AuditLogEntity>>

    @Query("SELECT COUNT(*) FROM audit_logs")
    fun getAuditCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM audit_logs WHERE severity = 'CRITICAL'")
    fun getCriticalAuditCountFlow(): Flow<Int>

    @Query("DELETE FROM audit_logs WHERE timestamp < :thresholdTimestamp")
    suspend fun deleteAuditsOlderThan(thresholdTimestamp: Long): Int
}
