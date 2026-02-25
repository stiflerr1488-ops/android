package com.example.teamcompass.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.teamcompass.R

// Multicam Tropic Color Scheme
// Military tactical camouflage with greens, browns, and tans
private val DarkColors = darkColorScheme(
    primary = MulticamGreen,
    onPrimary = MulticamOnSurface,
    secondary = MulticamTan,
    onSecondary = MulticamBackground,
    tertiary = MulticamWarning,
    onTertiary = MulticamBackground,

    background = MulticamBackground,
    onBackground = MulticamOnBackground,

    surface = MulticamSurface,
    onSurface = MulticamOnSurface,

    surfaceVariant = MulticamSurfaceVariant,
    onSurfaceVariant = MulticamOnSurfaceVariant,

    error = MulticamError,
    onError = MulticamOnSurface,
)

private val LightColors = lightColorScheme(
    primary = MulticamOlive,
    onPrimary = MulticamBackground,
    secondary = MulticamSand,
    onSecondary = MulticamBackground,
    tertiary = MulticamWarning,
    onTertiary = MulticamBackground,

    background = Color(0xFFF5F3ED),
    onBackground = MulticamBackground,

    surface = Color(0xFFFAF9F5),
    onSurface = MulticamBackground,

    surfaceVariant = Color(0xFFE8E6DD),
    onSurfaceVariant = MulticamDarkGreen,

    error = MulticamError,
    onError = MulticamOnSurface,
)


private val AppShapes = Shapes(
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
)

/**
 * Режим темы
 */
enum class ThemeMode {
    SYSTEM, // Следовать настройкам системы
    LIGHT,  // Всегда светлая
    DARK    // Всегда тёмная
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun TeamCompassTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            // Устанавливаем цвет статус-бара в цвет фона
            window.statusBarColor = colors.background.toArgb()
            // Для Android 10+ устанавливаем цвет навигационной панели
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.navigationBarColor = colors.surface.toArgb()
            }
        }
    }

    // Explicitly override Snackbar colors for Dark Theme consistency
    val tacticalColorScheme = colors.copy(
        inverseSurface = MulticamSurfaceVariant,
        inverseOnSurface = MulticamOnBackground,
        inversePrimary = MulticamGreen
    )

    MaterialTheme(
        colorScheme = tacticalColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun MulticamBackgroundWithPattern(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background pattern
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.bg_multicam_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        // Content
        content()
    }
}
