package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionStatus
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("more_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "MORE",
            connectionStatus = DeviceConnectionStatus.DISCONNECTED
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // About EsMesh Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EsMeshRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "ABOUT ESMESH",
                                style = MaterialTheme.typography.titleMedium,
                                color = EsMeshTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "EsMesh is a robust, bidirectional technical communication and configuration controller for ESP32 and RF mesh network topologies.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EsMeshTextSecondary
                        )
                        Text(
                            text = "Version: 1.0.0 • Protocol: EsMesh/1",
                            style = MaterialTheme.typography.labelSmall,
                            color = EsMeshYellow,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Protocol Documentation Summary
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = EsMeshYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "FIRMWARE PROTOCOL SPEC",
                                style = MaterialTheme.typography.titleMedium,
                                color = EsMeshTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "• Discovery: mDNS _espmesh._tcp / UDP port 8266\n• Stream: WebSocket at /api/v1/ws\n• REST: /api/v1/status, /api/v1/message, /api/v1/config\n• Packet Header: {\"protocol\":\"EsMesh/1\",\"type\":\"message\",\"id\":\"...\",\"source\":\"...\",\"destination\":\"...\",\"timestamp\":...,\"ttl\":5,\"payload\":\"...\"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EsMeshTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Developer Contact Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = EsMeshRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "SUPPORT & CONTACT",
                                style = MaterialTheme.typography.titleMedium,
                                color = EsMeshTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "For firmware specification queries, integrations, or technical questions, contact the author directly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EsMeshTextSecondary
                        )

                        EsMeshCyberButton(
                            text = "CONTACT DEVELOPER",
                            icon = Icons.Default.Email,
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:xerxesreal2@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "[EsMesh] Technical Support Inquiry")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "xerxesreal2@gmail.com", Toast.LENGTH_LONG).show()
                                }
                            },
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth().testTag("contact_developer_btn")
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
