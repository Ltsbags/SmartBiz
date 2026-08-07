package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val developer: String,
    val description: String,
    val pluginType: String,
    val status: String, // INSTALLED, ENABLED, DISABLED, ERROR, UNINSTALLED
    val minApiVersion: Int,
    val licenseType: String,
    val licenseKey: String,
    val isLicenseValid: Boolean,
    val licenseExpiryDate: Long = 0L,
    val installedAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "plugin_settings")
data class PluginSettingsEntity(
    @PrimaryKey val id: String,
    val pluginId: String,
    val key: String,
    val value: String,
    val valueType: String = "STRING",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugin_permissions")
data class PluginPermissionEntity(
    @PrimaryKey val id: String,
    val pluginId: String,
    val permissionName: String,
    val isGranted: Boolean,
    val grantedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugin_registry")
data class PluginRegistryEntity(
    @PrimaryKey val pluginId: String,
    val manifestJson: String,
    val entryPointClass: String,
    val isSandboxed: Boolean = true
)
