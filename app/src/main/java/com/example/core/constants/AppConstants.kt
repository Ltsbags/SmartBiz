package com.example.core.constants

object AppConstants {
    const val APP_NAME = "SmartBiz"
    const val APP_TAGLINE = "Modern Business Billing & Inventory"
    const val APP_VERSION = "1.0.0"

    // Database Constants
    const val DATABASE_NAME = "smartbiz_db"
    const val DATABASE_VERSION = 10

    // Preference Keys
    const val PREF_NAME = "smartbiz_prefs"
    const val KEY_BUSINESS_NAME = "business_name"
    const val KEY_BUSINESS_TAGLINE = "business_tagline"
    const val KEY_BUSINESS_ADDRESS = "business_address"
    const val KEY_BUSINESS_PHONE = "business_phone"
    const val KEY_BUSINESS_EMAIL = "business_email"
    const val KEY_BUSINESS_GST = "business_gst"
    const val KEY_BUSINESS_PAN = "business_pan"
    const val KEY_BUSINESS_WEBSITE = "business_website"
    const val KEY_BUSINESS_LOGO_URI = "business_logo_uri"

    const val KEY_INVOICE_PREFIX = "invoice_prefix"
    const val KEY_NEXT_INVOICE_NUMBER = "next_invoice_number"
    const val KEY_INVOICE_TERMS = "invoice_terms"
    const val KEY_INVOICE_NOTES = "invoice_notes"
    const val KEY_IS_TAX_INCLUSIVE = "is_tax_inclusive"
    const val KEY_SHOW_LOGO_ON_INVOICE = "show_logo_on_invoice"

    const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    const val KEY_DEFAULT_TAX_RATE = "default_tax_rate"
    const val KEY_DEFAULT_PAYMENT_TERMS_DAYS = "default_payment_terms_days"
    const val KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold"
    const val KEY_DATE_FORMAT = "date_format"
    const val KEY_COMPACT_UI_ENABLED = "compact_ui_enabled"

    const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"

    const val KEY_THEME_MODE = "theme_mode" // SYSTEM, LIGHT, DARK
    const val KEY_DARK_MODE = "dark_mode_enabled"
    const val KEY_DYNAMIC_COLOR_ENABLED = "dynamic_color_enabled"
    const val KEY_ACCENT_COLOR_HEX = "accent_color_hex"

    // Default Values
    const val DEFAULT_BUSINESS_NAME = "SmartBiz Commercial Store"
    const val DEFAULT_BUSINESS_TAGLINE = "Modern Business Billing & Inventory"
    const val DEFAULT_CURRENCY = "$"
    const val DEFAULT_TAX_RATE = 10.0f
    const val DEFAULT_INVOICE_PREFIX = "INV-"
    const val DEFAULT_INVOICE_TERMS = "Payment due within 15 days of invoice date."
    const val DEFAULT_INVOICE_NOTES = "Thank you for doing business with us!"

    // Animation Durations (ms)
    const val ANIM_SHORT = 150
    const val ANIM_MEDIUM = 300
    const val ANIM_LONG = 500

    // Touch Targets
    const val MIN_TOUCH_TARGET_DP = 48
}

object NavRoutes {
    const val AUTH = "auth"
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
    const val REPORTS = "reports"
    const val CREATE_INVOICE = "create_invoice"
    const val ADD_PRODUCT = "add_product"
    const val ADD_CUSTOMER = "add_customer"
    const val SEARCH = "global_search"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SECURITY_SETTINGS = "security_settings"
    const val DEVICE_MANAGEMENT = "device_management"
    const val LOGIN_HISTORY = "login_history"
    const val ACTIVE_SESSIONS = "active_sessions"
    const val ROLES_LIST = "roles_list"
    const val ROLE_DETAILS = "role_details/{roleId}"
    const val ROLE_CREATE = "role_create"
    const val PERMISSIONS_LIST = "permissions_list"
    const val USER_ROLES_MANAGEMENT = "user_roles_management"
    const val ACCESS_DENIED = "access_denied"
}
