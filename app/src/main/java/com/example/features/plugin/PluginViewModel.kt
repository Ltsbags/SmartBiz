package com.example.features.plugin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PluginEntity
import com.example.core.database.entity.PluginPermissionEntity
import com.example.repositories.PluginRepository
import com.example.services.plugin.MarketplacePluginItem
import com.example.services.plugin.RegisteredNavigationItem
import com.example.services.plugin.RegisteredUiWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PluginUiState(
    val selectedTab: Int = 0, // 0 = Marketplace, 1 = Installed, 2 = SDK Resources
    val searchQuery: String = "",
    val selectedCategory: String = "ALL",
    val installedPlugins: List<PluginEntity> = emptyList(),
    val marketplaceCatalog: List<MarketplacePluginItem> = emptyList(),
    val selectedMarketplaceItem: MarketplacePluginItem? = null,
    val selectedInstalledPlugin: PluginEntity? = null,
    val permissionsForSelectedPlugin: List<PluginPermissionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val commandResultOutput: String? = null,
    val registeredNavItems: List<RegisteredNavigationItem> = emptyList(),
    val registeredWidgets: List<RegisteredUiWidget> = emptyList()
)

class PluginViewModel(
    private val pluginRepository: PluginRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PluginUiState())
    val uiState: StateFlow<PluginUiState> = _uiState.asStateFlow()

    init {
        observeInstalledPlugins()
        refreshMarketplaceCatalog()
        refreshSdkResources()
    }

    private fun observeInstalledPlugins() {
        viewModelScope.launch {
            pluginRepository.installedPlugins.collectLatest { plugins ->
                _uiState.update { it.copy(installedPlugins = plugins) }
                refreshSdkResources()
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        refreshSdkResources()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshMarketplaceCatalog()
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        refreshMarketplaceCatalog()
    }

    fun selectMarketplaceItem(item: MarketplacePluginItem?) {
        _uiState.update { it.copy(selectedMarketplaceItem = item) }
    }

    fun selectInstalledPlugin(plugin: PluginEntity?) {
        _uiState.update { it.copy(selectedInstalledPlugin = plugin) }
        if (plugin != null) {
            loadPermissionsForPlugin(plugin.id)
        }
    }

    private fun loadPermissionsForPlugin(pluginId: String) {
        viewModelScope.launch {
            pluginRepository.getPermissionsForPlugin(pluginId).collectLatest { permissions ->
                _uiState.update { it.copy(permissionsForSelectedPlugin = permissions) }
            }
        }
    }

    fun refreshMarketplaceCatalog() {
        val category = _uiState.value.selectedCategory
        val search = _uiState.value.searchQuery
        val catalog = pluginRepository.getMarketplaceCatalog(category, search)
        _uiState.update { it.copy(marketplaceCatalog = catalog) }
    }

    fun installPlugin(item: MarketplacePluginItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = pluginRepository.installPluginFromMarketplace(item)
            val msg = if (success) "Extension ${item.manifest.pluginName} installed successfully" else "Installation failed or already installed"
            _uiState.update { it.copy(isLoading = false, userMessage = msg) }
            refreshSdkResources()
        }
    }

    fun togglePluginStatus(plugin: PluginEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = if (plugin.status == "ENABLED") {
                pluginRepository.disablePlugin(plugin.id)
            } else {
                pluginRepository.enablePlugin(plugin.id)
            }
            val statusAction = if (plugin.status == "ENABLED") "disabled" else "enabled"
            val msg = if (success) "Plugin ${plugin.name} $statusAction" else "Failed to update plugin state (check license/API version)"
            _uiState.update { it.copy(isLoading = false, userMessage = msg) }
            refreshSdkResources()
        }
    }

    fun uninstallPlugin(pluginId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = pluginRepository.uninstallPlugin(pluginId)
            val msg = if (success) "Plugin uninstalled successfully" else "Failed to uninstall plugin"
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userMessage = msg,
                    selectedInstalledPlugin = if (it.selectedInstalledPlugin?.id == pluginId) null else it.selectedInstalledPlugin
                )
            }
            refreshSdkResources()
        }
    }

    fun updatePermission(pluginId: String, permissionKey: String, isGranted: Boolean) {
        viewModelScope.launch {
            pluginRepository.setPermissionGranted(pluginId, permissionKey, isGranted)
            loadPermissionsForPlugin(pluginId)
            _uiState.update { it.copy(userMessage = "Updated permission $permissionKey -> $isGranted") }
        }
    }

    fun activateLicense(pluginId: String, licenseKey: String, licenseType: String) {
        viewModelScope.launch {
            val success = pluginRepository.activateLicense(pluginId, licenseKey, licenseType)
            val msg = if (success) "License activated successfully" else "License verification failed"
            _uiState.update { it.copy(userMessage = msg) }
        }
    }

    fun executePluginCommand(pluginId: String, commandName: String, argsJson: String = "{}") {
        viewModelScope.launch {
            val result = pluginRepository.executePluginCommand(pluginId, commandName, argsJson)
            _uiState.update {
                it.copy(commandResultOutput = "[$commandName] Result: ${result.message}\nData: ${result.dataJson}")
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun refreshSdkResources() {
        val navItems = pluginRepository.getRegisteredNavItems()
        val widgets = pluginRepository.getRegisteredWidgets("DASHBOARD") + pluginRepository.getRegisteredWidgets("INVOICE_DETAIL")
        _uiState.update {
            it.copy(
                registeredNavItems = navItems,
                registeredWidgets = widgets
            )
        }
    }

    class Factory(private val repository: PluginRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PluginViewModel(repository) as T
        }
    }
}
