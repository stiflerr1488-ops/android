package com.example.teamcompass.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object MulticamEffects {
    // Gradient for primary buttons
    val primaryGradient = Brush.verticalGradient(
        colors = listOf(MulticamGreen, MulticamDarkGreen)
    )

    // Gradient for secondary buttons
    val secondaryGradient = Brush.verticalGradient(
        colors = listOf(MulticamTan, MulticamKhaki)
    )

    // Gradient for SOS button
    val sosGradient = Brush.verticalGradient(
        colors = listOf(MulticamSOS, MulticamError)
    )

    // Gradient for surface cards
    val surfaceGradient = Brush.verticalGradient(
        colors = listOf(MulticamSurface, MulticamSurfaceVariant)
    )

    // Gradient for background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(MulticamBackground, Color(0xFF0F110C))
    )

    // Glassmorphism overlay
    val glassOverlay = Color(0xFF2D3128).copy(alpha = 0.6f)

    // Subtle glow for active states
    val activeGlow = MulticamActive.copy(alpha = 0.3f)

    // Shadow colors
    val shadowDark = Color(0xFF000000).copy(alpha = 0.4f)
    val shadowLight = Color(0xFF000000).copy(alpha = 0.2f)
}
