package com.example.features.globalization.repositories

import com.example.core.database.dao.GlobalizationDao
import com.example.core.database.entity.CurrencySettingsEntity
import com.example.core.database.entity.LocaleEntity
import com.example.core.database.entity.TaxProfileEntity
import com.example.core.database.entity.TranslationEntity
import com.example.features.globalization.models.AppLocale
import com.example.features.globalization.models.CurrencyConfig
import com.example.features.globalization.models.TaxProfile
import com.example.features.globalization.models.TaxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlobalizationRepository(
    private val dao: GlobalizationDao
) {
    fun getActiveLocales(): Flow<List<AppLocale>> {
        return dao.getActiveLocales().map { list ->
            if (list.isEmpty()) getDefaultLocales()
            else list.map { it.toModel() }
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        // Seed default supported languages
        val defaultLocales = listOf(
            LocaleEntity("en", "English", "English", isRtl = false, isDefault = true),
            LocaleEntity("hi", "Hindi", "हिन्दी", isRtl = false),
            LocaleEntity("ar", "Arabic", "العربية", isRtl = true),
            LocaleEntity("fr", "French", "Français", isRtl = false),
            LocaleEntity("es", "Spanish", "Español", isRtl = false),
            LocaleEntity("de", "German", "Deutsch", isRtl = false),
            LocaleEntity("zh", "Chinese", "中文", isRtl = false),
            LocaleEntity("ja", "Japanese", "日本語", isRtl = false)
        )
        dao.insertLocales(defaultLocales)

        // Seed default currencies
        val defaultCurrencies = listOf(
            CurrencySettingsEntity("USD", "US Dollar", "$", 2, 1.0, ",", ".", isDefault = true),
            CurrencySettingsEntity("INR", "Indian Rupee", "₹", 2, 0.012, ",", ".", isDefault = false),
            CurrencySettingsEntity("EUR", "Euro", "€", 2, 1.08, ".", ",", isDefault = false),
            CurrencySettingsEntity("GBP", "British Pound", "£", 2, 1.27, ",", ".", isDefault = false),
            CurrencySettingsEntity("AED", "UAE Dirham", "د.إ", 2, 0.27, ",", ".", isDefault = false),
            CurrencySettingsEntity("SAR", "Saudi Riyal", "﷼", 2, 0.27, ",", ".", isDefault = false),
            CurrencySettingsEntity("JPY", "Japanese Yen", "¥", 0, 0.0065, ",", ".", isDefault = false)
        )
        dao.insertCurrencies(defaultCurrencies)

        // Seed default tax profiles
        val defaultTaxProfiles = listOf(
            TaxProfileEntity("TAX_IN_GST", "IN", "GST (India)", 18.0, "EXCLUSIVE", isDefault = true),
            TaxProfileEntity("TAX_EU_VAT", "DE", "VAT (Germany/EU)", 19.0, "INCLUSIVE", isDefault = false),
            TaxProfileEntity("TAX_AE_VAT", "AE", "VAT (UAE)", 5.0, "EXCLUSIVE", isDefault = false),
            TaxProfileEntity("TAX_US_SALES", "US", "Sales Tax (US)", 8.25, "EXCLUSIVE", isDefault = false),
            TaxProfileEntity("TAX_UK_VAT", "GB", "VAT (UK)", 20.0, "INCLUSIVE", isDefault = false)
        )
        dao.insertTaxProfiles(defaultTaxProfiles)

        // Seed sample translations for key languages
        val sampleTranslations = listOf(
            // English
            TranslationEntity("en_dashboard", "en", "dashboard_title", "Enterprise Global Dashboard"),
            TranslationEntity("en_sales", "en", "sales_analytics", "Sales Analytics"),
            TranslationEntity("en_invoices", "en", "invoices", "Invoices"),
            TranslationEntity("en_settings", "en", "settings", "Settings"),
            TranslationEntity("en_welcome", "en", "welcome_user", "Welcome back, {name}!"),
            TranslationEntity("en_item_count", "en", "items_found", "{count} items found"),

            // Arabic
            TranslationEntity("ar_dashboard", "ar", "dashboard_title", "لوحة التحكم العالمية للمؤسسة"),
            TranslationEntity("ar_sales", "ar", "sales_analytics", "تحليلات المبيعات"),
            TranslationEntity("ar_invoices", "ar", "invoices", "الفواتير"),
            TranslationEntity("ar_settings", "ar", "settings", "الإعدادات"),
            TranslationEntity("ar_welcome", "ar", "welcome_user", "أهلاً بعودتك، {name}!"),

            // Hindi
            TranslationEntity("hi_dashboard", "hi", "dashboard_title", "एंटरप्राइज ग्लोबल डैशबोर्ड"),
            TranslationEntity("hi_sales", "hi", "sales_analytics", "बिक्री विश्लेषण"),
            TranslationEntity("hi_invoices", "hi", "invoices", "चालान (इन्वॉयस)"),
            TranslationEntity("hi_settings", "hi", "settings", "सेटिंग्स"),

            // French
            TranslationEntity("fr_dashboard", "fr", "dashboard_title", "Tableau de Bord Global"),
            TranslationEntity("fr_sales", "fr", "sales_analytics", "Analyse des Ventes"),
            TranslationEntity("fr_invoices", "fr", "invoices", "Factures"),

            // Spanish
            TranslationEntity("es_dashboard", "es", "dashboard_title", "Panel Global Empresarial"),
            TranslationEntity("es_sales", "es", "sales_analytics", "Análisis de Ventas"),
            TranslationEntity("es_invoices", "es", "invoices", "Facturas")
        )
        dao.insertTranslations(sampleTranslations)
    }

    fun getTranslations(localeCode: String): Flow<List<TranslationEntity>> {
        return dao.getTranslationsForLocale(localeCode)
    }

    fun getAllCurrencies(): Flow<List<CurrencyConfig>> {
        return dao.getAllCurrencies().map { list ->
            list.map { it.toModel() }
        }
    }

    fun getAllTaxProfiles(): Flow<List<TaxProfile>> {
        return dao.getAllTaxProfiles().map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun saveTranslation(localeCode: String, key: String, value: String) {
        val id = "${localeCode}_$key"
        dao.insertTranslation(TranslationEntity(id, localeCode, key, value))
    }

    suspend fun updateCurrencyRate(code: String, rateToUsd: Double) {
        val existing = dao.getCurrencyByCode(code)
        if (existing != null) {
            dao.insertCurrency(existing.copy(exchangeRateToUsd = rateToUsd))
        }
    }

    private fun LocaleEntity.toModel(): AppLocale {
        val emoji = when (code) {
            "en" -> "🇺🇸"
            "hi" -> "🇮🇳"
            "ar" -> "🇦🇪"
            "fr" -> "🇫🇷"
            "es" -> "🇪🇸"
            "de" -> "🇩🇪"
            "zh" -> "🇨🇳"
            "ja" -> "🇯🇵"
            else -> "🌐"
        }
        return AppLocale(code, name, nativeName, isRtl, isDefault, flagEmoji = emoji)
    }

    private fun CurrencySettingsEntity.toModel(): CurrencyConfig {
        return CurrencyConfig(
            code = code,
            name = name,
            symbol = symbol,
            decimalPrecision = decimalPrecision,
            exchangeRateToUsd = exchangeRateToUsd,
            thousandsSeparator = thousandsSeparator,
            decimalSeparator = decimalSeparator,
            isDefault = isDefault
        )
    }

    private fun TaxProfileEntity.toModel(): TaxProfile {
        return TaxProfile(
            id = id,
            countryCode = countryCode,
            name = name,
            ratePercentage = ratePercentage,
            taxType = try { TaxType.valueOf(taxType) } catch (_: Exception) { TaxType.EXCLUSIVE },
            isDefault = isDefault,
            taxCode = countryCode
        )
    }

    private fun getDefaultLocales(): List<AppLocale> {
        return listOf(
            AppLocale("en", "English", "English", false, true, "🇺🇸"),
            AppLocale("hi", "Hindi", "हिन्दी", false, false, "🇮🇳"),
            AppLocale("ar", "Arabic", "العربية", true, false, "🇦🇪"),
            AppLocale("fr", "French", "Français", false, false, "🇫🇷"),
            AppLocale("es", "Spanish", "Español", false, false, "🇪🇸"),
            AppLocale("de", "German", "Deutsch", false, false, "🇩🇪"),
            AppLocale("zh", "Chinese", "中文", false, false, "🇨🇳"),
            AppLocale("ja", "Japanese", "日本語", false, false, "🇯🇵")
        )
    }
}
