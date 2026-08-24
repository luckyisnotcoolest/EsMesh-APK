package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshBorderBright
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshRedDark
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.theme.EsMeshYellowDark

@Composable
fun EsMeshCyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val backgroundColor = when {
        !enabled -> Color(0xFF181820)
        isDestructive -> Color(0xFF28080A)
        isPrimary -> EsMeshRed
        else -> EsMeshCharcoalCard
    }

    val contentColor = when {
        !enabled -> Color(0xFF5A5A66)
        isDestructive -> EsMeshRed
        isPrimary -> Color.White
        else -> EsMeshTextPrimary
    }

    val borderColor = when {
        !enabled -> Color(0xFF22222E)
        isDestructive -> EsMeshRed.copy(alpha = 0.7f)
        isPrimary -> EsMeshRedDark
        else -> EsMeshBorderBright
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isPrimary) EsMeshYellow else EsMeshRed)
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
