package com.example.features.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DashboardWidgetEntity
import com.example.services.WidgetEngineService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WidgetCustomizerViewModel(
    private val widgetEngineService: WidgetEngineService
) : ViewModel() {

    val allWidgets: StateFlow<List<DashboardWidgetEntity>> = widgetEngineService.allWidgetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleWidget(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            widgetEngineService.toggleWidget(key, isEnabled)
        }
    }

    fun togglePin(key: String, isPinned: Boolean) {
        viewModelScope.launch {
            widgetEngineService.togglePin(key, isPinned)
        }
    }

    fun reorderWidgets(widgetKeysInOrder: List<String>) {
        viewModelScope.launch {
            widgetEngineService.reorderWidgets(widgetKeysInOrder)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            widgetEngineService.initializeDefaultWidgetsIfEmpty()
        }
    }

    class Factory(
        private val widgetEngineService: WidgetEngineService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WidgetCustomizerViewModel(widgetEngineService) as T
        }
    }
}
