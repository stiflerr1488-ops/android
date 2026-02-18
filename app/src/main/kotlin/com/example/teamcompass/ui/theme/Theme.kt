package com.example.teamcompass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape

// Military palette: olive + sand
private val DarkColors = darkColorScheme(
    primary = Color(0xFF6B7D2A),
    onPrimary = Color(0xFF10120B),
    secondary = Color(0xFFC2B280),
    onSecondary = Color(0xFF10120B),
    tertiary = Color(0xFFD9A441),
    onTertiary = Color(0xFF10120B),

    background = Color(0xFF0E0F0A),
    onBackground = Color(0xFFEFE6CF),

    surface = Color(0xFF14150D),
    onSurface = Color(0xFFEFE6CF),

    surfaceVariant = Color(0xFF1B1D12),
    onSurfaceVariant = Color(0xFFBFB59C),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4E5E1F),
    onPrimary = Color.White,
    secondary = Color(0xFF9E8F61),
    onSecondary = Color.White,
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,

    background = Color(0xFFF4EEDB),
    onBackground = Color(0xFF16170F),

    surface = Color(0xFFFDF9EE),
    onSurface = Color(0xFF16170F),

    surfaceVariant = Color(0xFFECE4CD),
    onSurfaceVariant = Color(0xFF3A3A2A),
)


private val AppShapes = Shapes(
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
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
        shapes = AppShapes,
        content = content
    )
}
