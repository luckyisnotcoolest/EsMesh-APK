package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.EsMeshConfig
import com.example.data.network.NetworkResponse
import com.example.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfigUiState(
    val config: EsMeshConfig = EsMeshConfig(),
    val supportedCapabilities: List<String> = listOf("wifi_sta", "wifi_ap", "websocket", "http", "mdns", "mesh", "repeater"),
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val activeTab: Int = 0 // 0=GENERAL, 1=WIFI, 2=AP, 3=NETWORK, 4=MESH, 5=SECURITY
)

class ConfigViewModel(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.connectedDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(
                    isConnected = dev != null,
                    supportedCapabilities = dev?.capabilities ?: listOf("wifi_sta", "wifi_ap", "websocket", "http", "mdns", "mesh")
                )
                if (dev != null) {
                    loadConfig(dev.ipAddress, dev.httpPort)
                }
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun updateConfig(update: (EsMeshConfig) -> EsMeshConfig) {
        _uiState.value = _uiState.value.copy(
            config = update(_uiState.value.config),
            saveSuccess = false,
            errorMessage = null
        )
    }

    fun loadConfig(ip: String, port: Int = 80) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val res = connectionRepository.httpClient.getConfig(ip, port)) {
                is NetworkResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        config = res.data,
                        isLoading = false
                    )
                }
                is NetworkResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load config: ${res.message}"
                    )
                }
                is NetworkResponse.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Config load note: Using current defaults (${res.exception.localizedMessage})"
                    )
                }
            }
        }
    }

    fun saveConfig(onComplete: (Boolean) -> Unit) {
        val dev = connectionRepository.connectedDevice.value
        if (dev == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "No ESP32 connected to push config")
            onComplete(false)
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
        viewModelScope.launch {
            when (val res = connectionRepository.httpClient.postConfig(dev.ipAddress, dev.httpPort, _uiState.value.config)) {
                is NetworkResponse.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                    onComplete(true)
                }
                is NetworkResponse.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Config rejected: ${res.message}")
                    onComplete(false)
                }
                is NetworkResponse.Failure -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Save failed: ${res.exception.localizedMessage}")
                    onComplete(false)
                }
            }
        }
    }

    fun restartDevice(onComplete: (Boolean) -> Unit) {
        val dev = connectionRepository.connectedDevice.value ?: return
        viewModelScope.launch {
            when (connectionRepository.httpClient.restartDevice(dev.ipAddress, dev.httpPort)) {
                is NetworkResponse.Success -> onComplete(true)
                else -> onComplete(false)
            }
        }
    }

    class Factory(private val connectionRepo: ConnectionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfigViewModel(connectionRepo) as T
        }
    }
}
