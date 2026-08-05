package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "login_history",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["timestamp"])
    ]
)
data class LoginHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val action: String, // e.g., SUCCESSFUL_LOGIN, FAILED_LOGIN, LOGOUT, PIN_CHANGED, BIOMETRIC_ENABLED, BIOMETRIC_DISABLED, PROFILE_UPDATED
    val status: String = "SUCCESS", // SUCCESS, FAILURE
    val details: String = "",
    val deviceId: String = "local_device",
    val deviceName: String = "Android Handheld",
    val timestamp: Long = System.currentTimeMillis()
)
