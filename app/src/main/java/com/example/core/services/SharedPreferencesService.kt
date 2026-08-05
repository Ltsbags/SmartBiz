package com.example.core.services

import android.content.Context
import android.content.SharedPreferences
import com.example.core.constants.AppConstants

class SharedPreferencesService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        AppConstants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    var businessName: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_NAME, AppConstants.DEFAULT_BUSINESS_NAME) ?: AppConstants.DEFAULT_BUSINESS_NAME
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_NAME, value).apply()

    var businessTagline: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_TAGLINE, AppConstants.DEFAULT_BUSINESS_TAGLINE) ?: AppConstants.DEFAULT_BUSINESS_TAGLINE
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_TAGLINE, value).apply()

    var businessAddress: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_ADDRESS, "100 Commerce Way, Suite 400, NY") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_ADDRESS, value).apply()

    var businessPhone: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_PHONE, "+1 (800) 555-BIZ1") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_PHONE, value).apply()

    var businessEmail: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_EMAIL, "contact@smartbiz.com") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_EMAIL, value).apply()

    var businessGst: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_GST, "27AAAAA0000A1Z5") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_GST, value).apply()

    var businessPan: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_PAN, "AAAAA0000A") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_PAN, value).apply()

    var businessWebsite: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_WEBSITE, "https://smartbiz.com") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_WEBSITE, value).apply()

    var businessLogoUri: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_LOGO_URI, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_LOGO_URI, value).apply()

    // Invoice Settings
    var invoicePrefix: String
        get() = prefs.getString(AppConstants.KEY_INVOICE_PREFIX, AppConstants.DEFAULT_INVOICE_PREFIX) ?: AppConstants.DEFAULT_INVOICE_PREFIX
        set(value) = prefs.edit().putString(AppConstants.KEY_INVOICE_PREFIX, value).apply()

    var nextInvoiceNumber: Int
        get() = prefs.getInt(AppConstants.KEY_NEXT_INVOICE_NUMBER, 1001)
        set(value) = prefs.edit().putInt(AppConstants.KEY_NEXT_INVOICE_NUMBER, value).apply()

    var invoiceTerms: String
        get() = prefs.getString(AppConstants.KEY_INVOICE_TERMS, AppConstants.DEFAULT_INVOICE_TERMS) ?: AppConstants.DEFAULT_INVOICE_TERMS
        set(value) = prefs.edit().putString(AppConstants.KEY_INVOICE_TERMS, value).apply()

    var invoiceNotes: String
        get() = prefs.getString(AppConstants.KEY_INVOICE_NOTES, AppConstants.DEFAULT_INVOICE_NOTES) ?: AppConstants.DEFAULT_INVOICE_NOTES
        set(value) = prefs.edit().putString(AppConstants.KEY_INVOICE_NOTES, value).apply()

    var isTaxInclusive: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_IS_TAX_INCLUSIVE, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_IS_TAX_INCLUSIVE, value).apply()

    var showLogoOnInvoice: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_SHOW_LOGO_ON_INVOICE, true)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_SHOW_LOGO_ON_INVOICE, value).apply()

    // Application Settings
    var currencySymbol: String
        get() = prefs.getString(AppConstants.KEY_CURRENCY_SYMBOL, AppConstants.DEFAULT_CURRENCY) ?: AppConstants.DEFAULT_CURRENCY
        set(value) = prefs.edit().putString(AppConstants.KEY_CURRENCY_SYMBOL, value).apply()

    var defaultTaxRate: Float
        get() = prefs.getFloat(AppConstants.KEY_DEFAULT_TAX_RATE, AppConstants.DEFAULT_TAX_RATE)
        set(value) = prefs.edit().putFloat(AppConstants.KEY_DEFAULT_TAX_RATE, value).apply()

    var defaultPaymentTermsDays: Int
        get() = prefs.getInt(AppConstants.KEY_DEFAULT_PAYMENT_TERMS_DAYS, 15)
        set(value) = prefs.edit().putInt(AppConstants.KEY_DEFAULT_PAYMENT_TERMS_DAYS, value).apply()

    var lowStockThreshold: Int
        get() = prefs.getInt(AppConstants.KEY_LOW_STOCK_THRESHOLD, 5)
        set(value) = prefs.edit().putInt(AppConstants.KEY_LOW_STOCK_THRESHOLD, value).apply()

    var dateFormat: String
        get() = prefs.getString(AppConstants.KEY_DATE_FORMAT, "dd/MM/yyyy") ?: "dd/MM/yyyy"
        set(value) = prefs.edit().putString(AppConstants.KEY_DATE_FORMAT, value).apply()

    var compactUiEnabled: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_COMPACT_UI_ENABLED, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_COMPACT_UI_ENABLED, value).apply()

    // Backup & Restore
    var autoBackupEnabled: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_AUTO_BACKUP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_AUTO_BACKUP_ENABLED, value).apply()

    var lastBackupTimestamp: Long
        get() = prefs.getLong(AppConstants.KEY_LAST_BACKUP_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(AppConstants.KEY_LAST_BACKUP_TIMESTAMP, value).apply()

    // Theme Settings
    var themeMode: String
        get() = prefs.getString(AppConstants.KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(AppConstants.KEY_THEME_MODE, value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_DARK_MODE, value).apply()

    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_DYNAMIC_COLOR_ENABLED, true)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_DYNAMIC_COLOR_ENABLED, value).apply()

    var accentColorHex: String
        get() = prefs.getString(AppConstants.KEY_ACCENT_COLOR_HEX, "#2196F3") ?: "#2196F3"
        set(value) = prefs.edit().putString(AppConstants.KEY_ACCENT_COLOR_HEX, value).apply()

    // Security & Authentication Settings
    var isBiometricsEnabled: Boolean
        get() = prefs.getBoolean("key_biometrics_enabled", false)
        set(value) = prefs.edit().putBoolean("key_biometrics_enabled", value).apply()

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean("key_app_lock_enabled", true)
        set(value) = prefs.edit().putBoolean("key_app_lock_enabled", value).apply()

    var failedPinAttempts: Int
        get() = prefs.getInt("key_failed_pin_attempts", 0)
        set(value) = prefs.edit().putInt("key_failed_pin_attempts", value).apply()

    var lockoutUntilTimestamp: Long
        get() = prefs.getLong("key_lockout_until_timestamp", 0L)
        set(value) = prefs.edit().putLong("key_lockout_until_timestamp", value).apply()

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getCustomString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun putCustomString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getEncrypted(key: String, defaultValue: String = ""): String {
        return prefs.getString("enc_$key", defaultValue) ?: defaultValue
    }

    fun saveEncrypted(key: String, value: String) {
        prefs.edit().putString("enc_$key", value).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: SharedPreferencesService? = null

        fun getInstance(context: Context): SharedPreferencesService {
            return INSTANCE ?: synchronized(this) {
                val instance = SharedPreferencesService(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
