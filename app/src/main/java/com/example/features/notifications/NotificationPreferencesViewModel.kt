package com.example.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.NotificationPreferenceEntity
import com.example.services.NotificationPreferenceService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationPreferencesViewModel(
    private val preferenceService: NotificationPreferenceService
) : ViewModel() {

    val preferences: StateFlow<List<NotificationPreferenceEntity>> = preferenceService.allPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferenceService.initializeDefaultPreferencesIfEmpty()
        }
    }

    fun togglePreference(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            preferenceService.togglePreference(key, isEnabled)
        }
    }

    class Factory(
        private val preferenceService: NotificationPreferenceService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationPreferencesViewModel(preferenceService) as T
        }
    }
}
