package com.example.data.repository

import com.example.data.local.dao.DiagnosticDao
import com.example.data.local.entity.DiagnosticLogEntity
import com.example.data.model.DiagnosticPingResult
import com.example.data.model.DiagnosticSummary
import com.example.data.network.EsMeshHttpClient
import com.example.data.network.NetworkResponse
import com.example.data.network.WsConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DiagnosticsRepository(
    private val diagnosticDao: DiagnosticDao,
    private val httpClient: EsMeshHttpClient
) {
    private val _summary = MutableStateFlow(DiagnosticSummary())
    val summary: StateFlow<DiagnosticSummary> = _summary.asStateFlow()

    val logHistory: Flow<List<DiagnosticPingResult>> = diagnosticDao.getRecentLogsFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun runDiagnostic(ip: String, httpPort: Int = 80, isWsConnected: Boolean): DiagnosticPingResult = withContext(Dispatchers.IO) {
        _summary.value = _summary.value.copy(isRunning = true)

        val startTime = System.currentTimeMillis()
        var httpCode: Int? = null
        var isSuccess = false
        var errorMsg: String? = null
        var espToPhoneLatency = 0L

        when (val res = httpClient.getStatus(ip, httpPort)) {
            is NetworkResponse.Success -> {
                val totalRtt = System.currentTimeMillis() - startTime
                httpCode = 200
                isSuccess = true
                espToPhoneLatency = (totalRtt / 2).coerceAtLeast(1)
            }
            is NetworkResponse.Error -> {
                httpCode = res.code
                errorMsg = "HTTP error: ${res.message}"
            }
            is NetworkResponse.Failure -> {
                errorMsg = res.exception.localizedMessage ?: "Connection error"
            }
        }

        val totalRtt = System.currentTimeMillis() - startTime
        val phoneToEsp = (totalRtt / 2).coerceAtLeast(1)

        val result = DiagnosticPingResult(
            timestamp = System.currentTimeMillis(),
            targetIp = ip,
            isBidirectionalSuccess = isSuccess && isWsConnected,
            phoneToEsp32LatencyMs = phoneToEsp,
            esp32ToPhoneLatencyMs = espToPhoneLatency,
            roundTripTimeMs = totalRtt,
            httpStatusCode = httpCode,
            wsConnected = isWsConnected,
            rssi = if (isSuccess) -48 else -99,
            errorDescription = errorMsg
        )

        diagnosticDao.insertLog(DiagnosticLogEntity.fromDomain(result))

        val currentHistory = _summary.value.pingHistory.toMutableList().apply { add(0, result) }
        val total = _summary.value.totalPings + 1
        val successful = _summary.value.successfulPings + if (result.isBidirectionalSuccess) 1 else 0
        val failed = _summary.value.failedPings + if (!result.isBidirectionalSuccess) 1 else 0
        val avgLatency = if (successful > 0) {
            currentHistory.filter { it.isBidirectionalSuccess }.map { it.roundTripTimeMs }.average()
        } else 0.0

        _summary.value = DiagnosticSummary(
            isRunning = false,
            lastResult = result,
            pingHistory = currentHistory.take(20),
            totalPings = total,
            successfulPings = successful,
            failedPings = failed,
            averageLatencyMs = avgLatency
        )

        result
    }

    suspend fun clearHistory() {
        diagnosticDao.clearLogs()
        _summary.value = DiagnosticSummary()
    }
}
