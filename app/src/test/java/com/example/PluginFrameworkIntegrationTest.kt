package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.repositories.PluginRepository
import com.example.services.plugin.PluginEvent
import com.example.services.plugin.PluginLicenseService
import com.example.services.plugin.PluginManagerService
import com.example.services.plugin.PluginMarketplaceService
import com.example.services.plugin.PluginPermissionService
import com.example.services.plugin.PluginPermissionType
import com.example.services.plugin.SmartBizPluginSdk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PluginFrameworkIntegrationTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pluginRepository: PluginRepository
    private lateinit var sdk: SmartBizPluginSdk

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)

        val permissionService = PluginPermissionService(dbHelper.pluginDao)
        val licenseService = PluginLicenseService(dbHelper.pluginDao)
        val managerService = PluginManagerService(dbHelper.pluginDao, permissionService, licenseService)
        val marketplaceService = PluginMarketplaceService()

        sdk = SmartBizPluginSdk.getInstance()
        pluginRepository = PluginRepository(
            pluginDao = dbHelper.pluginDao,
            managerService = managerService,
            permissionService = permissionService,
            licenseService = licenseService,
            marketplaceService = marketplaceService,
            sdk = sdk
        )
    }

    @Test
    fun testPluginInstallationAndLifecycle() = runBlocking {
        val catalog = pluginRepository.getMarketplaceCatalog()
        assertTrue(catalog.isNotEmpty())

        val rzpItem = catalog.first { it.manifest.pluginId == "com.smartbiz.plugin.razorpay_gateway" }
        // Ensure clean initial state if previous test installed it
        pluginRepository.uninstallPlugin("com.smartbiz.plugin.razorpay_gateway")

        val installRes = pluginRepository.installPluginFromMarketplace(rzpItem)
        assertTrue(installRes)

        val installed = pluginRepository.installedPlugins.first()
        assertTrue(installed.any { it.id == "com.smartbiz.plugin.razorpay_gateway" })

        // Enable plugin
        val enableRes = pluginRepository.enablePlugin("com.smartbiz.plugin.razorpay_gateway")
        assertTrue(enableRes)

        val enabledList = pluginRepository.enabledPlugins.first()
        assertTrue(enabledList.any { it.id == "com.smartbiz.plugin.razorpay_gateway" })

        // Disable plugin
        val disableRes = pluginRepository.disablePlugin("com.smartbiz.plugin.razorpay_gateway")
        assertTrue(disableRes)

        // Uninstall plugin
        val uninstallRes = pluginRepository.uninstallPlugin("com.smartbiz.plugin.razorpay_gateway")
        assertTrue(uninstallRes)

        val remaining = pluginRepository.installedPlugins.first()
        assertFalse(remaining.any { it.id == "com.smartbiz.plugin.razorpay_gateway" })
    }

    @Test
    fun testPluginSdkEventBusAndCommandExecution() = runBlocking {
        var receivedEvent: PluginEvent? = null
        sdk.subscribe("INVOICE_CREATED") { event ->
            receivedEvent = event
        }

        val testInvoiceEvent = PluginEvent("INVOICE_CREATED", "test_plugin", "{\"invoiceNumber\":\"INV-99001\"}")
        pluginRepository.publishEvent(testInvoiceEvent)

        assertNotNull(receivedEvent)
        assertEquals("INVOICE_CREATED", receivedEvent?.eventName)
        assertTrue(receivedEvent?.payloadJson?.contains("INV-99001") == true)

        // Test command registration and execution
        sdk.registerCommand("test_plugin", "CALCULATE_DISCOUNT") { args ->
            com.example.services.plugin.PluginCommandResult(true, "Discount calculated", "{\"discountAmount\":150.0}")
        }

        val cmdResult = pluginRepository.executePluginCommand("test_plugin", "CALCULATE_DISCOUNT", "{}")
        assertTrue(cmdResult.success)
        assertTrue(cmdResult.dataJson.contains("150.0"))
    }

    @Test
    fun testPluginPermissionValidationAndSandboxing() = runBlocking {
        val catalog = pluginRepository.getMarketplaceCatalog()
        val rzpItem = catalog.first { it.manifest.pluginId == "com.smartbiz.plugin.razorpay_gateway" }
        pluginRepository.installPluginFromMarketplace(rzpItem)

        // Grant permission
        pluginRepository.setPermissionGranted("com.smartbiz.plugin.razorpay_gateway", PluginPermissionType.ACCESS_PAYMENTS.key, true)

        val granted = pluginRepository.permissionService.isPermissionGranted("com.smartbiz.plugin.razorpay_gateway", PluginPermissionType.ACCESS_PAYMENTS)
        assertTrue(granted)

        val isAllowedInSdk = pluginRepository.permissionService.isSandboxedAccessAllowed("com.smartbiz.plugin.razorpay_gateway", PluginPermissionType.ACCESS_PAYMENTS)
        assertTrue(isAllowedInSdk)
    }

    @Test
    fun testPluginLicenseVerificationAndTrialCaching() = runBlocking {
        val freeValidation = pluginRepository.validateLicense("com.smartbiz.plugin.advanced_reports", "", "FREE")
        assertTrue(freeValidation.isValid)

        val trialValidation = pluginRepository.validateLicense("com.smartbiz.plugin.delhivery_shipping", "", "TRIAL")
        assertTrue(trialValidation.isValid)
        assertTrue(trialValidation.isTrialActive)

        val paidValidationInvalid = pluginRepository.validateLicense("com.smartbiz.plugin.tally_exporter", "", "PAID")
        assertFalse(paidValidationInvalid.isValid)

        val paidValidationValid = pluginRepository.validateLicense("com.smartbiz.plugin.tally_exporter", "LIC-TAL-88310", "PAID")
        assertTrue(paidValidationValid.isValid)
    }

    @Test
    fun testPluginMarketplaceCatalogQueries() = runBlocking {
        val paymentPlugins = pluginRepository.getMarketplaceCatalog(category = "PAYMENTS")
        assertTrue(paymentPlugins.isNotEmpty())
        assertTrue(paymentPlugins.all { it.manifest.pluginType.name == "PAYMENTS" })

        val searchQueryPlugins = pluginRepository.getMarketplaceCatalog(searchQuery = "WhatsApp")
        assertTrue(searchQueryPlugins.isNotEmpty())
        assertTrue(searchQueryPlugins.first().manifest.pluginName.contains("WhatsApp"))
    }
}
