package com.example.features.globalization.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.DatabaseHelper
import com.example.features.globalization.models.AppLocale
import com.example.features.globalization.models.CurrencyConfig
import com.example.features.globalization.models.DateFormatPattern
import com.example.features.globalization.models.NumberFormatStyle
import com.example.features.globalization.models.TaxCalculationResult
import com.example.features.globalization.models.TaxProfile
import com.example.features.globalization.models.TaxType
import com.example.features.globalization.repositories.GlobalizationRepository
import com.example.features.globalization.services.CurrencyService
import com.example.features.globalization.services.FormattingService
import com.example.features.globalization.services.TaxService
import com.example.features.globalization.services.TranslationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GlobalizationUiState(
    val currentLocale: AppLocale = AppLocale("en", "English", "English", false, true, "🇺🇸"),
    val availableLocales: List<AppLocale> = emptyList(),
    val activeCurrency: CurrencyConfig = CurrencyConfig("USD", "US Dollar", "$", 2, 1.0, ",", ".", true),
    val availableCurrencies: List<CurrencyConfig> = emptyList(),
    val activeTaxProfile: TaxProfile = TaxProfile("TAX_IN_GST", "IN", "GST (India)", 18.0, TaxType.EXCLUSIVE, true),
    val availableTaxProfiles: List<TaxProfile> = emptyList(),
    val dateFormat: DateFormatPattern = DateFormatPattern.ISO,
    val is24HourFormat: Boolean = false,
    val isRtl: Boolean = false,
    val missingTranslationsCount: Int = 0,
    val sampleAmount: Double = 12500.50,
    val convertedSampleAmount: Double = 12500.50,
    val sampleTaxResult: TaxCalculationResult? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class GlobalizationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GlobalizationRepository(DatabaseHelper.getInstance(application).globalizationDao)
    private val translationEngine = TranslationEngine()
    private val currencyService = CurrencyService()
    private val formattingService = FormattingService()
    private val taxService = TaxService()

    private val _uiState = MutableStateFlow(GlobalizationUiState())
    val uiState: StateFlow<GlobalizationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.seedInitialDataIfEmpty()

            // Observe Locales
            viewModelScope.launch {
                repository.getActiveLocales().collect { locales ->
                    _uiState.value = _uiState.value.copy(availableLocales = locales)
                }
            }

            // Observe Currencies
            viewModelScope.launch {
                repository.getAllCurrencies().collect { currencies ->
                    _uiState.value = _uiState.value.copy(availableCurrencies = currencies)
                    recalculateSampleValues()
                }
            }

            // Observe Tax Profiles
            viewModelScope.launch {
                repository.getAllTaxProfiles().collect { taxProfiles ->
                    _uiState.value = _uiState.value.copy(availableTaxProfiles = taxProfiles)
                    recalculateSampleValues()
                }
            }

            // Observe Translations for current locale
            viewModelScope.launch {
                repository.getTranslations(_uiState.value.currentLocale.code).collect { list ->
                    val map = list.associate { it.key to it.value }
                    translationEngine.loadTranslations(_uiState.value.currentLocale.code, map)
                    _uiState.value = _uiState.value.copy(
                        missingTranslationsCount = translationEngine.missingTranslationKeys.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectLocale(locale: AppLocale) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentLocale = locale,
                isRtl = locale.isRtl
            )
            repository.getTranslations(locale.code).collect { list ->
                val map = list.associate { it.key to it.value }
                translationEngine.loadTranslations(locale.code, map)
            }
        }
    }

    fun selectCurrency(currency: CurrencyConfig) {
        _uiState.value = _uiState.value.copy(activeCurrency = currency)
        recalculateSampleValues()
    }

    fun selectTaxProfile(taxProfile: TaxProfile) {
        _uiState.value = _uiState.value.copy(activeTaxProfile = taxProfile)
        recalculateSampleValues()
    }

    fun setDateFormat(pattern: DateFormatPattern) {
        _uiState.value = _uiState.value.copy(dateFormat = pattern)
    }

    fun set24HourFormat(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(is24HourFormat = enabled)
    }

    fun setSampleAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(sampleAmount = amount)
        recalculateSampleValues()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateExchangeRate(code: String, newRateToUsd: Double) {
        viewModelScope.launch {
            repository.updateCurrencyRate(code, newRateToUsd)
        }
    }

    fun saveTranslationOverride(key: String, value: String) {
        viewModelScope.launch {
            repository.saveTranslation(_uiState.value.currentLocale.code, key, value)
        }
    }

    private fun recalculateSampleValues() {
        val currentState = _uiState.value
        val converted = currencyService.convertCurrency(
            currentState.sampleAmount,
            CurrencyConfig("USD", "USD", "$", 2, 1.0),
            currentState.activeCurrency
        )
        val taxRes = taxService.calculateTax(currentState.sampleAmount, currentState.activeTaxProfile)

        _uiState.value = currentState.copy(
            convertedSampleAmount = converted,
            sampleTaxResult = taxRes
        )
    }

    fun translate(key: String, params: Map<String, String> = emptyMap()): String {
        return translationEngine.translate(key, _uiState.value.currentLocale.code, "en", params)
    }

    fun formatCurrency(amount: Double): String {
        return currencyService.formatCurrency(amount, _uiState.value.activeCurrency)
    }

    fun formatDate(timestamp: Long): String {
        return formattingService.formatDate(timestamp, _uiState.value.dateFormat)
    }

    fun formatTime(timestamp: Long): String {
        return formattingService.formatTime(timestamp, _uiState.value.is24HourFormat)
    }

    fun formatNumber(value: Double, style: NumberFormatStyle = NumberFormatStyle.STANDARD): String {
        return formattingService.formatNumber(value, style)
    }
}
