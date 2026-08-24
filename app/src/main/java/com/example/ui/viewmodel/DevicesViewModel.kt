package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.discovery.DiscoveryState
import com.example.data.discovery.EsMeshDiscoveryService
import com.example.data.model.EsMeshDevice
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DevicesUiState(
    val savedDevices: List<EsMeshDevice> = emptyList(),
    val discoveredDevices: List<EsMeshDevice> = emptyList(),
    val isScanning: Boolean = false,
    val scanMessage: String = "",
    val connectedDeviceId: String? = null,
    val isTestingConnection: Boolean = false,
    val testResult: String? = null,
    val selectedDeviceForDetails: EsMeshDevice? = null
)

class DevicesViewModel(
    private val deviceRepository: DeviceRepository,
    private val connectionRepository: ConnectionRepository,
    private val discoveryService: EsMeshDiscoveryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    val discoveryState: StateFlow<DiscoveryState> = discoveryService.discoveryState

    init {
        viewModelScope.launch {
            deviceRepository.getSavedDevicesFlow(null).collect { saved ->
                _uiState.value = _uiState.value.copy(savedDevices = saved)
            }
        }

        viewModelScope.launch {
            discoveryState.collect { disc ->
                _uiState.value = _uiState.value.copy(
                    discoveredDevices = disc.discoveredDevices,
                    isScanning = disc.isScanning,
                    scanMessage = disc.scanMessage
                )
            }
        }

        viewModelScope.launch {
            connectionRepository.connectedDevice.collect { connected ->
                _uiState.value = _uiState.value.copy(connectedDeviceId = connected?.id)
            }
        }
    }

    fun startDiscovery() {
        discoveryService.startDiscovery()
    }

    fun stopDiscovery() {
        discoveryService.stopDiscovery()
    }

    fun connectDevice(device: EsMeshDevice) {
        connectionRepository.connectDevice(device)
    }

    fun disconnect() {
        connectionRepository.disconnect()
    }

    fun renameDevice(id: String, newName: String) {
        viewModelScope.launch {
            deviceRepository.renameDevice(id, newName)
        }
    }

    fun removeDevice(id: String) {
        viewModelScope.launch {
            deviceRepository.removeDevice(id)
        }
    }

    fun toggleFavorite(device: EsMeshDevice) {
        viewModelScope.launch {
            deviceRepository.setFavorite(device.id, !device.isFavorite)
        }
    }

    fun selectDeviceForDetails(device: EsMeshDevice?) {
        _uiState.value = _uiState.value.copy(selectedDeviceForDetails = device)
    }

    fun testManualConnection(ip: String, httpPort: Int, wsPort: Int, onResult: (Result<EsMeshDevice>) -> Unit) {
        _uiState.value = _uiState.value.copy(isTestingConnection = true, testResult = null)

        viewModelScope.launch {
            val result = connectionRepository.testConnection(ip, httpPort, wsPort)
            result.onSuccess { dev ->
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testResult = "Verified ${dev.model} (${dev.nodeId}) successfully!"
                )
                deviceRepository.saveDevice(dev)
                onResult(Result.success(dev))
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testResult = "Connection failed: ${err.localizedMessage}"
                )
                onResult(Result.failure(err))
            }
        }
    }

    class Factory(
        private val deviceRepo: DeviceRepository,
        private val connectionRepo: ConnectionRepository,
        private val discoveryService: EsMeshDiscoveryService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DevicesViewModel(deviceRepo, connectionRepo, discoveryService) as T
        }
    }
}
