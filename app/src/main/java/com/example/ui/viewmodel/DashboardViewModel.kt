package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.discovery.EsMeshDiscoveryService
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.EsMeshDevice
import com.example.data.network.WsConnectionState
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val connectedDevice: EsMeshDevice? = null,
    val isConnecting: Boolean = false,
    val wsState: WsConnectionState = WsConnectionState.Disconnected,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val connectionRepository: ConnectionRepository,
    private val deviceRepository: DeviceRepository,
    private val discoveryService: EsMeshDiscoveryService
) : ViewModel() {

    val connectedDevice: StateFlow<EsMeshDevice?> = connectionRepository.connectedDevice
    val wsState: StateFlow<WsConnectionState> = connectionRepository.wsState
    val isConnecting: StateFlow<Boolean> = connectionRepository.isConnecting
    val lastError: StateFlow<String?> = connectionRepository.lastError

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun disconnect() {
        connectionRepository.disconnect()
    }

    fun startDiscovery() {
        discoveryService.startDiscovery()
    }

    fun connectDevice(device: EsMeshDevice) {
        connectionRepository.connectDevice(device)
    }

    class Factory(
        private val connectionRepo: ConnectionRepository,
        private val deviceRepo: DeviceRepository,
        private val discoveryService: EsMeshDiscoveryService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(connectionRepo, deviceRepo, discoveryService) as T
        }
    }
}
