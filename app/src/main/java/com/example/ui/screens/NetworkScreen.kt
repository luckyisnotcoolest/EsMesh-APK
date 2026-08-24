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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
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
import com.example.data.model.MeshNodeItem
import com.example.data.model.NetworkRouteItem
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.components.RssiMeter
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
import com.example.ui.viewmodel.NetworkViewModel

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topo = uiState.topology

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("network_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "TOPOLOGY",
            connectionStatus = if (uiState.isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = { viewModel.refreshTopology() }) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = EsMeshYellow, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = EsMeshTextPrimary)
                    }
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

            // Overview Metric Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TelemetryMetricCard(
                        label = "Total Nodes",
                        value = "${topo.nodes.size}",
                        icon = Icons.Default.Hub,
                        accentColor = EsMeshYellow,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        label = "Active Routes",
                        value = "${topo.routes.size}",
                        icon = Icons.Default.AltRoute,
                        accentColor = EsMeshRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Visual Node Topology Graph Representation
            item {
                Text(
                    text = "NETWORK TOPOLOGY & ROUTING HIERARCHY",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Phone Node
                        TopologyNodeItem(
                            icon = Icons.Default.Smartphone,
                            title = "Android Controller",
                            subtitle = "Local Client",
                            accentColor = EsMeshYellow
                        )

                        Text(text = "↕ (Wi-Fi / WebSocket)", color = EsMeshTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        // Gateway Node
                        TopologyNodeItem(
                            icon = Icons.Default.Router,
                            title = "ESP32 Gateway (${topo.gatewayNodeId})",
                            subtitle = "IP: ${topo.gatewayIp} • SSID: ${topo.routerSsid}",
                            accentColor = EsMeshRed
                        )

                        Text(text = "↕ (EsMesh RF / 2.4GHz)", color = EsMeshTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        // Mesh Nodes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            topo.nodes.filter { it.nodeId != topo.gatewayNodeId }.forEach { node ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF161622))
                                        .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Sensors, contentDescription = null, tint = EsMeshYellow, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = node.nodeId, color = EsMeshTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text(text = "${node.hops} hop", color = EsMeshTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Mesh Nodes List
            item {
                Text(
                    text = "ACTIVE MESH NODES (${topo.nodes.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            items(topo.nodes, key = { it.nodeId }) { node ->
                MeshNodeDetailCard(node = node)
            }

            // Routing Table
            item {
                Text(
                    text = "ROUTING TABLE & NEXT HOPS (${topo.routes.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(topo.routes) { route ->
                RouteDetailCard(route = route)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TopologyNodeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14141E))
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = EsMeshTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = EsMeshTextSecondary, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun MeshNodeDetailCard(node: MeshNodeItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EsMeshCharcoalCard)
            .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${node.name} (${node.nodeId})",
                    style = MaterialTheme.typography.titleMedium,
                    color = EsMeshTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Role: ${node.role} • IP: ${node.ipAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshYellow,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Hops: ${node.hops} • Parent: ${node.parentNodeId ?: "Direct Root"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            RssiMeter(rssi = node.rssi)
        }
    }
}

@Composable
private fun RouteDetailCard(route: NetworkRouteItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF101016))
            .border(0.8.dp, EsMeshBorder, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${route.destinationNode} via ${route.nextHopNode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsMeshTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Metric: ${route.metricHops} hops • State: ${route.state}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "${route.signalStrength} dBm",
                color = EsMeshYellow,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
