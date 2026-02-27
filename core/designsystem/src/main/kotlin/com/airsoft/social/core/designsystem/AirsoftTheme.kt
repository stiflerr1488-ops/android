package com.airsoft.social.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Mirrors TeamCompass palette from:
// app/src/main/kotlin/com/example/teamcompass/ui/theme/MulticamColors.kt
private val MulticamGreen = Color(0xFF5F6B4A)
private val MulticamBrown = Color(0xFF6B5342)
private val MulticamTan = Color(0xFFA89F81)
private val MulticamKhaki = Color(0xFF8B7D6B)
private val MulticamBackground = Color(0xFF1A1C16)
private val MulticamSurface = Color(0xFF252821)
private val MulticamSurfaceVariant = Color(0xFF2D3128)
private val MulticamOnBackground = Color(0xFFE8E4D5)
private val MulticamOnSurface = Color(0xFFE0DCC8)
private val MulticamOnSurfaceVariant = Color(0xFFB8B5A5)
private val MulticamSuccess = Color(0xFF6B8E5E)
private val MulticamWarning = Color(0xFFD9A441)
private val MulticamError = Color(0xFFC75B4C)
private val MulticamInfo = Color(0xFF5F7D8B)
private val MulticamSOS = Color(0xFFFF4D4D)
private val MulticamActive = Color(0xFF7D9E6B)

private val AirsoftDarkColors = darkColorScheme(
    primary = MulticamGreen,
    onPrimary = MulticamOnSurface,
    primaryContainer = MulticamBrown,
    onPrimaryContainer = MulticamOnBackground,
    secondary = MulticamBrown,
    onSecondary = MulticamOnSurface,
    secondaryContainer = MulticamTan,
    onSecondaryContainer = MulticamBackground,
    tertiary = MulticamWarning,
    onTertiary = MulticamBackground,
    tertiaryContainer = MulticamSuccess,
    onTertiaryContainer = MulticamOnBackground,
    background = MulticamBackground,
    onBackground = MulticamOnBackground,
    surface = MulticamSurface,
    onSurface = MulticamOnSurface,
    surfaceVariant = MulticamSurfaceVariant,
    onSurfaceVariant = MulticamOnSurfaceVariant,
    inverseSurface = MulticamSurfaceVariant,
    inverseOnSurface = MulticamOnBackground,
    inversePrimary = MulticamActive,
    error = MulticamError,
    onError = MulticamOnSurface,
    errorContainer = MulticamSOS.copy(alpha = 0.22f),
    onErrorContainer = MulticamOnBackground,
    outline = MulticamKhaki,
    outlineVariant = MulticamInfo.copy(alpha = 0.6f),
)

private val AirsoftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(14.dp),
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
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) AirsoftDarkColors else AirsoftDarkColors,
        typography = Typography(),
        shapes = AirsoftShapes,
        content = content,
    )
}
