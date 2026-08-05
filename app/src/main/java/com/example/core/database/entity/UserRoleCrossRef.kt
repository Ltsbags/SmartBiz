package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_role_cross_ref",
    primaryKeys = ["userId", "roleId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["roleId"])
    ]
)
data class UserRoleCrossRef(
    val userId: String,
    val roleId: String,
    val assignedAt: Long = System.currentTimeMillis(),
    val assignedBy: String = "SYSTEM"
)
