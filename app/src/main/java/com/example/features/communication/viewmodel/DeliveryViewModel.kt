package com.example.features.communication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CommunicationLogEntity
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.repositories.DeliveryRepository
import com.example.services.communication.CommunicationEngineService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeliveryUiState(
    val messages: List<CommunicationMessageEntity> = emptyList(),
    val selectedMessageLogs: List<CommunicationLogEntity> = emptyList(),
    val selectedMessageId: Long? = null,
    val statusFilter: String = "ALL",
    val channelFilter: String = "ALL",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val userNotice: String? = null
)

class DeliveryViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val communicationEngine: CommunicationEngineService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deliveryRepository.allMessages.collect { list ->
                _uiState.value = _uiState.value.copy(messages = filterMessages(list))
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshList()
    }

    fun setStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
        refreshList()
    }

    fun setChannelFilter(channel: String) {
        _uiState.value = _uiState.value.copy(channelFilter = channel)
        refreshList()
    }

    fun inspectMessageLogs(messageId: Long) {
        _uiState.value = _uiState.value.copy(selectedMessageId = messageId)
        viewModelScope.launch {
            deliveryRepository.getLogsForMessage(messageId).collect { logs ->
                _uiState.value = _uiState.value.copy(selectedMessageLogs = logs)
            }
        }
    }

    fun retryMessage(context: Context, messageId: Long) {
        viewModelScope.launch {
            communicationEngine.retryFailedMessage(context, messageId)
            _uiState.value = _uiState.value.copy(userNotice = "Manual retry dispatched for message #$messageId")
        }
    }

    private fun refreshList() {
        viewModelScope.launch {
            // Flow will emit updated state
        }
    }

    private fun filterMessages(list: List<CommunicationMessageEntity>): List<CommunicationMessageEntity> {
        val st = _uiState.value.statusFilter
        val ch = _uiState.value.channelFilter
        val q = _uiState.value.searchQuery.lowercase()

        return list.filter { msg ->
            val matchStatus = st == "ALL" || msg.status.equals(st, ignoreCase = true)
            val matchChannel = ch == "ALL" || msg.channel.equals(ch, ignoreCase = true)
            val matchQuery = q.isEmpty() ||
                    msg.recipient.lowercase().contains(q) ||
                    msg.recipientName.lowercase().contains(q) ||
                    msg.subject.lowercase().contains(q) ||
                    msg.body.lowercase().contains(q)
            matchStatus && matchChannel && matchQuery
        }
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(userNotice = null)
    }

    class Factory(
        private val deliveryRepository: DeliveryRepository,
        private val communicationEngine: CommunicationEngineService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeliveryViewModel(deliveryRepository, communicationEngine) as T
        }
    }
}
