package com.example.repositories

import com.example.core.database.dao.CommunicationDao
import com.example.core.database.entity.CommunicationLogEntity
import com.example.core.database.entity.CommunicationMessageEntity
import kotlinx.coroutines.flow.Flow

class DeliveryRepository(
    private val communicationDao: CommunicationDao
) {
    val allMessages: Flow<List<CommunicationMessageEntity>> = communicationDao.getAllMessages()

    fun getLogsForMessage(messageId: Long): Flow<List<CommunicationLogEntity>> {
        return communicationDao.getLogsForMessage(messageId)
    }

    suspend fun getPendingDispatchMessages(): List<CommunicationMessageEntity> {
        return communicationDao.getPendingDispatchMessages()
    }

    suspend fun updateMessageStatus(id: Long, status: String, details: String) {
        communicationDao.updateMessageStatus(id, status, details)
    }

    suspend fun addLog(messageId: Long, eventType: String, details: String): Long {
        return communicationDao.insertLog(
            CommunicationLogEntity(
                messageId = messageId,
                eventType = eventType,
                details = details
            )
        )
    }
}
