package com.example.services

import android.util.Log
import com.example.core.database.dao.PresenceDao
import com.example.core.database.entity.PresenceEntity
import com.example.core.realtime.EventBus
import com.example.core.realtime.RealtimeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PresenceService(
    private val presenceDao: PresenceDao,
    private val eventBus: EventBus = EventBus.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "PresenceService"

    val allPresenceFlow: Flow<List<PresenceEntity>> = presenceDao.getAllPresenceFlow()
    val onlineUsersFlow: Flow<List<PresenceEntity>> = presenceDao.getOnlineUsersFlow()
    val onlineCountFlow: Flow<Int> = presenceDao.getOnlineUsersCountFlow()

    init {
        // Seed default presence team members for multi-device simulation & initial view
        scope.launch {
            seedDefaultTeamPresence()
        }

        // Listen for remote presence change events from EventBus
        scope.launch {
            eventBus.events.collect { event ->
                if (event is RealtimeEvent.PresenceChanged) {
                    val entity = PresenceEntity(
                        userId = event.userId,
                        userName = event.userName,
                        status = event.newStatus,
                        lastSeenAt = event.timestamp,
                        currentDevice = event.device
                    )
                    presenceDao.insertOrUpdatePresence(entity)
                }
            }
        }
    }

    suspend fun updateCurrentUserStatus(
        userId: String = "USER_PRIMARY",
        userName: String = "Owner Admin",
        newStatus: String = "ONLINE",
        customStatus: String = "Active on POS Terminal",
        device: String = "Android Mobile"
    ) {
        val entity = PresenceEntity(
            userId = userId,
            userName = userName,
            status = newStatus,
            customStatus = customStatus,
            lastSeenAt = System.currentTimeMillis(),
            currentDevice = device
        )
        presenceDao.insertOrUpdatePresence(entity)

        // Publish presence change event
        eventBus.publish(
            RealtimeEvent.PresenceChanged(
                userId = userId,
                userName = userName,
                newStatus = newStatus,
                device = device
            )
        )
        Log.i(TAG, "Updated presence status for $userId to $newStatus ($customStatus)")
    }

    suspend fun markUserOffline(userId: String) {
        presenceDao.updatePresenceStatus(userId, "OFFLINE")
        val presence = presenceDao.getPresenceByUserId(userId)
        eventBus.publish(
            RealtimeEvent.PresenceChanged(
                userId = userId,
                userName = presence?.userName ?: "Team Member",
                newStatus = "OFFLINE",
                device = presence?.currentDevice ?: "Mobile"
            )
        )
    }

    private suspend fun seedDefaultTeamPresence() {
        val currentCount = presenceDao.getPresenceByUserId("USER_PRIMARY")
        if (currentCount == null) {
            val defaultTeam = listOf(
                PresenceEntity(
                    userId = "USER_PRIMARY",
                    userName = "Alex Dev (You)",
                    status = "ONLINE",
                    customStatus = "Managing Inventory",
                    currentDevice = "Pixel 8 Pro (This Device)"
                ),
                PresenceEntity(
                    userId = "USER_002",
                    userName = "Sarah Miller (Cashier)",
                    status = "ONLINE",
                    customStatus = "Billing Terminal 1",
                    currentDevice = "Samsung Tablet A8"
                ),
                PresenceEntity(
                    userId = "USER_003",
                    userName = "David Chen (Branch Manager)",
                    status = "AWAY",
                    customStatus = "In Meeting",
                    currentDevice = "MacBook Pro M3"
                ),
                PresenceEntity(
                    userId = "USER_004",
                    userName = "Maria Garcia (Store Staff)",
                    status = "OFFLINE",
                    customStatus = "Shift Finished",
                    currentDevice = "Android POS Terminal"
                )
            )
            defaultTeam.forEach { presenceDao.insertOrUpdatePresence(it) }
        }
    }
}
