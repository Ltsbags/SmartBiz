package com.example.repositories

import com.example.core.services.SharedPreferencesService

class SettingsRepository(private val prefsService: SharedPreferencesService) {
    // Business Profile
    fun getBusinessName(): String = prefsService.businessName
    fun setBusinessName(name: String) { prefsService.businessName = name }

    fun getBusinessTagline(): String = prefsService.businessTagline
    fun setBusinessTagline(tagline: String) { prefsService.businessTagline = tagline }

    fun getBusinessAddress(): String = prefsService.businessAddress
    fun setBusinessAddress(address: String) { prefsService.businessAddress = address }

    fun getBusinessPhone(): String = prefsService.businessPhone
    fun setBusinessPhone(phone: String) { prefsService.businessPhone = phone }

    fun getBusinessEmail(): String = prefsService.businessEmail
    fun setBusinessEmail(email: String) { prefsService.businessEmail = email }

    fun getBusinessGst(): String = prefsService.businessGst
    fun setBusinessGst(gst: String) { prefsService.businessGst = gst }

    fun getBusinessPan(): String = prefsService.businessPan
    fun setBusinessPan(pan: String) { prefsService.businessPan = pan }

    fun getBusinessWebsite(): String = prefsService.businessWebsite
    fun setBusinessWebsite(website: String) { prefsService.businessWebsite = website }

    fun getBusinessLogoUri(): String = prefsService.businessLogoUri
    fun setBusinessLogoUri(uri: String) { prefsService.businessLogoUri = uri }

    // Invoice Settings
    fun getInvoicePrefix(): String = prefsService.invoicePrefix
    fun setInvoicePrefix(prefix: String) { prefsService.invoicePrefix = prefix }

    fun getNextInvoiceNumber(): Int = prefsService.nextInvoiceNumber
    fun setNextInvoiceNumber(number: Int) { prefsService.nextInvoiceNumber = number }

    fun getInvoiceTerms(): String = prefsService.invoiceTerms
    fun setInvoiceTerms(terms: String) { prefsService.invoiceTerms = terms }

    fun getInvoiceNotes(): String = prefsService.invoiceNotes
    fun setInvoiceNotes(notes: String) { prefsService.invoiceNotes = notes }

    fun isTaxInclusive(): Boolean = prefsService.isTaxInclusive
    fun setTaxInclusive(inclusive: Boolean) { prefsService.isTaxInclusive = inclusive }

    fun isShowLogoOnInvoice(): Boolean = prefsService.showLogoOnInvoice
    fun setShowLogoOnInvoice(show: Boolean) { prefsService.showLogoOnInvoice = show }

    // Application Settings
    fun getCurrencySymbol(): String = prefsService.currencySymbol
    fun setCurrencySymbol(currency: String) { prefsService.currencySymbol = currency }

    fun getDefaultTaxRate(): Float = prefsService.defaultTaxRate
    fun setDefaultTaxRate(rate: Float) { prefsService.defaultTaxRate = rate }

    fun getDefaultPaymentTermsDays(): Int = prefsService.defaultPaymentTermsDays
    fun setDefaultPaymentTermsDays(days: Int) { prefsService.defaultPaymentTermsDays = days }

    fun getLowStockThreshold(): Int = prefsService.lowStockThreshold
    fun setLowStockThreshold(threshold: Int) { prefsService.lowStockThreshold = threshold }

    fun getDateFormat(): String = prefsService.dateFormat
    fun setDateFormat(format: String) { prefsService.dateFormat = format }

    fun isCompactUiEnabled(): Boolean = prefsService.compactUiEnabled
    fun setCompactUiEnabled(enabled: Boolean) { prefsService.compactUiEnabled = enabled }

    // Backup & Restore
    fun isAutoBackupEnabled(): Boolean = prefsService.autoBackupEnabled
    fun setAutoBackupEnabled(enabled: Boolean) { prefsService.autoBackupEnabled = enabled }

    fun getLastBackupTimestamp(): Long = prefsService.lastBackupTimestamp
    fun setLastBackupTimestamp(timestamp: Long) { prefsService.lastBackupTimestamp = timestamp }

    // Theme Settings
    fun getThemeMode(): String = prefsService.themeMode
    fun setThemeMode(mode: String) { prefsService.themeMode = mode }

    fun isDarkModeEnabled(): Boolean = prefsService.darkModeEnabled
    fun setDarkModeEnabled(enabled: Boolean) { prefsService.darkModeEnabled = enabled }

    fun isDynamicColorEnabled(): Boolean = prefsService.dynamicColorEnabled
    fun setDynamicColorEnabled(enabled: Boolean) { prefsService.dynamicColorEnabled = enabled }

    fun getAccentColorHex(): String = prefsService.accentColorHex
    fun setAccentColorHex(colorHex: String) { prefsService.accentColorHex = colorHex }

    // Security Settings
    fun isAppLockEnabled(): Boolean = prefsService.isAppLockEnabled
    fun setAppLockEnabled(enabled: Boolean) { prefsService.isAppLockEnabled = enabled }

    fun isBiometricsEnabled(): Boolean = prefsService.isBiometricsEnabled
    fun setBiometricsEnabled(enabled: Boolean) { prefsService.isBiometricsEnabled = enabled }

    fun clearAllPreferences() {
        prefsService.clearAll()
    }
}
