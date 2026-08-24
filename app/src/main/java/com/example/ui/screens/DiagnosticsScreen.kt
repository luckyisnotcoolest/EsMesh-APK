package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.DiagnosticPingResult
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.components.TelemetryMetricCard
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshGreen
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.viewmodel.DiagnosticsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("diagnostics_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "DIAGNOSTICS",
            connectionStatus = if (uiState.isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = { viewModel.clearLogs() }) {
                    Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Clear Logs", tint = EsMeshTextSecondary)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Quick Diagnostic Action Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BIDIRECTIONAL LATENCY & LINK PROBE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EsMeshYellow,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target: ${uiState.connectedIp}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EsMeshTextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (uiState.isRunning) {
                                CircularProgressIndicator(color = EsMeshYellow, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        EsMeshCyberButton(
                            text = if (uiState.isRunning) "TESTING IN FLIGHT..." else "RUN PING / PONG TEST",
                            icon = Icons.Default.PlayArrow,
                            onClick = { viewModel.runPingTest() },
                            isPrimary = true,
                            enabled = !uiState.isRunning,
                            modifier = Modifier.fillMaxWidth().testTag("run_ping_test_btn")
                        )
                    }
                }
            }

            // Telemetry Metric Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TelemetryMetricCard(
                        label = "Avg Latency",
                        value = "${String.format("%.1f", uiState.averageLatencyMs)} ms",
                        icon = Icons.Default.Speed,
                        accentColor = EsMeshYellow,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        label = "Successful",
                        value = "${uiState.successfulPings}",
                        icon = Icons.Default.PlayArrow,
                        accentColor = EsMeshGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        label = "Failed",
                        value = "${uiState.failedPings}",
                        icon = Icons.Default.Terminal,
                        accentColor = EsMeshRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Last Ping Detail Breakdown
            uiState.lastResult?.let { last ->
                item {
                    Text(
                        text = "LAST PROBE DETAIL",
                        style = MaterialTheme.typography.labelSmall,
                        color = EsMeshTextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF12121A))
                            .border(1.dp, if (last.isBidirectionalSuccess) EsMeshGreen else EsMeshRed, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DiagRow("Status", if (last.isBidirectionalSuccess) "SUCCESS (200 OK)" else "FAILED (${last.errorDescription ?: "Timeout"})")
                            DiagRow("Phone → ESP32 Latency", "${last.phoneToEsp32LatencyMs} ms")
                            DiagRow("ESP32 → Phone Latency", "${last.esp32ToPhoneLatencyMs} ms")
                            DiagRow("Round-Trip Time (RTT)", "${last.roundTripTimeMs} ms")
                            DiagRow("WebSocket Active", if (last.wsConnected) "YES" else "NO")
                            DiagRow("Signal RSSI", "${last.rssi} dBm")
                        }
                    }
                }
            }

            // Live WebSocket Frame Logs
            item {
                Text(
                    text = "REAL-TIME SOCKET FRAMES & DEBUG LOGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF08080C))
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    if (uiState.rawWsLogs.isEmpty()) {
                        Text(
                            text = "Socket event stream idle. Open socket or trigger diagnostics to view raw frames.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EsMeshTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        LazyColumn {
                            items(uiState.rawWsLogs) { log ->
                                val color = when {
                                    log.contains("RX") -> EsMeshGreen
                                    log.contains("TX") -> EsMeshYellow
                                    log.contains("ERROR") || log.contains("FAILURE") -> EsMeshRed
                                    else -> EsMeshTextSecondary
                                }
                                Text(
                                    text = log,
                                    color = color,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = EsMeshTextMuted, fontFamily = FontFamily.Monospace)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = EsMeshTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}
