package com.example.repositories

import com.example.core.database.dao.CompliancePolicyDao
import com.example.core.database.entity.CompliancePolicyEntity
import kotlinx.coroutines.flow.Flow

class ComplianceRepository(
    private val compliancePolicyDao: CompliancePolicyDao
) {
    fun getAllPoliciesFlow(): Flow<List<CompliancePolicyEntity>> {
        return compliancePolicyDao.getAllPoliciesFlow()
    }

    fun getPoliciesByFrameworkFlow(framework: String): Flow<List<CompliancePolicyEntity>> {
        return compliancePolicyDao.getPoliciesByFrameworkFlow(framework)
    }

    suspend fun getAllPoliciesList(): List<CompliancePolicyEntity> {
        return compliancePolicyDao.getAllPoliciesList()
    }

    suspend fun savePolicy(policy: CompliancePolicyEntity): Long {
        return compliancePolicyDao.insertOrUpdatePolicy(policy)
    }

    suspend fun updateStatus(policyId: String, status: String) {
        compliancePolicyDao.updatePolicyStatus(policyId, status)
    }
}
