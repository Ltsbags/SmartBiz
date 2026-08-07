package com.example.services.plugin

data class MarketplacePluginItem(
    val manifest: PluginManifest,
    val rating: Double,
    val reviewCount: Int,
    val downloadsCount: Int,
    val priceText: String, // "Free", "$9.99/mo", "14-Day Free Trial"
    val isFeatured: Boolean = false,
    val isOfficial: Boolean = true,
    val screenshots: List<String> = emptyList(),
    val releaseNotes: String = ""
)

class PluginMarketplaceService {

    private val availableCatalog = listOf(
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.advanced_reports",
                pluginName = "Executive Custom Reports",
                version = "1.2.0",
                developer = "SmartBiz Core Team",
                description = "Build customizable multi-branch financial reports, GST summaries, and export directly to PDF or Excel.",
                pluginType = PluginType.REPORTS,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_REPORTS, PluginPermissionType.ACCESS_SALES),
                license = PluginLicenseInfo("FREE", "", true),
                entryPointClass = "com.example.services.plugin.builtin.AdvancedReportsPlugin"
            ),
            rating = 4.8,
            reviewCount = 142,
            downloadsCount = 12500,
            priceText = "Free",
            isFeatured = true,
            releaseNotes = "Added automated PDF export scheduler and branch comparison columns."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.razorpay_gateway",
                pluginName = "Razorpay & UPI Express Payment Adapter",
                version = "2.0.1",
                developer = "FinTech Connect",
                description = "Enable seamless UPI QR generation, Razorpay dynamic links, and auto-settlement reconciliation.",
                pluginType = PluginType.PAYMENTS,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_PAYMENTS, PluginPermissionType.ACCESS_SALES),
                license = PluginLicenseInfo("PAID", "LIC-RZP-99201", true),
                entryPointClass = "com.example.services.plugin.builtin.RazorpayGatewayPlugin"
            ),
            rating = 4.9,
            reviewCount = 310,
            downloadsCount = 28900,
            priceText = "14-Day Free Trial",
            isFeatured = true,
            releaseNotes = "Supports UPI Intent, GooglePay, and instant payment reconciliation status hooks."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.delhivery_shipping",
                pluginName = "Delhivery & FedEx Logistics Sync",
                version = "1.0.4",
                developer = "LogiTech India",
                description = "Generate shipping waybills, print shipping barcode labels, and track package dispatch live.",
                pluginType = PluginType.SHIPPING,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_CUSTOMERS),
                license = PluginLicenseInfo("TRIAL", "", true),
                entryPointClass = "com.example.services.plugin.builtin.ShippingLogisticsPlugin"
            ),
            rating = 4.6,
            reviewCount = 88,
            downloadsCount = 5400,
            priceText = "Trial / $4.99/mo",
            isFeatured = false,
            releaseNotes = "Added bulk AWB label printing for batch invoices."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.tally_exporter",
                pluginName = "Tally Prime & QuickBooks Sync",
                version = "1.5.0",
                developer = "TaxBiz Labs",
                description = "One-click export of sales vouchers, credit notes, and expense ledgers into Tally Prime XML.",
                pluginType = PluginType.ACCOUNTING,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_PURCHASES, PluginPermissionType.ACCESS_REPORTS),
                license = PluginLicenseInfo("PAID", "LIC-TAL-88310", true),
                entryPointClass = "com.example.services.plugin.builtin.TallyExporterPlugin"
            ),
            rating = 4.7,
            reviewCount = 205,
            downloadsCount = 18200,
            priceText = "$14.99/yr",
            isFeatured = true,
            releaseNotes = "Supports Tally Prime 4.0 XML schemas and multi-currency mapping."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.customer_loyalty",
                pluginName = "Smart Customer Loyalty & Rewards",
                version = "1.1.2",
                developer = "GrowthEngine",
                description = "Reward points program on invoice amounts, birthday coupons, and tiered membership badges.",
                pluginType = PluginType.CRM,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_CUSTOMERS, PluginPermissionType.ACCESS_SALES),
                license = PluginLicenseInfo("FREE", "", true),
                entryPointClass = "com.example.services.plugin.builtin.CustomerLoyaltyPlugin"
            ),
            rating = 4.5,
            reviewCount = 64,
            downloadsCount = 8900,
            priceText = "Free",
            isFeatured = false,
            releaseNotes = "Auto-calculates reward points on invoice creation."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.whatsapp_alerts",
                pluginName = "WhatsApp Business Instant Messaging",
                version = "2.1.0",
                developer = "SmartBiz Core Team",
                description = "Send automated WhatsApp PDF invoice copies, payment reminders, and low-stock notification alerts.",
                pluginType = PluginType.COMMUNICATION,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_COMMUNICATION, PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_CUSTOMERS),
                license = PluginLicenseInfo("FREE", "", true),
                entryPointClass = "com.example.services.plugin.builtin.WhatsAppCommunicationPlugin"
            ),
            rating = 4.9,
            reviewCount = 420,
            downloadsCount = 35000,
            priceText = "Free",
            isFeatured = true,
            releaseNotes = "Supports interactive WhatsApp template buttons and delivery status callbacks."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.ai_assistant",
                pluginName = "Gemini Business Copilot Extension",
                version = "3.0.0",
                developer = "SmartBiz AI Research",
                description = "Intelligent AI assistant for automated sales insights, demand forecasting, and inventory optimization.",
                pluginType = PluginType.AI,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_AI, PluginPermissionType.ACCESS_SALES, PluginPermissionType.ACCESS_INVENTORY),
                license = PluginLicenseInfo("FREE", "", true),
                entryPointClass = "com.example.services.plugin.builtin.AiCopilotExtensionPlugin"
            ),
            rating = 5.0,
            reviewCount = 512,
            downloadsCount = 42000,
            priceText = "Free",
            isFeatured = true,
            releaseNotes = "Integrates Gemini 1.5 Pro engine for sales query answers and stock predictions."
        ),
        MarketplacePluginItem(
            manifest = PluginManifest(
                pluginId = "com.smartbiz.plugin.barcode_batch",
                pluginName = "Barcode, IMEI & Batch Expiry Manager",
                version = "1.3.0",
                developer = "Inventory Pro",
                description = "Track product batch numbers, serial codes, manufacture/expiry dates, and custom barcode sticker printing.",
                pluginType = PluginType.INVENTORY,
                requiredApiVersion = 1,
                permissions = listOf(PluginPermissionType.ACCESS_INVENTORY, PluginPermissionType.ACCESS_PURCHASES),
                license = PluginLicenseInfo("TRIAL", "", true),
                entryPointClass = "com.example.services.plugin.builtin.BarcodeBatchPlugin"
            ),
            rating = 4.6,
            reviewCount = 115,
            downloadsCount = 11000,
            priceText = "14-Day Free Trial",
            isFeatured = false,
            releaseNotes = "Added thermal printer label size templates."
        )
    )

    fun getAvailablePlugins(category: String? = null, searchQuery: String? = null): List<MarketplacePluginItem> {
        return availableCatalog.filter { item ->
            val matchesCategory = category.isNullOrBlank() || category == "ALL" || item.manifest.pluginType.name.equals(category, ignoreCase = true)
            val query = searchQuery?.trim()
            val matchesSearch = query.isNullOrBlank() ||
                    item.manifest.pluginName.contains(query, ignoreCase = true) ||
                    item.manifest.description.contains(query, ignoreCase = true) ||
                    item.manifest.developer.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    fun getFeaturedPlugins(): List<MarketplacePluginItem> {
        return availableCatalog.filter { it.isFeatured }
    }

    fun getPluginByMarketplaceId(pluginId: String): MarketplacePluginItem? {
        return availableCatalog.find { it.manifest.pluginId == pluginId }
    }
}
