package com.example.features.globalization.services

import com.example.features.globalization.models.CurrencyConfig
import com.example.features.globalization.models.DateFormatPattern
import com.example.features.globalization.models.NumberFormatStyle
import com.example.features.globalization.models.TaxCalculationResult
import com.example.features.globalization.models.TaxProfile
import com.example.features.globalization.models.TaxType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TranslationEngine {
    private val translationMap = mutableMapOf<String, MutableMap<String, String>>()
    val missingTranslationKeys = mutableSetOf<String>()

    fun loadTranslations(localeCode: String, translations: Map<String, String>) {
        val map = translationMap.getOrPut(localeCode) { mutableMapOf() }
        map.putAll(translations)
    }

    fun translate(
        key: String,
        currentLocale: String = "en",
        fallbackLocale: String = "en",
        params: Map<String, String> = emptyMap(),
        count: Int? = null
    ): String {
        var template: String = translationMap[currentLocale]?.get(key)
            ?: translationMap[fallbackLocale]?.get(key)
            ?: run {
                missingTranslationKeys.add("$currentLocale:$key")
                key.replace("_", " ").capitalizeWords()
            }

        // Pluralization handling
        if (count != null && template.contains("|")) {
            val parts = template.split("|")
            template = when {
                count == 0 && parts.size >= 3 -> parts[0]
                count == 1 && parts.size >= 2 -> parts[1]
                else -> parts.last()
            }
        }

        // Parameter substitution
        var result = template
        params.forEach { (paramKey, paramVal) ->
            result = result.replace("{$paramKey}", paramVal)
        }
        if (count != null) {
            result = result.replace("{count}", count.toString())
        }

        return result
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}

class CurrencyService {
    fun formatCurrency(
        amount: Double,
        currency: CurrencyConfig,
        useAccountingFormat: Boolean = false
    ): String {
        val absAmount = Math.abs(amount)
        val decimalFormat = StringBuilder("#,##0")
        if (currency.decimalPrecision > 0) {
            decimalFormat.append(".")
            for (i in 1..currency.decimalPrecision) {
                decimalFormat.append("0")
            }
        }

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = currency.thousandsSeparator.firstOrNull() ?: ','
            decimalSeparator = currency.decimalSeparator.firstOrNull() ?: '.'
        }

        val df = DecimalFormat(decimalFormat.toString(), symbols)
        val formattedNumber = df.format(absAmount)

        val resultStr = "${currency.symbol}$formattedNumber"

        return if (amount < 0) {
            if (useAccountingFormat) "($resultStr)" else "-$resultStr"
        } else {
            resultStr
        }
    }

    fun convertCurrency(
        amount: Double,
        fromCurrency: CurrencyConfig,
        toCurrency: CurrencyConfig
    ): Double {
        if (fromCurrency.code == toCurrency.code) return amount
        // Convert from source to USD then USD to target
        val amountInUsd = amount * fromCurrency.exchangeRateToUsd
        return amountInUsd / toCurrency.exchangeRateToUsd
    }
}

class FormattingService {
    fun formatDate(
        timestamp: Long,
        pattern: DateFormatPattern = DateFormatPattern.ISO,
        timeZoneId: String = "UTC"
    ): String {
        val sdf = SimpleDateFormat(pattern.pattern, Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone(timeZoneId)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(
        timestamp: Long,
        is24Hour: Boolean = false,
        timeZoneId: String = "UTC"
    ): String {
        val pattern = if (is24Hour) "HH:mm:ss" else "hh:mm:ss a"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone(timeZoneId)
        return sdf.format(Date(timestamp))
    }

    fun formatNumber(
        value: Double,
        style: NumberFormatStyle = NumberFormatStyle.STANDARD,
        decimalPlaces: Int = 2
    ): String {
        return when (style) {
            NumberFormatStyle.STANDARD -> String.format(Locale.US, "%.${decimalPlaces}f", value)
            NumberFormatStyle.PERCENTAGE -> String.format(Locale.US, "%.${decimalPlaces}f%%", value * 100)
            NumberFormatStyle.ACCOUNTING -> if (value < 0) "(${String.format(Locale.US, "%.${decimalPlaces}f", Math.abs(value))})" else String.format(Locale.US, "%.${decimalPlaces}f", value)
            NumberFormatStyle.SCIENTIFIC -> String.format(Locale.US, "%E", value)
        }
    }
}

class TaxService {
    fun calculateTax(
        amount: Double,
        taxProfile: TaxProfile
    ): TaxCalculationResult {
        val rate = taxProfile.ratePercentage / 100.0
        return when (taxProfile.taxType) {
            TaxType.EXCLUSIVE -> {
                val tax = amount * rate
                TaxCalculationResult(
                    netAmount = amount,
                    taxAmount = tax,
                    grossAmount = amount + tax,
                    taxName = taxProfile.name,
                    ratePercentage = taxProfile.ratePercentage,
                    taxType = taxProfile.taxType
                )
            }
            TaxType.INCLUSIVE -> {
                val net = amount / (1.0 + rate)
                val tax = amount - net
                TaxCalculationResult(
                    netAmount = net,
                    taxAmount = tax,
                    grossAmount = amount,
                    taxName = taxProfile.name,
                    ratePercentage = taxProfile.ratePercentage,
                    taxType = taxProfile.taxType
                )
            }
            TaxType.COMPOUND -> {
                val tax = amount * rate
                TaxCalculationResult(
                    netAmount = amount,
                    taxAmount = tax,
                    grossAmount = amount + tax,
                    taxName = taxProfile.name,
                    ratePercentage = taxProfile.ratePercentage,
                    taxType = taxProfile.taxType
                )
            }
        }
    }
}
