package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.EsMeshDevice
import com.example.ui.components.CapabilityChip
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.RssiMeter
import com.example.ui.components.StatusLedIndicator
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceDetailsScreen(
    device: EsMeshDevice,
    isConnected: Boolean,
    onBack: () -> Unit,
    onConnectToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("device_details_screen")
    ) {
        EsMeshTopAppBar(
            title = device.name,
            subtitle = "NODE SPECS",
            connectionStatus = if (isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = EsMeshTextPrimary
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, if (isConnected) EsMeshRed else EsMeshBorder, RoundedCornerShape(10.dp))
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
                                    text = device.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = EsMeshTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${device.model} • ${device.nodeId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EsMeshYellow,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            StatusLedIndicator(
                                status = if (isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EsMeshCyberButton(
                                text = if (isConnected) "DISCONNECT" else "CONNECT NOW",
                                onClick = onConnectToggle,
                                isPrimary = !isConnected,
                                isDestructive = isConnected,
                                modifier = Modifier.weight(1f)
                            )

                            EsMeshCyberButton(
                                text = "DELETE",
                                icon = Icons.Default.Delete,
                                onClick = onDelete,
                                isPrimary = false,
                                isDestructive = true
                            )
                        }
                    }
                }
            }

            // Technical Specifications List
            item {
                Text(
                    text = "RADIO & HARDWARE PARAMETERS",
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
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpecRow("Model Name", device.model)
                        SpecRow("Chipset", device.chip)
                        SpecRow("Node ID", device.nodeId, isAccent = true)
                        SpecRow("IP Address", device.ipAddress)
                        SpecRow("HTTP Port", device.httpPort.toString())
                        SpecRow("WebSocket Port", device.wsPort.toString())
                        SpecRow("MAC Address", device.macAddress)
                        SpecRow("RSSI Signal", "${device.rssi} dBm")
                        SpecRow("Firmware Version", device.firmwareVersion)
                        SpecRow("Protocol", device.protocolVersion, isAccent = true)
                        SpecRow("Wi-Fi Mode", device.wifiMode)
                        SpecRow("Wi-Fi Band", device.wifiBand)
                        SpecRow("SSID", device.ssid)
                        SpecRow("Uptime", "${device.uptimeSeconds}s")
                    }
                }
            }

            // Protocol Capabilities Chips
            item {
                Text(
                    text = "SUPPORTED CAPABILITIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    device.capabilities.forEach { cap ->
                        CapabilityChip(name = cap)
                    }
                }
            }

            // Web Dashboard Button
            item {
                EsMeshCyberButton(
                    text = "OPEN ESP32 WEB INTERFACE",
                    icon = Icons.Default.Language,
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://${device.ipAddress}:${device.httpPort}/")
                        )
                        context.startActivity(browserIntent)
                    },
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth().testTag("open_web_ui_btn")
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, isAccent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = EsMeshTextMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = if (isAccent) EsMeshYellow else EsMeshTextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
