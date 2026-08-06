package com.example.features.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PrivacySettingsEntity
import com.example.repositories.AppRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrivacySettingsUiState(
    val isLoading: Boolean = true,
    val settings: PrivacySettingsEntity = PrivacySettingsEntity(),
    val isSavedMessageVisible: Boolean = false,
    val errorMessage: String? = null
)

class PrivacySettingsViewModel : ViewModel() {
    private val appProvider = AppRepositoryProvider.getInstance()
    private val privacyService = appProvider.privacyService
    private val privacyRepository = appProvider.privacyRepository

    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                privacyRepository.getPrivacySettingsFlow("DEFAULT_USER").collect { settings ->
                    val currentSettings = settings ?: PrivacySettingsEntity()
                    _uiState.value = PrivacySettingsUiState(
                        isLoading = false,
                        settings = currentSettings
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun updateSettings(updatedSettings: PrivacySettingsEntity) {
        viewModelScope.launch {
            try {
                privacyService.updatePrivacySettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    settings = updatedSettings,
                    isSavedMessageVisible = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun dismissSavedMessage() {
        _uiState.value = _uiState.value.copy(isSavedMessageVisible = false)
    }
}
