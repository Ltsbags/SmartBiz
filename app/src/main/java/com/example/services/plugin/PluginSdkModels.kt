package com.example.services.plugin

enum class PluginType {
    REPORTS,
    PAYMENTS,
    COMMUNICATION,
    AI_ASSISTANT,
    WORKFLOW,
    INVENTORY,
    SHIPPING,
    ACCOUNTING,
    CRM,
    AI,
    OTHER
}

enum class PluginPermissionType(val key: String) {
    ACCESS_SALES("access_sales"),
    ACCESS_PURCHASES("access_purchases"),
    ACCESS_REPORTS("access_reports"),
    ACCESS_PAYMENTS("access_payments"),
    ACCESS_AI("access_ai"),
    ACCESS_COMMUNICATIONS("access_communications"),
    ACCESS_COMMUNICATION("access_communication"),
    ACCESS_CUSTOMERS("access_customers"),
    ACCESS_INVENTORY("access_inventory"),
    SEND_NOTIFICATIONS("send_notifications"),
    NETWORK_REQUESTS("network_requests"),
    STORAGE_READ_WRITE("storage_read_write");

    val title: String get() = key.replace("_", " ").uppercase()
}

data class PluginLicenseInfo(
    val licenseType: String, // FREE, TRIAL, PAID
    val licenseKey: String = "",
    val isValid: Boolean = true,
    val licenseExpiryDate: Long = 0L
)

data class PluginManifest(
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val developer: String,
    val description: String,
    val pluginType: PluginType,
    val requiredApiVersion: Int = 1,
    val permissions: List<PluginPermissionType> = emptyList(),
    val license: PluginLicenseInfo = PluginLicenseInfo("FREE", "", true),
    val entryPointClass: String = ""
)

data class RegisteredNavigationItem(
    val pluginId: String,
    val title: String,
    val route: String,
    val iconName: String = "Extension",
    val order: Int = 100
)

data class RegisteredUiWidget(
    val pluginId: String,
    val widgetId: String,
    val title: String,
    val description: String = "",
    val targetScreen: String = "DASHBOARD"
)

data class PluginCommandResult(
    val success: Boolean,
    val message: String,
    val dataJson: String = "{}"
)

data class PluginEvent(
    val eventName: String,
    val pluginId: String,
    val payloadJson: String = "{}"
)

interface SmartBizPlugin {
    val manifest: PluginManifest
    suspend fun onInstall(sdk: SmartBizPluginSdk) {}
    suspend fun onEnable(sdk: SmartBizPluginSdk) {}
    suspend fun onDisable(sdk: SmartBizPluginSdk) {}
    suspend fun onUpdate(oldVersion: String, newVersion: String, sdk: SmartBizPluginSdk) {}
    suspend fun onUninstall(sdk: SmartBizPluginSdk) {}
}
