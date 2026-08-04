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

    var businessAddress: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_ADDRESS, "100 Commerce Way, Suite 400, NY") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_ADDRESS, value).apply()

    var businessPhone: String
        get() = prefs.getString(AppConstants.KEY_BUSINESS_PHONE, "+1 (800) 555-BIZ1") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_BUSINESS_PHONE, value).apply()

    var currencySymbol: String
        get() = prefs.getString(AppConstants.KEY_CURRENCY_SYMBOL, AppConstants.DEFAULT_CURRENCY) ?: AppConstants.DEFAULT_CURRENCY
        set(value) = prefs.edit().putString(AppConstants.KEY_CURRENCY_SYMBOL, value).apply()

    var defaultTaxRate: Float
        get() = prefs.getFloat(AppConstants.KEY_DEFAULT_TAX_RATE, AppConstants.DEFAULT_TAX_RATE)
        set(value) = prefs.edit().putFloat(AppConstants.KEY_DEFAULT_TAX_RATE, value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_DARK_MODE, value).apply()

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
