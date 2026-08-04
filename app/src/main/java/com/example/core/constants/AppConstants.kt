package com.example.core.constants

object AppConstants {
    const val APP_NAME = "SmartBiz"
    const val APP_TAGLINE = "Modern Business Billing & Inventory"
    const val APP_VERSION = "1.0.0"

    // Database Constants
    const val DATABASE_NAME = "smartbiz_db"
    const val DATABASE_VERSION = 6

    // Preference Keys
    const val PREF_NAME = "smartbiz_prefs"
    const val KEY_BUSINESS_NAME = "business_name"
    const val KEY_BUSINESS_ADDRESS = "business_address"
    const val KEY_BUSINESS_PHONE = "business_phone"
    const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    const val KEY_DEFAULT_TAX_RATE = "default_tax_rate"
    const val KEY_DARK_MODE = "dark_mode_enabled"

    // Default Values
    const val DEFAULT_BUSINESS_NAME = "SmartBiz Commercial Store"
    const val DEFAULT_CURRENCY = "$"
    const val DEFAULT_TAX_RATE = 10.0f

    // Animation Durations (ms)
    const val ANIM_SHORT = 150
    const val ANIM_MEDIUM = 300
    const val ANIM_LONG = 500

    // Touch Targets
    const val MIN_TOUCH_TARGET_DP = 48
}

object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val INVOICES = "invoices"
    const val INVENTORY = "inventory"
    const val CUSTOMERS = "customers"
    const val PURCHASES = "purchases"
    const val SUPPLIERS = "suppliers"
    const val EXPENSES = "expenses"
    const val INCOME = "income"
    const val CASH_BOOK = "cash_book"
    const val EXPENSE_CATEGORIES = "expense_categories"
    const val SETTINGS = "settings"
    const val CREATE_INVOICE = "create_invoice"
    const val ADD_PRODUCT = "add_product"
    const val ADD_CUSTOMER = "add_customer"
}
