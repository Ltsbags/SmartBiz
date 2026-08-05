package com.example.features.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.BusinessHealthEntity
import com.example.services.BusinessHealthService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessHealthViewModel(
    private val businessHealthService: BusinessHealthService
) : ViewModel() {

    val healthRecord: StateFlow<BusinessHealthEntity?> = businessHealthService.latestHealthFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthHistory: StateFlow<List<BusinessHealthEntity>> = businessHealthService.healthHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recalculateHealth() {
        viewModelScope.launch {
            businessHealthService.calculateAndStoreBusinessHealth()
        }
    }

    class Factory(
        private val businessHealthService: BusinessHealthService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BusinessHealthViewModel(businessHealthService) as T
        }
    }
}
