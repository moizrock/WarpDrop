package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VayuCyberSlateColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberObsidian,
    primaryContainer = Color(0xFF0A2E3D),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = TextPrimaryIce,
    secondaryContainer = DeepViolet,
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = EmeraldNominal,
    onTertiary = CyberObsidian,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = CyberObsidian,
    onBackground = TextPrimaryIce,
    surface = CyberSlateBackground,
    onSurface = TextPrimaryIce,
    surfaceVariant = CyberSlateCard,
    onSurfaceVariant = TextSecondarySlate,
    outline = CyberBorderSubtle,
    outlineVariant = Color(0xFF1E293B),
    error = CrimsonCritical,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA)
)

@Composable
fun VayuDropTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VayuCyberSlateColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    VayuDropTheme(content = content)
}
