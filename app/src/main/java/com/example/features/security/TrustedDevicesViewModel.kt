package com.example.features.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DeviceEntity
import com.example.repositories.AppRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrustedDevicesUiState(
    val isLoading: Boolean = true,
    val devices: List<DeviceEntity> = emptyList(),
    val selectedDeviceForDetails: DeviceEntity? = null,
    val isRenameDialogOpen: Boolean = false,
    val deviceToRename: DeviceEntity? = null,
    val renameInputText: String = "",
    val message: String? = null,
    val errorMessage: String? = null
)

class TrustedDevicesViewModel : ViewModel() {
    private val appProvider = AppRepositoryProvider.getInstance()
    private val trustedDeviceRepository = appProvider.trustedDeviceRepository
    private val trustedDeviceService = appProvider.trustedDeviceService

    private val _uiState = MutableStateFlow(TrustedDevicesUiState())
    val uiState: StateFlow<TrustedDevicesUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Ensure local device exists
                val current = trustedDeviceRepository.getDeviceById("LOCAL_DEVICE")
                if (current == null) {
                    trustedDeviceRepository.registerOrUpdateDevice(
                        DeviceEntity(
                            deviceId = "LOCAL_DEVICE",
                            userId = "DEFAULT_USER",
                            deviceName = "Primary Android Device",
                            androidVersion = "Android 14 (API 34)",
                            appVersion = "1.0.0",
                            platform = "Android",
                            isTrusted = true,
                            isCurrentDevice = true
                        )
                    )
                }

                trustedDeviceRepository.getAllDevicesFlow().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        devices = list
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

    fun toggleTrustStatus(device: DeviceEntity) {
        viewModelScope.launch {
            try {
                if (device.isTrusted) {
                    trustedDeviceService.untrustDevice(device.deviceId)
                    _uiState.value = _uiState.value.copy(message = "${device.deviceName} marked untrusted.")
                } else {
                    trustedDeviceService.trustDevice(device.deviceId)
                    _uiState.value = _uiState.value.copy(message = "${device.deviceName} marked trusted.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun openRenameDialog(device: DeviceEntity) {
        _uiState.value = _uiState.value.copy(
            isRenameDialogOpen = true,
            deviceToRename = device,
            renameInputText = device.deviceName
        )
    }

    fun closeRenameDialog() {
        _uiState.value = _uiState.value.copy(
            isRenameDialogOpen = false,
            deviceToRename = null,
            renameInputText = ""
        )
    }

    fun onRenameInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(renameInputText = text)
    }

    fun confirmRename() {
        val device = _uiState.value.deviceToRename ?: return
        val newName = _uiState.value.renameInputText.trim()
        if (newName.isBlank()) return

        viewModelScope.launch {
            try {
                trustedDeviceService.renameDevice(device.deviceId, newName)
                closeRenameDialog()
                _uiState.value = _uiState.value.copy(message = "Device renamed to $newName")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun removeDevice(device: DeviceEntity) {
        viewModelScope.launch {
            try {
                trustedDeviceService.removeDevice(device.deviceId)
                _uiState.value = _uiState.value.copy(message = "${device.deviceName} removed.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun requestRemoteApproval(device: DeviceEntity) {
        viewModelScope.launch {
            try {
                trustedDeviceService.requestRemoteApproval(device.deviceId, "Enterprise Admin Remote Approval Request")
                _uiState.value = _uiState.value.copy(message = "Remote approval request dispatched for ${device.deviceName}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun selectDeviceForDetails(device: DeviceEntity?) {
        _uiState.value = _uiState.value.copy(selectedDeviceForDetails = device)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, errorMessage = null)
    }
}
