package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EsMeshRed,
    onPrimary = EsMeshWhite,
    primaryContainer = EsMeshRedContainer,
    onPrimaryContainer = EsMeshRed,
    secondary = EsMeshYellow,
    onSecondary = EsMeshBlack,
    secondaryContainer = EsMeshYellowContainer,
    onSecondaryContainer = EsMeshYellow,
    tertiary = EsMeshBlue,
    onTertiary = EsMeshWhite,
    background = EsMeshBlack,
    onBackground = EsMeshTextPrimary,
    surface = EsMeshCharcoalDark,
    onSurface = EsMeshTextPrimary,
    surfaceVariant = EsMeshCharcoalCard,
    onSurfaceVariant = EsMeshTextSecondary,
    outline = EsMeshBorder,
    outlineVariant = EsMeshBorderBright
)

private val LightColorScheme = lightColorScheme(
    primary = EsMeshRed,
    onPrimary = EsMeshWhite,
    primaryContainer = Color(0xFFFFDCDA),
    onPrimaryContainer = EsMeshRedDark,
    secondary = EsMeshYellowDark,
    onSecondary = EsMeshWhite,
    secondaryContainer = Color(0xFFFFF3B0),
    onSecondaryContainer = EsMeshYellowDark,
    tertiary = EsMeshBlue,
    onTertiary = EsMeshWhite,
    background = EsMeshLightBackground,
    onBackground = EsMeshLightTextPrimary,
    surface = EsMeshLightSurface,
    onSurface = EsMeshLightTextPrimary,
    surfaceVariant = EsMeshLightSurfaceVariant,
    onSurfaceVariant = EsMeshLightTextSecondary,
    outline = EsMeshLightBorder,
    outlineVariant = Color(0xFF9CA3AF)
)

@Composable
fun EsMeshTheme(
    darkTheme: Boolean = true, // Default to dark technical theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
