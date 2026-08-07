package com.example.services.plugin.builtin

import com.example.services.plugin.PluginCommandResult
import com.example.services.plugin.PluginEvent
import com.example.services.plugin.PluginLicenseInfo
import com.example.services.plugin.PluginManifest
import com.example.services.plugin.PluginPermissionType
import com.example.services.plugin.PluginType
import com.example.services.plugin.RegisteredNavigationItem
import com.example.services.plugin.RegisteredUiWidget
import com.example.services.plugin.SmartBizPlugin
import com.example.services.plugin.SmartBizPluginSdk

class AdvancedReportsPlugin : SmartBizPlugin {
    override val manifest = PluginManifest(
        pluginId = "com.smartbiz.plugin.advanced_reports",
        pluginName = "Executive Custom Reports",
        version = "1.2.0",
        developer = "BillNova Core Team",
        description = "Build customizable multi-branch financial reports, GST summaries, and export directly to PDF or Excel.",
        pluginType = PluginType.REPORTS,
        permissions = listOf(PluginPermissionType.ACCESS_REPORTS, PluginPermissionType.ACCESS_SALES),
        license = PluginLicenseInfo("FREE", "", true)
    )

    override suspend fun onEnable(sdk: SmartBizPluginSdk) {
        sdk.registerNavigationItem(
            RegisteredNavigationItem(
                pluginId = manifest.pluginId,
                title = "Custom Reports Exporter",
                route = "plugin_custom_reports_exporter",
                iconName = "Assessment",
                order = 10
            )
        )
        sdk.registerUiWidget(
            RegisteredUiWidget(
                pluginId = manifest.pluginId,
                widgetId = "widget_reports_summary",
                title = "Export GST & Multi-Branch Summaries",
                description = "Quick export invoice audit records to CSV/PDF",
                targetScreen = "DASHBOARD"
            )
        )
        sdk.registerCommand(manifest.pluginId, "EXPORT_GST_REPORT") { args ->
            PluginCommandResult(true, "GST Tax Summary generated successfully for parameters: $args", "{\"downloadUrl\":\"https://exports.smartbiz.internal/gst.pdf\"}")
        }
    }

    override suspend fun onDisable(sdk: SmartBizPluginSdk) {
        sdk.unregisterPluginResources(manifest.pluginId)
    }
}

class RazorpayGatewayPlugin : SmartBizPlugin {
    override val manifest = PluginManifest(
        pluginId = "com.smartbiz.plugin.razorpay_gateway",
        pluginName = "Razorpay & UPI Express Payment Adapter",
        version = "2.0.1",
        developer = "FinTech Connect",
        description = "Enable seamless UPI QR generation, Razorpay dynamic links, and auto-settlement reconciliation.",
        pluginType = PluginType.PAYMENTS,
        permissions = listOf(PluginPermissionType.ACCESS_PAYMENTS, PluginPermissionType.ACCESS_SALES),
        license = PluginLicenseInfo("PAID", "LIC-RZP-99201", true)
    )

    override suspend fun onEnable(sdk: SmartBizPluginSdk) {
        sdk.subscribe("INVOICE_CREATED") { event ->
            // Auto generate dynamic payment link upon invoice creation
            sdk.publishEvent(
                PluginEvent(
                    eventName = "PAYMENT_LINK_GENERATED",
                    pluginId = manifest.pluginId,
                    payloadJson = "{\"invoice\":\"INV-1001\",\"link\":\"https://rzp.io/i/INV-1001\"}"
                )
            )
        }
        sdk.registerCommand(manifest.pluginId, "INITIATE_UPI_REFUND") { args ->
            PluginCommandResult(true, "Razorpay UPI refund initiated for args: $args", "{\"refundTxnId\":\"RZP_RFD_${System.currentTimeMillis()}\"}")
        }
    }

    override suspend fun onDisable(sdk: SmartBizPluginSdk) {
        sdk.unregisterPluginResources(manifest.pluginId)
    }
}

class WhatsAppCommunicationPlugin : SmartBizPlugin {
    override val manifest = PluginManifest(
        pluginId = "com.smartbiz.plugin.whatsapp_alerts",
        pluginName = "WhatsApp Business Instant Messaging",
        version = "2.1.0",
        developer = "BillNova Core Team",
        description = "Send automated WhatsApp PDF invoice copies, payment reminders, and low-stock notification alerts.",
        pluginType = PluginType.COMMUNICATION,
        permissions = listOf(PluginPermissionType.ACCESS_COMMUNICATION, PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_CUSTOMERS),
        license = PluginLicenseInfo("FREE", "", true)
    )

    override suspend fun onEnable(sdk: SmartBizPluginSdk) {
        sdk.registerUiWidget(
            RegisteredUiWidget(
                pluginId = manifest.pluginId,
                widgetId = "widget_whatsapp_quick_send",
                title = "WhatsApp Invoice Dispatch",
                description = "Send invoice copy via official WhatsApp Business API",
                targetScreen = "INVOICE_DETAIL"
            )
        )
        sdk.registerCommand(manifest.pluginId, "SEND_WHATSAPP_INVOICE") { args ->
            PluginCommandResult(true, "WhatsApp invoice copy dispatched successfully to customer recipient.", "{\"status\":\"DELIVERED\",\"messageId\":\"WA_MSG_9912\"}")
        }
    }

    override suspend fun onDisable(sdk: SmartBizPluginSdk) {
        sdk.unregisterPluginResources(manifest.pluginId)
    }
}

class AiCopilotExtensionPlugin : SmartBizPlugin {
    override val manifest = PluginManifest(
        pluginId = "com.smartbiz.plugin.ai_assistant",
        pluginName = "Gemini Business Copilot Extension",
        version = "3.0.0",
        developer = "BillNova AI Research",
        description = "Intelligent AI assistant for automated sales insights, demand forecasting, and inventory optimization.",
        pluginType = PluginType.AI,
        permissions = listOf(PluginPermissionType.ACCESS_AI, PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_INVENTORY),
        license = PluginLicenseInfo("FREE", "", true)
    )

    override suspend fun onEnable(sdk: SmartBizPluginSdk) {
        sdk.registerUiWidget(
            RegisteredUiWidget(
                pluginId = manifest.pluginId,
                widgetId = "widget_ai_copilot_card",
                title = "Gemini Business Copilot Insights",
                description = "Automated sales anomaly detection and demand prediction card",
                targetScreen = "DASHBOARD"
            )
        )
        sdk.registerCommand(manifest.pluginId, "ANALYZE_STOCK_TRENDS") { args ->
            PluginCommandResult(true, "Gemini AI analysis complete: High probability demand surge for POS Hardware in 14 days.", "{\"confidence\":0.94,\"predictedSurge\":35}")
        }
    }

    override suspend fun onDisable(sdk: SmartBizPluginSdk) {
        sdk.unregisterPluginResources(manifest.pluginId)
    }
}
