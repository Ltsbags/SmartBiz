package com.example.services

import com.example.core.database.dao.LoginHistoryDao
import com.example.core.database.entity.LoginHistoryEntity

class SessionHistoryService(private val loginHistoryDao: LoginHistoryDao) {

    suspend fun recordAuditLog(
        userId: String,
        action: String,
        status: String = "SUCCESS",
        details: String = "",
        deviceId: String = "local_device",
        deviceName: String = "Android Handheld"
    ) {
        val entry = LoginHistoryEntity(
            userId = userId,
            action = action,
            status = status,
            details = details,
            deviceId = deviceId,
            deviceName = deviceName,
            timestamp = System.currentTimeMillis()
        )
        loginHistoryDao.insertHistory(entry)
    }
}
