package com.example.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.ReminderEntity
import com.example.repositories.ReminderRepository
import com.example.services.ReminderService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderManagementViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderService: ReminderService
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository.allRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createReminder(
        title: String,
        description: String,
        module: String = "CUSTOM",
        repeatType: String = "NONE",
        triggerTime: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            reminderService.createCustomReminder(title, description, module, repeatType, triggerTime)
        }
    }

    fun toggleReminder(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            reminderService.toggleReminder(id, isEnabled)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderService.deleteReminder(id)
        }
    }

    fun triggerAutoEvaluation() {
        viewModelScope.launch {
            reminderService.evaluateAndGenerateAutoReminders()
        }
    }

    class Factory(
        private val reminderRepository: ReminderRepository,
        private val reminderService: ReminderService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderManagementViewModel(reminderRepository, reminderService) as T
        }
    }
}
