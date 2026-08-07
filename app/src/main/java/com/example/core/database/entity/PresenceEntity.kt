package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "presence",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["businessId"]),
        Index(value = ["lastSeenAt"])
    ]
)
data class PresenceEntity(
    @PrimaryKey
    val userId: String,
    val userName: String,
    val status: String = "ONLINE", // ONLINE, AWAY, BUSY, OFFLINE
    val customStatus: String = "",
    val lastSeenAt: Long = System.currentTimeMillis(),
    val currentDevice: String = "Android Device",
    val businessId: String = "BIZ_001",
    val branchId: String = "BRANCH_MAIN"
)
