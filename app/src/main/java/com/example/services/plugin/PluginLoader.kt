package com.example.services.plugin

import com.example.services.plugin.builtin.AdvancedReportsPlugin
import com.example.services.plugin.builtin.AiCopilotExtensionPlugin
import com.example.services.plugin.builtin.RazorpayGatewayPlugin
import com.example.services.plugin.builtin.WhatsAppCommunicationPlugin
import java.util.concurrent.ConcurrentHashMap

class PluginLoader {

    private val pluginRegistry = ConcurrentHashMap<String, SmartBizPlugin>()

    init {
        // Register known built-in extensions
        registerPluginInstance(AdvancedReportsPlugin())
        registerPluginInstance(RazorpayGatewayPlugin())
        registerPluginInstance(WhatsAppCommunicationPlugin())
        registerPluginInstance(AiCopilotExtensionPlugin())
    }

    fun registerPluginInstance(plugin: SmartBizPlugin) {
        pluginRegistry[plugin.manifest.pluginId] = plugin
    }

    fun getPluginInstance(pluginId: String): SmartBizPlugin? {
        return pluginRegistry[pluginId]
    }

    fun getAllKnownPlugins(): List<SmartBizPlugin> {
        return pluginRegistry.values.toList()
    }
}
