package com.example.features.usermanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DeviceEntity
import com.example.repositories.DeviceRepository
import com.example.repositories.UserRepository
import com.example.services.DeviceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceUiState(
    val currentDevice: DeviceEntity? = null,
    val devices: List<DeviceEntity> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class DeviceViewModel(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val deviceService: DeviceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        initializeDevices()
    }

    fun initializeDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = userRepository.getPrimaryUser()
            if (user != null) {
                val curDevice = deviceService.getCurrentDevice(user.userId)
                deviceRepository.registerOrUpdateDevice(curDevice)

                _uiState.update { it.copy(currentDevice = curDevice) }

                deviceRepository.getDevicesForUser(user.userId).collect { deviceList ->
                    _uiState.update { state ->
                        state.copy(
                            devices = deviceList,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleDeviceTrust(deviceId: String, isTrusted: Boolean) {
        viewModelScope.launch {
            deviceRepository.setDeviceTrustStatus(deviceId, isTrusted)
            _uiState.update {
                it.copy(message = if (isTrusted) "Device marked as trusted" else "Device marked as standard")
            }
        }
    }

    fun revokeDevice(deviceId: String) {
        viewModelScope.launch {
            deviceRepository.removeDevice(deviceId)
            _uiState.update { it.copy(message = "Device access revoked successfully") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    class Factory(
        private val deviceRepository: DeviceRepository,
        private val userRepository: UserRepository,
        private val deviceService: DeviceService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(deviceRepository, userRepository, deviceService) as T
        }
    }
}
