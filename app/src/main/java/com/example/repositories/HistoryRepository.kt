package com.example.repositories

import com.example.core.database.dao.EntityHistoryDao
import com.example.core.database.entity.EntityHistoryEntity
import com.example.features.audit.model.RetentionPolicy
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val entityHistoryDao: EntityHistoryDao
) {

    suspend fun recordHistory(history: EntityHistoryEntity): Long {
        return entityHistoryDao.insertHistory(history)
    }

    fun getAllHistoryFlow(): Flow<List<EntityHistoryEntity>> {
        return entityHistoryDao.getAllHistoryFlow()
    }

    fun getHistoryForEntityFlow(entityName: String, entityId: String): Flow<List<EntityHistoryEntity>> {
        return entityHistoryDao.getHistoryForEntityFlow(entityName, entityId)
    }

    fun getHistoryByEntityNameFlow(entityName: String): Flow<List<EntityHistoryEntity>> {
        return entityHistoryDao.getHistoryByEntityNameFlow(entityName)
    }

    suspend fun executeHistoryRetentionCleanup(policy: RetentionPolicy): Int {
        val threshold = policy.getThresholdTimestamp() ?: return 0
        return entityHistoryDao.deleteHistoryOlderThan(threshold)
    }
}
