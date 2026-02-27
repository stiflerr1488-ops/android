package com.airsoft.social.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

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
