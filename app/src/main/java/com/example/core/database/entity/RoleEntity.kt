package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey
    val roleId: String,
    val roleName: String,
    val roleCode: String,
    val description: String = "",
    val isSystemRole: Boolean = true,
    val isCustomRole: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
