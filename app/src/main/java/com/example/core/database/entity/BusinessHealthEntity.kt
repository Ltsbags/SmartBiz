package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "business_health",
    indices = [
        Index("id"),
        Index("calculatedDate")
    ]
)
data class BusinessHealthEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val overallScore: Int, // 0 to 100
    val revenueScore: Int, // 0 to 100
    val cashFlowScore: Int, // 0 to 100
    val inventoryScore: Int, // 0 to 100
    val securityScore: Int, // 0 to 100
    val backupScore: Int, // 0 to 100
    val statusColor: String = "GREEN", // GREEN, YELLOW, AMBER, RED
    val recommendationsJson: String = "[]", // List of actionable rule-based tips
    val calculatedDate: Long = System.currentTimeMillis()
)
