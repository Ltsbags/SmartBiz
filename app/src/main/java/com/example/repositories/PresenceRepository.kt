package com.example.repositories

import com.example.core.database.entity.PresenceEntity
import com.example.services.PresenceService
import kotlinx.coroutines.flow.Flow

class PresenceRepository(
    private val presenceService: PresenceService
) {
    val allPresenceFlow: Flow<List<PresenceEntity>> = presenceService.allPresenceFlow
    val onlineUsersFlow: Flow<List<PresenceEntity>> = presenceService.onlineUsersFlow
    val onlineCountFlow: Flow<Int> = presenceService.onlineCountFlow

    suspend fun updatePresence(
        userId: String = "USER_PRIMARY",
        userName: String = "Owner Admin",
        newStatus: String = "ONLINE",
        customStatus: String = "Active",
        device: String = "Android Mobile"
    ) {
        presenceService.updateCurrentUserStatus(userId, userName, newStatus, customStatus, device)
    }

    suspend fun setOffline(userId: String) {
        presenceService.markUserOffline(userId)
    }
}
