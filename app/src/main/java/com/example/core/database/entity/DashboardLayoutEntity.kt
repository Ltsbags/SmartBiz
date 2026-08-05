package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "dashboard_layouts",
    indices = [
        Index("id"),
        Index("userId"),
        Index("businessId"),
        Index("branchId")
    ]
)
data class DashboardLayoutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val layoutName: String = "Default Command Center Layout",
    val userId: String = "default_user",
    val businessId: String = "default_biz",
    val branchId: String = "main_branch",
    val isDefault: Boolean = true,
    val widgetPositionsJson: String = "[]",
    val updatedDate: Long = System.currentTimeMillis()
)
