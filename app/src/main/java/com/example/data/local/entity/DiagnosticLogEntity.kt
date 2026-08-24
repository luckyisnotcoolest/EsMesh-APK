package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DiagnosticPingResult

@Entity(tableName = "diagnostic_logs")
data class DiagnosticLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val targetIp: String,
    val isBidirectionalSuccess: Boolean,
    val phoneToEsp32LatencyMs: Long,
    val esp32ToPhoneLatencyMs: Long,
    val roundTripTimeMs: Long,
    val httpStatusCode: Int?,
    val wsConnected: Boolean,
    val rssi: Int,
    val errorDescription: String?
) {
    fun toDomain(): DiagnosticPingResult {
        return DiagnosticPingResult(
            timestamp = timestamp,
            targetIp = targetIp,
            isBidirectionalSuccess = isBidirectionalSuccess,
            phoneToEsp32LatencyMs = phoneToEsp32LatencyMs,
            esp32ToPhoneLatencyMs = esp32ToPhoneLatencyMs,
            roundTripTimeMs = roundTripTimeMs,
            httpStatusCode = httpStatusCode,
            wsConnected = wsConnected,
            rssi = rssi,
            errorDescription = errorDescription
        )
    }

    companion object {
        fun fromDomain(item: DiagnosticPingResult): DiagnosticLogEntity {
            return DiagnosticLogEntity(
                timestamp = item.timestamp,
                targetIp = item.targetIp,
                isBidirectionalSuccess = item.isBidirectionalSuccess,
                phoneToEsp32LatencyMs = item.phoneToEsp32LatencyMs,
                esp32ToPhoneLatencyMs = item.esp32ToPhoneLatencyMs,
                roundTripTimeMs = item.roundTripTimeMs,
                httpStatusCode = item.httpStatusCode,
                wsConnected = item.wsConnected,
                rssi = item.rssi,
                errorDescription = item.errorDescription
            )
        }
    }
}
