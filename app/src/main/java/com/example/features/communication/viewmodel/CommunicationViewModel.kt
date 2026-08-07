package com.example.features.communication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CommunicationAutomationRuleEntity
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.core.database.entity.CommunicationTemplateEntity
import com.example.repositories.CommunicationRepository
import com.example.services.communication.CommunicationEngineService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CommunicationUiState(
    val selectedTab: Int = 0, // 0 = Messages Log, 1 = Templates, 2 = Automation Rules
    val channelFilter: String = "ALL", // "ALL", "WHATSAPP", "EMAIL", "SMS", "PUSH", "TELEGRAM", "SLACK"
    val statusFilter: String = "ALL", // "ALL", "DELIVERED", "PENDING", "FAILED"
    val messages: List<CommunicationMessageEntity> = emptyList(),
    val templates: List<CommunicationTemplateEntity> = emptyList(),
    val automationRules: List<CommunicationAutomationRuleEntity> = emptyList(),
    val isLoading: Boolean = false,
    val userNotice: String? = null
)

data class CommunicationFilterState(
    val tab: Int,
    val channel: String,
    val status: String
)

class CommunicationViewModel(
    private val repository: CommunicationRepository,
    private val communicationEngine: CommunicationEngineService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunicationUiState())
    val uiState: StateFlow<CommunicationUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    private val _channelFilter = MutableStateFlow("ALL")
    private val _statusFilter = MutableStateFlow("ALL")

    init {
        viewModelScope.launch {
            repository.seedDefaultTemplatesIfEmpty()
        }

        viewModelScope.launch {
            val filtersFlow = combine(_selectedTab, _channelFilter, _statusFilter) { tab, channel, status ->
                CommunicationFilterState(tab, channel, status)
            }

            combine(
                filtersFlow,
                repository.allMessages,
                repository.allTemplates,
                repository.allAutomationRules
            ) { filters, msgs, tmpls, rules ->
                val filteredMsgs = msgs.filter { msg ->
                    val matchesChannel = filters.channel == "ALL" || msg.channel.equals(filters.channel, ignoreCase = true)
                    val matchesStatus = filters.status == "ALL" || msg.status.equals(filters.status, ignoreCase = true)
                    matchesChannel && matchesStatus
                }
                CommunicationUiState(
                    selectedTab = filters.tab,
                    channelFilter = filters.channel,
                    statusFilter = filters.status,
                    messages = filteredMsgs,
                    templates = tmpls,
                    automationRules = rules,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setChannelFilter(channel: String) {
        _channelFilter.value = channel
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun sendQuickMessage(
        context: Context,
        channel: String,
        recipient: String,
        recipientName: String,
        subject: String,
        body: String
    ) {
        viewModelScope.launch {
            try {
                communicationEngine.sendDirectMessage(
                    context = context,
                    channel = channel,
                    recipient = recipient,
                    recipientName = recipientName,
                    subject = subject,
                    body = body,
                    relatedEntityType = "DIRECT"
                )
                _uiState.value = _uiState.value.copy(userNotice = "Message dispatched to $recipient via $channel")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(userNotice = "Error dispatching message: ${e.message}")
            }
        }
    }

    fun sendTemplatedMessage(
        context: Context,
        templateId: String,
        channel: String?,
        recipient: String,
        recipientName: String,
        variables: Map<String, String>
    ) {
        viewModelScope.launch {
            try {
                val id = communicationEngine.sendTemplatedMessage(
                    context = context,
                    templateId = templateId,
                    channel = channel,
                    recipient = recipient,
                    recipientName = recipientName,
                    variables = variables,
                    relatedEntityType = "TEMPLATED"
                )
                if (id != null) {
                    _uiState.value = _uiState.value.copy(userNotice = "Templated message sent successfully!")
                } else {
                    _uiState.value = _uiState.value.copy(userNotice = "Template $templateId not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(userNotice = "Error: ${e.message}")
            }
        }
    }

    fun retryMessage(context: Context, messageId: Long) {
        viewModelScope.launch {
            communicationEngine.retryFailedMessage(context, messageId)
            _uiState.value = _uiState.value.copy(userNotice = "Retry initiated for message #$messageId")
        }
    }

    fun toggleAutomationRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutomationRule(ruleId, isEnabled)
            _uiState.value = _uiState.value.copy(userNotice = "Automation rule updated")
        }
    }

    fun saveTemplate(template: CommunicationTemplateEntity) {
        viewModelScope.launch {
            repository.saveTemplate(template)
            _uiState.value = _uiState.value.copy(userNotice = "Template saved successfully")
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            _uiState.value = _uiState.value.copy(userNotice = "Template deleted")
        }
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(userNotice = null)
    }

    class Factory(
        private val repository: CommunicationRepository,
        private val communicationEngine: CommunicationEngineService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CommunicationViewModel(repository, communicationEngine) as T
        }
    }
}
