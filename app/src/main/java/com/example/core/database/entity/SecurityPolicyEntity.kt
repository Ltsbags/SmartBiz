package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_policies")
data class SecurityPolicyEntity(
    @PrimaryKey
    val policyKey: String,
    val policyName: String,
    val description: String,
    val isEnabled: Boolean = true,
    val rulesJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
