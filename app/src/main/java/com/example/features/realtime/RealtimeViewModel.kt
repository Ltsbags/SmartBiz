package com.example.features.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PresenceEntity
import com.example.core.database.entity.RealtimeEventEntity
import com.example.core.realtime.ConnectionState
import com.example.core.realtime.RealtimeEvent
import com.example.repositories.PresenceRepository
import com.example.repositories.RealtimeRepository
import com.example.services.ConnectionHealth
import com.example.services.ConnectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RealtimeUiState(
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val connectionHealth: ConnectionHealth = ConnectionHealth(),
    val onlineCount: Int = 0,
    val onlineUsers: List<PresenceEntity> = emptyList(),
    val recentEvents: List<RealtimeEventEntity> = emptyList(),
    val selectedModuleFilter: String = "ALL",
    val isSimulating: Boolean = false
)

class RealtimeViewModel(
    private val realtimeRepository: RealtimeRepository,
    private val presenceRepository: PresenceRepository,
    private val connectionService: ConnectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RealtimeUiState())
    val uiState: StateFlow<RealtimeUiState> = _uiState.asStateFlow()

    init {
        // Collect Connection State
        viewModelScope.launch {
            realtimeRepository.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }

        // Collect Connection Health
        viewModelScope.launch {
            connectionService.connectionHealth.collect { health ->
                _uiState.value = _uiState.value.copy(connectionHealth = health)
            }
        }

        // Collect Online Users Count
        viewModelScope.launch {
            presenceRepository.onlineCountFlow.collect { count ->
                _uiState.value = _uiState.value.copy(onlineCount = count)
            }
        }

        // Collect Online Users
        viewModelScope.launch {
            presenceRepository.onlineUsersFlow.collect { users ->
                _uiState.value = _uiState.value.copy(onlineUsers = users)
            }
        }

        // Collect Historical & Live Events from DB
        viewModelScope.launch {
            realtimeRepository.allHistoricalEventsFlow.collect { events ->
                _uiState.value = _uiState.value.copy(recentEvents = events)
            }
        }

        // Start session on view model init
        realtimeRepository.startRealtimeService()
    }

    fun setModuleFilter(module: String) {
        _uiState.value = _uiState.value.copy(selectedModuleFilter = module)
    }

    fun updateUserStatus(status: String, customMessage: String) {
        viewModelScope.launch {
            presenceRepository.updatePresence(
                userId = "USER_PRIMARY",
                userName = "Alex Dev (You)",
                newStatus = status,
                customStatus = customMessage
            )
        }
    }

    fun reconnectRealtime() {
        connectionService.triggerManualReconnect()
    }

    fun simulateLiveInvoiceEvent() {
        val invoiceNumber = "INV-${(1000..9999).random()}"
        val amount = (500..15000).random().toDouble()
        realtimeRepository.broadcastLocalRealtimeEvent(
            RealtimeEvent.InvoiceCreated(
                id = "INV_EVENT_${System.currentTimeMillis()}",
                invoiceNumber = invoiceNumber,
                customerName = "Global Tech Corp",
                totalAmount = amount,
                createdBy = "Alex Dev"
            )
        )
    }

    fun simulateLiveStockEvent() {
        val newStock = (0..12).random()
        realtimeRepository.broadcastLocalRealtimeEvent(
            RealtimeEvent.StockChanged(
                productId = "PROD_${(100..999).random()}",
                productName = "Wireless Laser Scanner",
                previousStock = newStock + 15,
                newStock = newStock,
                reason = "Realtime POS Sale Sync"
            )
        )
    }

    fun simulateSecurityAlert() {
        realtimeRepository.broadcastLocalRealtimeEvent(
            RealtimeEvent.SecurityAlert(
                alertId = "SEC_${System.currentTimeMillis()}",
                alertTitle = "Unrecognized Device Login",
                message = "New login attempt from Chrome/macOS in Tokyo, Japan",
                alertSeverity = "HIGH"
            )
        )
    }

    class Factory(
        private val realtimeRepository: RealtimeRepository,
        private val presenceRepository: PresenceRepository,
        private val connectionService: ConnectionService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RealtimeViewModel(realtimeRepository, presenceRepository, connectionService) as T
        }
    }
}
