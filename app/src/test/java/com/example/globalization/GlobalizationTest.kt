package com.example.globalization

import com.example.features.globalization.models.CurrencyConfig
import com.example.features.globalization.models.DateFormatPattern
import com.example.features.globalization.models.NumberFormatStyle
import com.example.features.globalization.models.TaxProfile
import com.example.features.globalization.models.TaxType
import com.example.features.globalization.services.CurrencyService
import com.example.features.globalization.services.FormattingService
import com.example.features.globalization.services.TaxService
import com.example.features.globalization.services.TranslationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GlobalizationTest {

    private lateinit var translationEngine: TranslationEngine
    private lateinit var currencyService: CurrencyService
    private lateinit var formattingService: FormattingService
    private lateinit var taxService: TaxService

    @Before
    fun setUp() {
        translationEngine = TranslationEngine()
        currencyService = CurrencyService()
        formattingService = FormattingService()
        taxService = TaxService()

        // Load translations
        translationEngine.loadTranslations("en", mapOf(
            "welcome" to "Welcome, {name}!",
            "items_count" to "0 items|1 item|{count} items",
            "invoice_title" to "Tax Invoice"
        ))
        translationEngine.loadTranslations("ar", mapOf(
            "welcome" to "مرحباً، {name}!",
            "invoice_title" to "فاتورة ضريبية"
        ))
    }

    @Test
    fun testTranslationLookupAndFallback() {
        // Arabic existing key
        val arTitle = translationEngine.translate("invoice_title", currentLocale = "ar", fallbackLocale = "en")
        assertEquals("فاتورة ضريبية", arTitle)

        // Missing key in Arabic, falling back to English
        val fallbackResult = translationEngine.translate("items_count", currentLocale = "ar", fallbackLocale = "en", count = 5)
        assertEquals("5 items", fallbackResult)

        // Fully missing key auto-formatted
        val missingResult = translationEngine.translate("unknown_key_code", currentLocale = "ar", fallbackLocale = "en")
        assertEquals("Unknown Key Code", missingResult)
        assertTrue(translationEngine.missingTranslationKeys.contains("ar:unknown_key_code"))
    }

    @Test
    fun testTranslationParameterizedAndPlurals() {
        val paramResult = translationEngine.translate("welcome", currentLocale = "en", params = mapOf("name" to "John"))
        assertEquals("Welcome, John!", paramResult)

        val zeroPlural = translationEngine.translate("items_count", currentLocale = "en", count = 0)
        assertEquals("0 items", zeroPlural)

        val singularPlural = translationEngine.translate("items_count", currentLocale = "en", count = 1)
        assertEquals("1 item", singularPlural)

        val manyPlural = translationEngine.translate("items_count", currentLocale = "en", count = 10)
        assertEquals("10 items", manyPlural)
    }

    @Test
    fun testCurrencyFormattingAndConversions() {
        val usd = CurrencyConfig("USD", "US Dollar", "$", 2, 1.0, ",", ".", true)
        val inr = CurrencyConfig("INR", "Indian Rupee", "₹", 2, 0.012, ",", ".", false)
        val eur = CurrencyConfig("EUR", "Euro", "€", 2, 1.08, ".", ",", false)

        val formattedUsd = currencyService.formatCurrency(1250.5, usd)
        assertEquals("$1,250.50", formattedUsd)

        val formattedAccountingNegative = currencyService.formatCurrency(-500.0, usd, useAccountingFormat = true)
        assertEquals("($500.00)", formattedAccountingNegative)

        val convertedAmount = currencyService.convertCurrency(100.0, usd, inr)
        assertEquals(8333.3333, convertedAmount, 0.01)
    }

    @Test
    fun testRegionalFormatting() {
        val timestamp = 1700000000000L // Sample epoch

        val isoDate = formattingService.formatDate(timestamp, DateFormatPattern.ISO)
        assertTrue(isoDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val percentFormatted = formattingService.formatNumber(0.18, NumberFormatStyle.PERCENTAGE)
        assertEquals("18.00%", percentFormatted)

        val accountingFormatted = formattingService.formatNumber(-150.0, NumberFormatStyle.ACCOUNTING)
        assertEquals("(150.00)", accountingFormatted)
    }

    @Test
    fun testTaxCalculations() {
        val gstExclusive = TaxProfile("TAX_IN", "IN", "GST", 18.0, TaxType.EXCLUSIVE)
        val gstRes = taxService.calculateTax(1000.0, gstExclusive)
        assertEquals(1000.0, gstRes.netAmount, 0.01)
        assertEquals(180.0, gstRes.taxAmount, 0.01)
        assertEquals(1180.0, gstRes.grossAmount, 0.01)

        val vatInclusive = TaxProfile("TAX_EU", "DE", "VAT", 19.0, TaxType.INCLUSIVE)
        val vatRes = taxService.calculateTax(1190.0, vatInclusive)
        assertEquals(1000.0, vatRes.netAmount, 0.01)
        assertEquals(190.0, vatRes.taxAmount, 0.01)
        assertEquals(1190.0, vatRes.grossAmount, 0.01)
    }
}
