package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EsMeshDevice
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshBorderBright
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow

@Composable
fun TelemetryMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = EsMeshYellow,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EsMeshCharcoalCard)
            .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = EsMeshTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TechnicalDeviceCard(
    device: EsMeshDevice,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EsMeshCharcoalCard)
            .border(
                width = if (isConnected) 1.5.dp else 1.dp,
                color = if (isConnected) EsMeshRed else EsMeshBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onDetailsClick() }
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Name, Model, and Status LED
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isConnected) Color(0xFF280A0D) else Color(0xFF1E1E2A), RoundedCornerShape(6.dp))
                            .border(1.dp, if (isConnected) EsMeshRed else EsMeshBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Device",
                            tint = if (isConnected) EsMeshRed else EsMeshYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = EsMeshTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${device.model} • ${device.nodeId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EsMeshYellow,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                StatusLedIndicator(status = device.connectionStatus, showLabel = false)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tech stats row: IP, Wi-Fi, RSSI, Protocol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "IP: ${device.ipAddress}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EsMeshTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Wi-Fi: ${device.ssid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EsMeshTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                RssiMeter(rssi = device.rssi)
                ProtocolBadge(protocol = device.protocolVersion)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions row: Quick Connect / Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EsMeshCyberButton(
                    text = if (isConnected) "CONNECTED" else "CONNECT",
                    onClick = onConnectClick,
                    isPrimary = !isConnected,
                    isDestructive = isConnected,
                    modifier = Modifier.weight(1f)
                )

                EsMeshCyberButton(
                    text = "DETAILS",
                    onClick = onDetailsClick,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
