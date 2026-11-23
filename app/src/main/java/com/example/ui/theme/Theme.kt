package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FarmForestAccent,
    secondary = FarmBlueAccent,
    tertiary = FarmGoldAccent,
    background = Color(0xFF121412),
    surface = Color(0xFF1E211E),
    onPrimary = Color(0xFF1A3F1D),
    onSecondary = Color(0xFF0D2D4F),
    onBackground = Color(0xFFE2E3E1),
    onSurface = Color(0xFFE2E3E1)
)

private val LightColorScheme = lightColorScheme(
    primary = FarmPrimary,
    secondary = FarmSecondary,
    tertiary = FarmTertiary,
    background = FarmBackground,
    surface = FarmSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1D1B),
    onSurface = Color(0xFF1C1D1B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
