package com.example.data.model

data class DiagnosticPingResult(
    val timestamp: Long = System.currentTimeMillis(),
    val targetIp: String,
    val isBidirectionalSuccess: Boolean,
    val phoneToEsp32LatencyMs: Long,
    val esp32ToPhoneLatencyMs: Long,
    val roundTripTimeMs: Long,
    val httpStatusCode: Int?,
    val wsConnected: Boolean,
    val rssi: Int,
    val errorDescription: String? = null
)

data class DiagnosticSummary(
    val isRunning: Boolean = false,
    val lastResult: DiagnosticPingResult? = null,
    val pingHistory: List<DiagnosticPingResult> = emptyList(),
    val totalPings: Int = 0,
    val successfulPings: Int = 0,
    val failedPings: Int = 0,
    val averageLatencyMs: Double = 0.0
)
