package com.example.repositories

import com.example.core.database.dao.SecurityPolicyDao
import com.example.core.database.entity.SecurityPolicyEntity
import kotlinx.coroutines.flow.Flow

class SecurityPolicyRepository(
    private val securityPolicyDao: SecurityPolicyDao
) {

    suspend fun savePolicy(policy: SecurityPolicyEntity): Long {
        return securityPolicyDao.insertPolicy(policy)
    }

    suspend fun getPolicyByKey(policyKey: String): SecurityPolicyEntity? {
        return securityPolicyDao.getPolicyByKey(policyKey)
    }

    fun getPolicyByKeyFlow(policyKey: String): Flow<SecurityPolicyEntity?> {
        return securityPolicyDao.getPolicyByKeyFlow(policyKey)
    }

    fun getAllPoliciesFlow(): Flow<List<SecurityPolicyEntity>> {
        return securityPolicyDao.getAllPoliciesFlow()
    }

    suspend fun getAllPoliciesList(): List<SecurityPolicyEntity> {
        return securityPolicyDao.getAllPoliciesList()
    }
}
