package com.example.features.globalization.models

data class AppLocale(
    val code: String,
    val name: String,
    val nativeName: String,
    val isRtl: Boolean = false,
    val isDefault: Boolean = false,
    val flagEmoji: String = "🌐"
)

data class CurrencyConfig(
    val code: String,            // "USD", "INR", "EUR", "GBP", "AED", "SAR", "JPY"
    val name: String,
    val symbol: String,
    val decimalPrecision: Int = 2,
    val exchangeRateToUsd: Double = 1.0,
    val thousandsSeparator: String = ",",
    val decimalSeparator: String = ".",
    val isDefault: Boolean = false
)

enum class TaxType {
    INCLUSIVE, EXCLUSIVE, COMPOUND
}

data class TaxProfile(
    val id: String,
    val countryCode: String,     // "IN", "US", "AE", "GB", "DE", "FR", "ES", "JP"
    val name: String,            // "GST", "VAT", "Sales Tax", "Service Tax"
    val ratePercentage: Double,
    val taxType: TaxType = TaxType.EXCLUSIVE,
    val isDefault: Boolean = false,
    val taxCode: String = ""
)

data class TaxCalculationResult(
    val netAmount: Double,
    val taxAmount: Double,
    val grossAmount: Double,
    val taxName: String,
    val ratePercentage: Double,
    val taxType: TaxType
)

enum class DateFormatPattern(val label: String, val pattern: String) {
    ISO("ISO 8601 (YYYY-MM-DD)", "yyyy-MM-dd"),
    US("US Standard (MM/DD/YYYY)", "MM/dd/yyyy"),
    EUROPEAN("European (DD/MM/YYYY)", "dd/MM/yyyy"),
    INDIAN("Indian (DD-MM-YYYY)", "dd-MM-yyyy"),
    FULL_TEXT("Full Text (MMM dd, yyyy)", "MMM dd, yyyy")
}

enum class NumberFormatStyle {
    STANDARD, PERCENTAGE, ACCOUNTING, SCIENTIFIC
}

data class RegionalSettings(
    val currentLocale: AppLocale,
    val activeCurrency: CurrencyConfig,
    val activeTaxProfile: TaxProfile,
    val dateFormat: DateFormatPattern = DateFormatPattern.ISO,
    val is24HourFormat: Boolean = false,
    val weekStartDay: String = "MONDAY",
    val timeZone: String = "UTC"
)
