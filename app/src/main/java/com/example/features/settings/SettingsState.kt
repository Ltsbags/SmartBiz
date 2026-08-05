package com.example.features.settings

data class SettingsState(
    // Business Profile
    val businessName: String = "",
    val businessTagline: String = "",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessGst: String = "",
    val businessPan: String = "",
    val businessWebsite: String = "",
    val businessLogoUri: String = "",

    // Invoice Settings
    val invoicePrefix: String = "INV-",
    val nextInvoiceNumber: Int = 1001,
    val invoiceTerms: String = "",
    val invoiceNotes: String = "",
    val isTaxInclusive: Boolean = false,
    val showLogoOnInvoice: Boolean = true,

    // Application Settings
    val currencySymbol: String = "$",
    val defaultTaxRate: Float = 10.0f,
    val defaultPaymentTermsDays: Int = 15,
    val lowStockThreshold: Int = 5,
    val dateFormat: String = "dd/MM/yyyy",
    val compactUiEnabled: Boolean = false,

    // Backup & Restore
    val autoBackupEnabled: Boolean = false,
    val lastBackupTimestamp: Long = 0L,

    // Theme Settings
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val isDarkMode: Boolean = false,
    val dynamicColorEnabled: Boolean = true,
    val accentColorHex: String = "#2196F3",

    // Security Settings
    val isAppLockEnabled: Boolean = true,
    val isBiometricsEnabled: Boolean = false,

    // UI Control
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val isBackupDialogOpen: Boolean = false,
    val isResetConfirmDialogOpen: Boolean = false
)
