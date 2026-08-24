package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiagnosticPingResult
import com.example.data.model.DiagnosticSummary
import com.example.data.network.WsConnectionState
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.DiagnosticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val isRunning: Boolean = false,
    val isConnected: Boolean = false,
    val connectedIp: String = "192.168.1.120",
    val wsConnected: Boolean = false,
    val lastResult: DiagnosticPingResult? = null,
    val pingHistory: List<DiagnosticPingResult> = emptyList(),
    val totalPings: Int = 0,
    val successfulPings: Int = 0,
    val failedPings: Int = 0,
    val averageLatencyMs: Double = 0.0,
    val rawWsLogs: List<String> = emptyList()
)

class DiagnosticsViewModel(
    private val diagnosticsRepository: DiagnosticsRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.wsLogs.collect { log ->
                val current = _uiState.value.rawWsLogs.toMutableList()
                current.add(0, log)
                _uiState.value = _uiState.value.copy(rawWsLogs = current.take(50))
            }
        }

        viewModelScope.launch {
            diagnosticsRepository.summary.collect { summary ->
                _uiState.value = _uiState.value.copy(
                    isRunning = summary.isRunning,
                    lastResult = summary.lastResult ?: _uiState.value.lastResult,
                    pingHistory = summary.pingHistory,
                    totalPings = summary.totalPings,
                    successfulPings = summary.successfulPings,
                    failedPings = summary.failedPings,
                    averageLatencyMs = summary.averageLatencyMs
                )
            }
        }

        viewModelScope.launch {
            connectionRepository.connectedDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(
                    isConnected = dev != null,
                    connectedIp = dev?.ipAddress ?: "192.168.1.120"
                )
            }
        }

        viewModelScope.launch {
            connectionRepository.wsState.collect { wsState ->
                _uiState.value = _uiState.value.copy(wsConnected = wsState is WsConnectionState.Connected)
            }
        }
    }

    fun runPingTest() {
        val dev = connectionRepository.connectedDevice.value
        val ip = dev?.ipAddress ?: "192.168.1.120"
        val port = dev?.httpPort ?: 80
        val isWsConnected = connectionRepository.wsState.value is WsConnectionState.Connected

        viewModelScope.launch {
            diagnosticsRepository.runDiagnostic(ip, port, isWsConnected)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            diagnosticsRepository.clearHistory()
            _uiState.value = _uiState.value.copy(
                rawWsLogs = emptyList(),
                lastResult = null,
                pingHistory = emptyList(),
                totalPings = 0,
                successfulPings = 0,
                failedPings = 0,
                averageLatencyMs = 0.0
            )
        }
    }

    class Factory(
        private val diagRepo: DiagnosticsRepository,
        private val connRepo: ConnectionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiagnosticsViewModel(diagRepo, connRepo) as T
        }
    }
}
