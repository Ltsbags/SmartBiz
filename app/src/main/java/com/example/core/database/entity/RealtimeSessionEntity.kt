package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "realtime_sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["businessId"]),
        Index(value = ["connectionState"])
    ]
)
data class RealtimeSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val userName: String,
    val businessId: String,
    val branchId: String,
    val connectionState: String = "CONNECTED", // CONNECTED, DISCONNECTED, RECONNECTING, FAILED
    val transportType: String = "WEBSOCKET", // WEBSOCKET, SSE, LONG_POLLING
    val connectedAt: Long = System.currentTimeMillis(),
    val lastHeartbeatAt: Long = System.currentTimeMillis(),
    val ipAddress: String = "127.0.0.1",
    val deviceInfo: String = "Android Client"
)
