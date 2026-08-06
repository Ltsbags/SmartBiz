package com.example.repositories

import com.example.core.database.dao.DataAccessPolicyDao
import com.example.core.database.entity.DataAccessPolicyEntity
import kotlinx.coroutines.flow.Flow

class DataAccessPolicyRepository(
    private val dataAccessPolicyDao: DataAccessPolicyDao
) {
    fun getAllPoliciesFlow(): Flow<List<DataAccessPolicyEntity>> {
        return dataAccessPolicyDao.getAllPoliciesFlow()
    }

    suspend fun getPolicyForRole(roleId: String): DataAccessPolicyEntity {
        return dataAccessPolicyDao.getPolicyForRole(roleId) ?: DataAccessPolicyEntity()
    }

    suspend fun savePolicy(policy: DataAccessPolicyEntity): Long {
        return dataAccessPolicyDao.insertOrUpdatePolicy(policy)
    }
}
