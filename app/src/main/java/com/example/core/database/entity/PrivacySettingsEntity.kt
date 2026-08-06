package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_settings")
data class PrivacySettingsEntity(
    @PrimaryKey
    val userId: String = "DEFAULT_USER",
    val hideFinancialValues: Boolean = false,
    val hideDashboardAmounts: Boolean = false,
    val maskMobileNumbers: Boolean = true,
    val maskGstNumbers: Boolean = true,
    val maskEmailAddresses: Boolean = false,
    val blurSensitiveScreens: Boolean = false,
    val secureClipboard: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
