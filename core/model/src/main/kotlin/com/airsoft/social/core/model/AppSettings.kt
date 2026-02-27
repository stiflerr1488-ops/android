package com.airsoft.social.core.model

enum class AppThemePreference {
    System,
    Light,
    Dark,
}

data class AppSettings(
    val themePreference: AppThemePreference = AppThemePreference.System,
    val pushEnabled: Boolean = true,
    val telemetryEnabled: Boolean = false,
    val tacticalQuickLaunchEnabled: Boolean = true,
)
