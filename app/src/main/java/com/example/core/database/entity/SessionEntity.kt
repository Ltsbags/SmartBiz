package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["sessionStatus"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val loginTime: Long = System.currentTimeMillis(),
    val expiryTime: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days default
    val deviceId: String = "local_device",
    val deviceName: String = "Android Handheld",
    val appVersion: String = "1.0.0",
    val sessionStatus: String = "ACTIVE",
    val authToken: String = "",
    val refreshToken: String = ""
)
