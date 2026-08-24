package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceConnectionStatus
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshGreen
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshYellow

@Composable
fun StatusLedIndicator(
    status: DeviceConnectionStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (color, label) = when (status) {
        DeviceConnectionStatus.CONNECTED -> EsMeshGreen to "CONNECTED"
        DeviceConnectionStatus.CONNECTING -> EsMeshYellow to "CONNECTING..."
        DeviceConnectionStatus.DISCONNECTED -> EsMeshTextMuted to "DISCONNECTED"
        DeviceConnectionStatus.ERROR -> EsMeshRed to "ERROR"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (status == DeviceConnectionStatus.CONNECTED || status == DeviceConnectionStatus.CONNECTING) 0.4f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .alpha(pulseAlpha)
                .background(color, CircleShape)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "● $label",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun RssiMeter(
    rssi: Int,
    modifier: Modifier = Modifier
) {
    val bars = when {
        rssi >= -55 -> 4
        rssi >= -68 -> 3
        rssi >= -80 -> 2
        rssi >= -90 -> 1
        else -> 0
    }
    val barColor = when {
        bars >= 3 -> EsMeshGreen
        bars == 2 -> EsMeshYellow
        else -> EsMeshRed
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        for (i in 1..4) {
            val height = (i * 3 + 3).dp
            val isLit = i <= bars
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(3.dp)
                    .size(width = 3.dp, height = height)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isLit) barColor else EsMeshBorder)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$rssi dBm",
            style = MaterialTheme.typography.labelSmall,
            color = if (bars > 0) barColor else EsMeshTextMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ProtocolBadge(
    protocol: String = "EsMesh/1",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E28))
            .border(0.8.dp, EsMeshBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = protocol,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = EsMeshYellow,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CapabilityChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF181822))
            .border(0.5.dp, EsMeshBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = EsMeshTextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
