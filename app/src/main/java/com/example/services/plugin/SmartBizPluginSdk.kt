package com.example.services.plugin

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class SmartBizPluginSdk private constructor() {

    private val navigationItems = CopyOnWriteArrayList<RegisteredNavigationItem>()
    private val uiWidgets = CopyOnWriteArrayList<RegisteredUiWidget>()
    private val commandHandlers = ConcurrentHashMap<String, suspend (String) -> PluginCommandResult>()
    private val eventListeners = ConcurrentHashMap<String, CopyOnWriteArrayList<suspend (PluginEvent) -> Unit>>()
    private val grantedPermissions = ConcurrentHashMap<String, Set<String>>()

    fun registerNavigationItem(item: RegisteredNavigationItem) {
        navigationItems.removeIf { it.pluginId == item.pluginId && it.route == item.route }
        navigationItems.add(item)
    }

    fun registerUiWidget(widget: RegisteredUiWidget) {
        uiWidgets.removeIf { it.pluginId == widget.pluginId && it.widgetId == widget.widgetId }
        uiWidgets.add(widget)
    }

    fun registerCommand(pluginId: String, commandName: String, handler: suspend (String) -> PluginCommandResult) {
        commandHandlers["${pluginId}_${commandName}"] = handler
    }

    suspend fun executeCommand(pluginId: String, commandName: String, argsJson: String): PluginCommandResult {
        val handler = commandHandlers["${pluginId}_${commandName}"]
            ?: return PluginCommandResult(false, "Command $commandName not found for plugin $pluginId")
        return handler(argsJson)
    }

    fun subscribe(eventName: String, listener: suspend (PluginEvent) -> Unit) {
        eventListeners.getOrPut(eventName) { CopyOnWriteArrayList() }.add(listener)
    }

    suspend fun publishEvent(event: PluginEvent) {
        val listeners = eventListeners[event.eventName] ?: return
        listeners.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                // Log and swallow event handler failure to sandbox plugins
            }
        }
    }

    fun updateGrantedPermissions(pluginId: String, grantedKeys: Set<String>) {
        grantedPermissions[pluginId] = grantedKeys
    }

    fun checkPermission(pluginId: String, permission: PluginPermissionType): Boolean {
        return grantedPermissions[pluginId]?.contains(permission.key) == true
    }

    fun getRegisteredNavigationItems(): List<RegisteredNavigationItem> {
        return navigationItems.sortedBy { it.order }
    }

    fun getRegisteredUiWidgets(targetScreen: String): List<RegisteredUiWidget> {
        return uiWidgets.filter { it.targetScreen.equals(targetScreen, ignoreCase = true) }
    }

    fun unregisterPluginResources(pluginId: String) {
        navigationItems.removeIf { it.pluginId == pluginId }
        uiWidgets.removeIf { it.pluginId == pluginId }
        commandHandlers.keys.filter { it.startsWith("${pluginId}_") }.forEach { commandHandlers.remove(it) }
        grantedPermissions.remove(pluginId)
    }

    companion object {
        @Volatile
        private var instance: SmartBizPluginSdk? = null

        fun getInstance(): SmartBizPluginSdk {
            return instance ?: synchronized(this) {
                instance ?: SmartBizPluginSdk().also { instance = it }
            }
        }
    }
}
