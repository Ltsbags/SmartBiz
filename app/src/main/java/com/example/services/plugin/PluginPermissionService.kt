package com.example.services.plugin

import com.example.core.database.dao.PluginDao
import com.example.core.database.entity.PluginPermissionEntity

class PluginPermissionService(
    private val pluginDao: PluginDao,
    private val sdk: SmartBizPluginSdk = SmartBizPluginSdk.getInstance()
) {

    suspend fun requestPermissions(
        pluginId: String,
        requestedPermissions: List<PluginPermissionType>
    ): List<PluginPermissionEntity> {
        val results = mutableListOf<PluginPermissionEntity>()
        for (perm in requestedPermissions) {
            val existing = pluginDao.getPluginPermission(pluginId, perm.key)
            if (existing == null) {
                val entity = PluginPermissionEntity(
                    id = "${pluginId}_${perm.key}",
                    pluginId = pluginId,
                    permissionName = perm.key,
                    isGranted = false
                )
                pluginDao.insertOrUpdatePermission(entity)
                results.add(entity)
            } else {
                results.add(existing)
            }
        }
        syncGrantedPermissionsWithSdk(pluginId)
        return results
    }

    suspend fun setPermissionGranted(pluginId: String, permissionKey: String, granted: Boolean) {
        val existing = pluginDao.getPluginPermission(pluginId, permissionKey)
        val updated = if (existing != null) {
            existing.copy(isGranted = granted, grantedAt = System.currentTimeMillis())
        } else {
            PluginPermissionEntity(
                id = "${pluginId}_${permissionKey}",
                pluginId = pluginId,
                permissionName = permissionKey,
                isGranted = granted,
                grantedAt = System.currentTimeMillis()
            )
        }
        pluginDao.insertOrUpdatePermission(updated)
        syncGrantedPermissionsWithSdk(pluginId)
    }

    suspend fun syncGrantedPermissionsWithSdk(pluginId: String) {
        val granted = pluginDao.getGrantedPermissionsForPlugin(pluginId)
        val grantedKeys = granted.map { it.permissionName }.toSet()
        sdk.updateGrantedPermissions(pluginId, grantedKeys)
    }

    suspend fun isPermissionGranted(pluginId: String, permission: PluginPermissionType): Boolean {
        val entity = pluginDao.getPluginPermission(pluginId, permission.key)
        return entity?.isGranted == true
    }

    fun isSandboxedAccessAllowed(pluginId: String, requiredPermission: PluginPermissionType): Boolean {
        return sdk.checkPermission(pluginId, requiredPermission)
    }
}
