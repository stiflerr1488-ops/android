package com.airsoft.social.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AirsoftDarkColors = darkColorScheme(
    primary = Color(0xFFFB8C00),
    onPrimary = Color(0xFF0B0E14),
    primaryContainer = Color(0xFF5F3A00),
    onPrimaryContainer = Color(0xFFFFDDB4),
    secondary = Color(0xFF94A3B8),
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF151821),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2430),
    onSurfaceVariant = Color(0xFFB8C1D1),
)

object AirsoftSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

@Composable
fun AirsoftTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = AirsoftDarkColors
    MaterialTheme(
        colorScheme = if (useDarkTheme) colors else colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content,
    )
}

