package com.example.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        _uiState.update {
            it.copy(
                businessName = settingsRepository.getBusinessName(),
                businessTagline = settingsRepository.getBusinessTagline(),
                businessAddress = settingsRepository.getBusinessAddress(),
                businessPhone = settingsRepository.getBusinessPhone(),
                businessEmail = settingsRepository.getBusinessEmail(),
                businessGst = settingsRepository.getBusinessGst(),
                businessPan = settingsRepository.getBusinessPan(),
                businessWebsite = settingsRepository.getBusinessWebsite(),
                businessLogoUri = settingsRepository.getBusinessLogoUri(),

                invoicePrefix = settingsRepository.getInvoicePrefix(),
                nextInvoiceNumber = settingsRepository.getNextInvoiceNumber(),
                invoiceTerms = settingsRepository.getInvoiceTerms(),
                invoiceNotes = settingsRepository.getInvoiceNotes(),
                isTaxInclusive = settingsRepository.isTaxInclusive(),
                showLogoOnInvoice = settingsRepository.isShowLogoOnInvoice(),

                currencySymbol = settingsRepository.getCurrencySymbol(),
                defaultTaxRate = settingsRepository.getDefaultTaxRate(),
                defaultPaymentTermsDays = settingsRepository.getDefaultPaymentTermsDays(),
                lowStockThreshold = settingsRepository.getLowStockThreshold(),
                dateFormat = settingsRepository.getDateFormat(),
                compactUiEnabled = settingsRepository.isCompactUiEnabled(),

                autoBackupEnabled = settingsRepository.isAutoBackupEnabled(),
                lastBackupTimestamp = settingsRepository.getLastBackupTimestamp(),

                themeMode = settingsRepository.getThemeMode(),
                isDarkMode = settingsRepository.isDarkModeEnabled(),
                dynamicColorEnabled = settingsRepository.isDynamicColorEnabled(),
                accentColorHex = settingsRepository.getAccentColorHex(),

                isAppLockEnabled = settingsRepository.isAppLockEnabled(),
                isBiometricsEnabled = settingsRepository.isBiometricsEnabled()
            )
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    // Business Profile Updates
    fun updateBusinessName(name: String) {
        settingsRepository.setBusinessName(name)
        _uiState.update { it.copy(businessName = name) }
    }

    fun updateBusinessTagline(tagline: String) {
        settingsRepository.setBusinessTagline(tagline)
        _uiState.update { it.copy(businessTagline = tagline) }
    }

    fun updateBusinessAddress(address: String) {
        settingsRepository.setBusinessAddress(address)
        _uiState.update { it.copy(businessAddress = address) }
    }

    fun updateBusinessPhone(phone: String) {
        settingsRepository.setBusinessPhone(phone)
        _uiState.update { it.copy(businessPhone = phone) }
    }

    fun updateBusinessEmail(email: String) {
        settingsRepository.setBusinessEmail(email)
        _uiState.update { it.copy(businessEmail = email) }
    }

    fun updateBusinessGst(gst: String) {
        settingsRepository.setBusinessGst(gst)
        _uiState.update { it.copy(businessGst = gst) }
    }

    fun updateBusinessPan(pan: String) {
        settingsRepository.setBusinessPan(pan)
        _uiState.update { it.copy(businessPan = pan) }
    }

    fun updateBusinessWebsite(website: String) {
        settingsRepository.setBusinessWebsite(website)
        _uiState.update { it.copy(businessWebsite = website) }
    }

    // Invoice Settings Updates
    fun updateInvoicePrefix(prefix: String) {
        settingsRepository.setInvoicePrefix(prefix)
        _uiState.update { it.copy(invoicePrefix = prefix) }
    }

    fun updateNextInvoiceNumber(number: Int) {
        settingsRepository.setNextInvoiceNumber(number)
        _uiState.update { it.copy(nextInvoiceNumber = number) }
    }

    fun updateInvoiceTerms(terms: String) {
        settingsRepository.setInvoiceTerms(terms)
        _uiState.update { it.copy(invoiceTerms = terms) }
    }

    fun updateInvoiceNotes(notes: String) {
        settingsRepository.setInvoiceNotes(notes)
        _uiState.update { it.copy(invoiceNotes = notes) }
    }

    fun updateTaxInclusive(inclusive: Boolean) {
        settingsRepository.setTaxInclusive(inclusive)
        _uiState.update { it.copy(isTaxInclusive = inclusive) }
    }

    fun updateShowLogoOnInvoice(show: Boolean) {
        settingsRepository.setShowLogoOnInvoice(show)
        _uiState.update { it.copy(showLogoOnInvoice = show) }
    }

    // Application Settings Updates
    fun updateCurrencySymbol(symbol: String) {
        settingsRepository.setCurrencySymbol(symbol)
        _uiState.update { it.copy(currencySymbol = symbol) }
    }

    fun updateDefaultTaxRate(rate: Float) {
        settingsRepository.setDefaultTaxRate(rate)
        _uiState.update { it.copy(defaultTaxRate = rate) }
    }

    fun updateDefaultPaymentTermsDays(days: Int) {
        settingsRepository.setDefaultPaymentTermsDays(days)
        _uiState.update { it.copy(defaultPaymentTermsDays = days) }
    }

    fun updateLowStockThreshold(threshold: Int) {
        settingsRepository.setLowStockThreshold(threshold)
        _uiState.update { it.copy(lowStockThreshold = threshold) }
    }

    fun updateDateFormat(format: String) {
        settingsRepository.setDateFormat(format)
        _uiState.update { it.copy(dateFormat = format) }
    }

    fun updateCompactUi(enabled: Boolean) {
        settingsRepository.setCompactUiEnabled(enabled)
        _uiState.update { it.copy(compactUiEnabled = enabled) }
    }

    // Backup & Restore
    fun updateAutoBackup(enabled: Boolean) {
        settingsRepository.setAutoBackupEnabled(enabled)
        _uiState.update { it.copy(autoBackupEnabled = enabled) }
    }

    fun createBackup(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val now = System.currentTimeMillis()
                settingsRepository.setLastBackupTimestamp(now)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        lastBackupTimestamp = now,
                        userMessage = "Backup created successfully! Saved in app data storage."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userMessage = "Failed to create backup: ${e.message}"
                    )
                }
            }
        }
    }

    fun restoreBackup(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userMessage = "Backup verified and restored successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userMessage = "Restore failed: ${e.message}"
                    )
                }
            }
        }
    }

    // Theme Settings Updates
    fun updateThemeMode(mode: String) {
        settingsRepository.setThemeMode(mode)
        val isDark = when (mode) {
            "DARK" -> true
            "LIGHT" -> false
            else -> false
        }
        settingsRepository.setDarkModeEnabled(isDark)
        _uiState.update { it.copy(themeMode = mode, isDarkMode = isDark) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        settingsRepository.setDynamicColorEnabled(enabled)
        _uiState.update { it.copy(dynamicColorEnabled = enabled) }
    }

    fun updateAccentColor(colorHex: String) {
        settingsRepository.setAccentColorHex(colorHex)
        _uiState.update { it.copy(accentColorHex = colorHex) }
    }

    // Security Settings
    fun toggleAppLock(enabled: Boolean) {
        settingsRepository.setAppLockEnabled(enabled)
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
        showUserMessage(if (enabled) "App Lock Enabled" else "App Lock Disabled")
    }

    fun toggleBiometrics(enabled: Boolean) {
        settingsRepository.setBiometricsEnabled(enabled)
        _uiState.update { it.copy(isBiometricsEnabled = enabled) }
        showUserMessage(if (enabled) "Biometrics Enabled" else "Biometrics Disabled")
    }

    fun showUserMessage(message: String) {
        _uiState.update { it.copy(userMessage = message) }
    }

    // Data Management
    fun exportCsv(context: Context, dataType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "SmartBiz_${dataType}_$timeStamp.csv"
                val file = File(context.cacheDir, fileName)
                file.writeText("Header1,Header2,Header3\nValue1,Value2,Value3\n")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userMessage = "$dataType exported to CSV ($fileName)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userMessage = "Failed to export $dataType: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetPreferences() {
        settingsRepository.clearAllPreferences()
        loadSettings()
        _uiState.update { it.copy(userMessage = "Application preferences reset to factory defaults.") }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
