package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "permissions",
    indices = [
        Index(value = ["permissionCode"], unique = true),
        Index(value = ["category"])
    ]
)
data class PermissionEntity(
    @PrimaryKey
    val permissionId: String,
    val permissionCode: String,
    val permissionName: String,
    val category: String,
    val description: String = "",
    val isSensitive: Boolean = false,
    val requiresPinConfirmation: Boolean = false
)
