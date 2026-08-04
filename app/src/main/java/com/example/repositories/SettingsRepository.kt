package com.example.repositories

import com.example.core.services.SharedPreferencesService

class SettingsRepository(private val prefsService: SharedPreferencesService) {
    fun getBusinessName(): String = prefsService.businessName
    fun setBusinessName(name: String) { prefsService.businessName = name }

    fun getBusinessAddress(): String = prefsService.businessAddress
    fun setBusinessAddress(address: String) { prefsService.businessAddress = address }

    fun getBusinessPhone(): String = prefsService.businessPhone
    fun setBusinessPhone(phone: String) { prefsService.businessPhone = phone }

    fun getCurrencySymbol(): String = prefsService.currencySymbol
    fun setCurrencySymbol(currency: String) { prefsService.currencySymbol = currency }

    fun getDefaultTaxRate(): Float = prefsService.defaultTaxRate
    fun setDefaultTaxRate(rate: Float) { prefsService.defaultTaxRate = rate }

    fun isDarkModeEnabled(): Boolean = prefsService.darkModeEnabled
    fun setDarkModeEnabled(enabled: Boolean) { prefsService.darkModeEnabled = enabled }
}
