package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_access_policies")
data class DataAccessPolicyEntity(
    @PrimaryKey
    val policyId: String = "DEFAULT_DATA_ACCESS",
    val roleId: String = "ALL",
    val allowExport: Boolean = true,
    val allowBackup: Boolean = true,
    val allowRestore: Boolean = false,
    val allowScreenshot: Boolean = false,
    val allowPrinting: Boolean = true,
    val allowPdfSharing: Boolean = true,
    val rulesJson: String = "{}",
    val updatedAt: Long = System.currentTimeMillis()
)
