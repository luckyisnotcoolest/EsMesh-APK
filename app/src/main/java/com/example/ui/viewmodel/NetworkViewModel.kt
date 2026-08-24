package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.EsMeshNetworkTopology
import com.example.data.network.NetworkResponse
import com.example.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NetworkUiState(
    val topology: EsMeshNetworkTopology = EsMeshNetworkTopology(),
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val selectedNodeId: String? = null
)

class NetworkViewModel(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.connectedDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(isConnected = dev != null)
                if (dev != null) {
                    refreshTopology(dev.ipAddress, dev.httpPort)
                }
            }
        }
    }

    fun refreshTopology(ip: String? = null, port: Int = 80) {
        val targetIp = ip ?: connectionRepository.connectedDevice.value?.ipAddress
        if (targetIp == null) {
            _uiState.value = _uiState.value.copy(
                topology = EsMeshNetworkTopology(),
                isConnected = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val res = connectionRepository.httpClient.getNetworkTopology(targetIp, port)) {
                is NetworkResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        topology = res.data,
                        isLoading = false
                    )
                }
                is NetworkResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Topology sync error: ${res.message}"
                    )
                }
                is NetworkResponse.Failure -> {
                    // Fallback to local default topology if device connection is offline
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Using cached topology: ${res.exception.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectNode(nodeId: String?) {
        _uiState.value = _uiState.value.copy(selectedNodeId = nodeId)
    }

    class Factory(private val connectionRepo: ConnectionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NetworkViewModel(connectionRepo) as T
        }
    }
}
