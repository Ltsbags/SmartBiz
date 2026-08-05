package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "role_permission_cross_ref",
    primaryKeys = ["roleId", "permissionId"],
    indices = [
        Index(value = ["roleId"]),
        Index(value = ["permissionId"])
    ]
)
data class RolePermissionCrossRef(
    val roleId: String,
    val permissionId: String
)
