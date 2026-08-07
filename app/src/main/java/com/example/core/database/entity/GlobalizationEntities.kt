package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locales")
data class LocaleEntity(
    @PrimaryKey val code: String, // e.g., "en", "hi", "ar", "fr", "es", "de", "zh", "ja"
    val name: String,             // e.g., "English", "العربية", "हिन्दी"
    val nativeName: String,
    val isRtl: Boolean = false,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val id: String, // e.g. "en_app_title", "ar_invoices"
    val localeCode: String,
    val key: String,
    val value: String,
    val category: String = "COMMON"
)

@Entity(tableName = "currency_settings")
data class CurrencySettingsEntity(
    @PrimaryKey val code: String, // e.g., "USD", "INR", "EUR", "GBP", "AED", "SAR", "JPY"
    val name: String,
    val symbol: String,
    val decimalPrecision: Int = 2,
    val exchangeRateToUsd: Double = 1.0,
    val thousandsSeparator: String = ",",
    val decimalSeparator: String = ".",
    val isDefault: Boolean = false
)

@Entity(tableName = "tax_profiles")
data class TaxProfileEntity(
    @PrimaryKey val id: String,
    val countryCode: String, // "IN", "US", "AE", "GB", "DE"
    val name: String,        // "GST", "VAT", "Sales Tax"
    val ratePercentage: Double,
    val taxType: String,     // "INCLUSIVE", "EXCLUSIVE", "COMPOUND"
    val isDefault: Boolean = false,
    val rulesJson: String = "{}"
)
