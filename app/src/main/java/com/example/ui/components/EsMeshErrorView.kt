package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshRedContainer
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow

@Composable
fun EsMeshErrorBanner(
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EsMeshRedContainer)
            .border(1.dp, EsMeshRed.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = EsMeshRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = EsMeshRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = EsMeshTextPrimary
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                EsMeshCyberButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    isPrimary = true,
                    isDestructive = true
                )
            }
        }
    }
}
