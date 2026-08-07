package com.example.services.plugin

import com.example.core.database.dao.PluginDao
import com.example.core.database.entity.PluginEntity
import com.example.core.database.entity.PluginRegistryEntity
import com.example.core.database.entity.PluginSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PluginManagerService(
    private val pluginDao: PluginDao,
    private val permissionService: PluginPermissionService,
    private val licenseService: PluginLicenseService,
    private val pluginLoader: PluginLoader = PluginLoader(),
    private val sdk: SmartBizPluginSdk = SmartBizPluginSdk.getInstance()
) {
    val currentPlatformApiVersion = 1

    val allPluginsFlow: Flow<List<PluginEntity>> get() = pluginDao.getAllPlugins()
    val enabledPluginsFlow: Flow<List<PluginEntity>> get() = pluginDao.getEnabledPlugins()

    suspend fun installPlugin(manifest: PluginManifest): Boolean {
        val existing = pluginDao.getPluginById(manifest.pluginId)
        if (existing != null && existing.status != "UNINSTALLED") {
            return false // Already installed
        }

        val pluginEntity = PluginEntity(
            id = manifest.pluginId,
            name = manifest.pluginName,
            version = manifest.version,
            developer = manifest.developer,
            description = manifest.description,
            pluginType = manifest.pluginType.name,
            status = "INSTALLED",
            minApiVersion = manifest.requiredApiVersion,
            licenseType = manifest.license.licenseType,
            licenseKey = manifest.license.licenseKey,
            isLicenseValid = manifest.license.isValid,
            installedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        pluginDao.insertPlugin(pluginEntity)

        val registryEntity = PluginRegistryEntity(
            pluginId = manifest.pluginId,
            manifestJson = "{\"id\":\"${manifest.pluginId}\",\"version\":\"${manifest.version}\"}",
            entryPointClass = manifest.entryPointClass,
            isSandboxed = true
        )
        pluginDao.insertRegistryEntry(registryEntity)

        // Request permissions
        permissionService.requestPermissions(manifest.pluginId, manifest.permissions)

        // Invoke lifecycle hook
        val instance = pluginLoader.getPluginInstance(manifest.pluginId)
        instance?.onInstall(sdk)

        return true
    }

    suspend fun enablePlugin(pluginId: String): Boolean {
        val plugin = pluginDao.getPluginById(pluginId) ?: return false

        if (plugin.minApiVersion > currentPlatformApiVersion) {
            pluginDao.updatePluginStatus(pluginId, "ERROR")
            return false // Incompatible API version
        }

        // Validate License
        val licenseResult = licenseService.validateLicense(pluginId, plugin.licenseKey, plugin.licenseType)
        if (!licenseResult.isValid) {
            pluginDao.updatePluginStatus(pluginId, "DISABLED")
            return false
        }

        // Sync permissions
        permissionService.syncGrantedPermissionsWithSdk(pluginId)

        // Update DB state
        pluginDao.updatePluginStatus(pluginId, "ENABLED")

        // Invoke plugin hook
        val instance = pluginLoader.getPluginInstance(pluginId)
        instance?.onEnable(sdk)

        return true
    }

    suspend fun disablePlugin(pluginId: String): Boolean {
        val plugin = pluginDao.getPluginById(pluginId) ?: return false

        pluginDao.updatePluginStatus(pluginId, "DISABLED")

        // Invoke plugin hook
        val instance = pluginLoader.getPluginInstance(pluginId)
        instance?.onDisable(sdk)

        // Unregister SDK resources
        sdk.unregisterPluginResources(pluginId)

        return true
    }

    suspend fun updatePlugin(pluginId: String, newVersion: String): Boolean {
        val plugin = pluginDao.getPluginById(pluginId) ?: return false
        val oldVersion = plugin.version

        val updated = plugin.copy(
            version = newVersion,
            status = if (plugin.status == "ENABLED") "ENABLED" else "INSTALLED",
            updatedAt = System.currentTimeMillis()
        )
        pluginDao.updatePlugin(updated)

        val instance = pluginLoader.getPluginInstance(pluginId)
        instance?.onUpdate(oldVersion, newVersion, sdk)

        return true
    }

    suspend fun rollbackPlugin(pluginId: String, targetVersion: String): Boolean {
        val plugin = pluginDao.getPluginById(pluginId) ?: return false
        val updated = plugin.copy(
            version = targetVersion,
            updatedAt = System.currentTimeMillis()
        )
        pluginDao.updatePlugin(updated)
        return true
    }

    suspend fun uninstallPlugin(pluginId: String): Boolean {
        val instance = pluginLoader.getPluginInstance(pluginId)
        instance?.onUninstall(sdk)

        sdk.unregisterPluginResources(pluginId)

        pluginDao.deleteSettingsForPlugin(pluginId)
        pluginDao.deletePermissionsForPlugin(pluginId)
        pluginDao.deleteRegistryEntry(pluginId)
        pluginDao.deletePlugin(pluginId)

        return true
    }

    suspend fun setSetting(pluginId: String, key: String, value: String, valueType: String = "STRING") {
        val entity = PluginSettingsEntity(
            id = "${pluginId}_${key}",
            pluginId = pluginId,
            key = key,
            value = value,
            valueType = valueType,
            updatedAt = System.currentTimeMillis()
        )
        pluginDao.insertOrUpdateSetting(entity)
    }

    fun getSettings(pluginId: String): Flow<List<PluginSettingsEntity>> {
        return pluginDao.getSettingsForPlugin(pluginId)
    }

    suspend fun initializeActivePlugins() {
        val enabledPlugins = pluginDao.getEnabledPlugins().first()
        for (plugin in enabledPlugins) {
            enablePlugin(plugin.id)
        }
    }
}
