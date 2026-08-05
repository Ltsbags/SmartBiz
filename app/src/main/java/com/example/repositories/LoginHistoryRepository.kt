package com.example.repositories

import com.example.core.database.dao.LoginHistoryDao
import com.example.core.database.entity.LoginHistoryEntity
import kotlinx.coroutines.flow.Flow

class LoginHistoryRepository(private val loginHistoryDao: LoginHistoryDao) {

    fun getHistoryForUser(userId: String): Flow<List<LoginHistoryEntity>> {
        return loginHistoryDao.getHistoryForUser(userId)
    }

    suspend fun getRecentHistory(userId: String, limit: Int = 20): List<LoginHistoryEntity> {
        return loginHistoryDao.getRecentHistory(userId, limit)
    }

    suspend fun logAction(
        userId: String,
        action: String,
        status: String = "SUCCESS",
        details: String = "",
        deviceId: String = "local_device",
        deviceName: String = "Android Handheld"
    ) {
        val entity = LoginHistoryEntity(
            userId = userId,
            action = action,
            status = status,
            details = details,
            deviceId = deviceId,
            deviceName = deviceName,
            timestamp = System.currentTimeMillis()
        )
        loginHistoryDao.insertHistory(entity)
    }

    suspend fun clearHistory(userId: String) {
        loginHistoryDao.clearHistoryForUser(userId)
    }
}
