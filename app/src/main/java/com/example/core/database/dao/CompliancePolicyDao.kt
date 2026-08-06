package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.CompliancePolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompliancePolicyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: CompliancePolicyEntity): Long

    @Query("SELECT * FROM compliance_policies WHERE policyId = :policyId LIMIT 1")
    suspend fun getPolicyById(policyId: String): CompliancePolicyEntity?

    @Query("SELECT * FROM compliance_policies WHERE framework = :framework")
    fun getPoliciesByFrameworkFlow(framework: String): Flow<List<CompliancePolicyEntity>>

    @Query("SELECT * FROM compliance_policies")
    fun getAllPoliciesFlow(): Flow<List<CompliancePolicyEntity>>

    @Query("SELECT * FROM compliance_policies")
    suspend fun getAllPoliciesList(): List<CompliancePolicyEntity>

    @Query("UPDATE compliance_policies SET status = :status, lastEvaluatedAt = :evaluatedAt WHERE policyId = :policyId")
    suspend fun updatePolicyStatus(policyId: String, status: String, evaluatedAt: Long = System.currentTimeMillis())
}
