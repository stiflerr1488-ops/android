package com.example.teamcompass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFE8F5E9),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0B0F0C),
    tertiary = Color(0xFFFFCDD2),
    onTertiary = Color(0xFF0B0F0C),
    background = Color(0xFF0B0F0C),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF0F1511),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF121A14),
    onSurfaceVariant = Color(0xFFB7C8B8),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    secondary = Color(0xFF1B5E20),
    onSecondary = Color.White,
    tertiary = Color(0xFFB71C1C),
    onTertiary = Color.White,
    background = Color(0xFFF7FFF7),
    onBackground = Color(0xFF0B0F0C),
    surface = Color.White,
    onSurface = Color(0xFF0B0F0C),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF1B3A1F),
)

@Composable
fun TeamCompassTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
