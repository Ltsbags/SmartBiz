package com.example.repositories

import com.example.core.database.dao.SessionPolicyDao
import com.example.core.database.entity.SessionPolicyEntity
import kotlinx.coroutines.flow.Flow

class SessionPolicyRepository(
    private val sessionPolicyDao: SessionPolicyDao
) {
    fun getDefaultSessionPolicyFlow(): Flow<SessionPolicyEntity?> {
        return sessionPolicyDao.getDefaultPolicyFlow()
    }

    suspend fun getDefaultSessionPolicy(): SessionPolicyEntity {
        return sessionPolicyDao.getDefaultPolicy() ?: SessionPolicyEntity()
    }

    suspend fun saveSessionPolicy(policy: SessionPolicyEntity): Long {
        return sessionPolicyDao.insertOrUpdatePolicy(policy)
    }
}
