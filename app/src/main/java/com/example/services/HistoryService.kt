package com.example.services

import com.example.core.database.entity.EntityHistoryEntity
import com.example.core.utils.EntityDiffHelper
import com.example.repositories.HistoryRepository
import com.example.repositories.UserRepository

class HistoryService(
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository? = null
) {

    suspend fun recordEntityChange(
        entityName: String,
        entityId: String,
        action: String, // CREATED, UPDATED, DELETED, RESTORED, ADJUSTED
        oldObj: Any? = null,
        newObj: Any? = null,
        userId: String? = null,
        userName: String? = null
    ): Long {
        var activeUserId = userId ?: "system"
        var activeUserName = userName ?: "System User"

        if (userId == null && userRepository != null) {
            val user = userRepository.getPrimaryUser()
            if (user != null) {
                activeUserId = user.userId
                activeUserName = user.fullName.ifBlank { user.displayName.ifBlank { user.mobileNumber } }
            }
        }

        val diff = EntityDiffHelper.compareObjects(oldObj, newObj)

        val history = EntityHistoryEntity(
            entityName = entityName,
            entityId = entityId,
            action = action,
            oldValueJson = diff.oldValueJson,
            newValueJson = diff.newValueJson,
            modifiedFieldsJson = diff.modifiedFieldsJson,
            userId = activeUserId,
            userName = activeUserName,
            timestamp = System.currentTimeMillis()
        )

        return historyRepository.recordHistory(history)
    }
}
