package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionStatus
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoal
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshYellow

@Composable
fun EsMeshTopAppBar(
    title: String = "EsMesh",
    subtitle: String? = null,
    connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.DISCONNECTED,
    actions: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EsMeshBlack)
            .border(width = 0.5.dp, color = EsMeshBorder)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("esmesh_top_app_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio branding symbol
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1B080A), RoundedCornerShape(8.dp))
                    .border(1.dp, EsMeshRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "EsMesh Node Indicator",
                    tint = EsMeshRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Title & Tagline
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = EsMeshTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ $subtitle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EsMeshYellow,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Connection indicator & custom actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLedIndicator(status = connectionStatus, showLabel = true)
                Spacer(modifier = Modifier.width(8.dp))
                actions()
            }
        }
    }
}
