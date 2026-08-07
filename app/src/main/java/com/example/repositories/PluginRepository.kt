package com.example.repositories

import com.example.core.database.dao.PluginDao
import com.example.core.database.entity.PluginEntity
import com.example.core.database.entity.PluginPermissionEntity
import com.example.core.database.entity.PluginSettingsEntity
import com.example.services.plugin.LicenseValidationResult
import com.example.services.plugin.MarketplacePluginItem
import com.example.services.plugin.PluginCommandResult
import com.example.services.plugin.PluginEvent
import com.example.services.plugin.PluginLicenseService
import com.example.services.plugin.PluginManagerService
import com.example.services.plugin.PluginMarketplaceService
import com.example.services.plugin.PluginPermissionService
import com.example.services.plugin.PluginPermissionType
import com.example.services.plugin.RegisteredNavigationItem
import com.example.services.plugin.RegisteredUiWidget
import com.example.services.plugin.SmartBizPluginSdk
import kotlinx.coroutines.flow.Flow

class PluginRepository(
    private val pluginDao: PluginDao,
    val managerService: PluginManagerService,
    val permissionService: PluginPermissionService,
    val licenseService: PluginLicenseService,
    val marketplaceService: PluginMarketplaceService = PluginMarketplaceService(),
    val sdk: SmartBizPluginSdk = SmartBizPluginSdk.getInstance()
) {

    val installedPlugins: Flow<List<PluginEntity>> = managerService.allPluginsFlow
    val enabledPlugins: Flow<List<PluginEntity>> = managerService.enabledPluginsFlow

    suspend fun getPluginById(id: String): PluginEntity? {
        return pluginDao.getPluginById(id)
    }

    suspend fun installPluginFromMarketplace(marketplaceItem: MarketplacePluginItem): Boolean {
        return managerService.installPlugin(marketplaceItem.manifest)
    }

    suspend fun enablePlugin(pluginId: String): Boolean {
        return managerService.enablePlugin(pluginId)
    }

    suspend fun disablePlugin(pluginId: String): Boolean {
        return managerService.disablePlugin(pluginId)
    }

    suspend fun uninstallPlugin(pluginId: String): Boolean {
        return managerService.uninstallPlugin(pluginId)
    }

    suspend fun updatePlugin(pluginId: String, newVersion: String): Boolean {
        return managerService.updatePlugin(pluginId, newVersion)
    }

    suspend fun setPermissionGranted(pluginId: String, permissionKey: String, granted: Boolean) {
        permissionService.setPermissionGranted(pluginId, permissionKey, granted)
    }

    fun getPermissionsForPlugin(pluginId: String): Flow<List<PluginPermissionEntity>> {
        return pluginDao.getPermissionsForPlugin(pluginId)
    }

    suspend fun activateLicense(pluginId: String, licenseKey: String, licenseType: String): Boolean {
        return licenseService.activateLicense(pluginId, licenseKey, licenseType)
    }

    suspend fun validateLicense(pluginId: String, licenseKey: String, licenseType: String): LicenseValidationResult {
        return licenseService.validateLicense(pluginId, licenseKey, licenseType)
    }

    fun getMarketplaceCatalog(category: String? = null, searchQuery: String? = null): List<MarketplacePluginItem> {
        return marketplaceService.getAvailablePlugins(category, searchQuery)
    }

    fun getFeaturedPlugins(): List<MarketplacePluginItem> {
        return marketplaceService.getFeaturedPlugins()
    }

    suspend fun publishEvent(event: PluginEvent) {
        sdk.publishEvent(event)
    }

    suspend fun executePluginCommand(pluginId: String, commandName: String, argsJson: String): PluginCommandResult {
        return sdk.executeCommand(pluginId, commandName, argsJson)
    }

    fun getRegisteredNavItems(): List<RegisteredNavigationItem> {
        return sdk.getRegisteredNavigationItems()
    }

    fun getRegisteredWidgets(targetScreen: String): List<RegisteredUiWidget> {
        return sdk.getRegisteredUiWidgets(targetScreen)
    }

    fun getPluginSettings(pluginId: String): Flow<List<PluginSettingsEntity>> {
        return managerService.getSettings(pluginId)
    }

    suspend fun updatePluginSetting(pluginId: String, key: String, value: String) {
        managerService.setSetting(pluginId, key, value)
    }
}
