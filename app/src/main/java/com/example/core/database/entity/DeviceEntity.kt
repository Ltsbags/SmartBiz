package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["userId"])
    ]
)
data class DeviceEntity(
    @PrimaryKey
    val deviceId: String,
    val userId: String,
    val deviceName: String,
    val androidVersion: String = "Android 14",
    val appVersion: String = "1.0.0",
    val deviceIdentifier: String = "",
    val lastLoginTime: Long = System.currentTimeMillis(),
    val isTrusted: Boolean = true,
    val registeredDate: Long = System.currentTimeMillis()
)
