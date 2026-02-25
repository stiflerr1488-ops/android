package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun SettingsScreenAndThemeCard(
    state: UiState,
    onAutoBrightnessEnabled: (Boolean) -> Unit,
    onScreenBrightness: (Float) -> Unit,
    onThemeMode: (com.example.teamcompass.ui.theme.ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(stringResource(R.string.settings_screen_title), fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        stringResource(R.string.settings_auto_brightness_title),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        stringResource(R.string.settings_auto_brightness_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.autoBrightnessEnabled,
                    onCheckedChange = onAutoBrightnessEnabled,
                )
            }

            Text(
                stringResource(R.string.settings_screen_brightness_title),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = state.screenBrightness,
                onValueChange = onScreenBrightness,
                valueRange = 0.1f..1.0f,
                steps = 9,
            )
            Text(
                "${(state.screenBrightness * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Text(
                stringResource(R.string.settings_theme_title),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                val currentTheme = state.themeMode
                FilledTonalButton(
                    onClick = { onThemeMode(com.example.teamcompass.ui.theme.ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f),
                    enabled = currentTheme != com.example.teamcompass.ui.theme.ThemeMode.SYSTEM,
                ) {
                    Text(stringResource(R.string.settings_theme_system))
                }
                FilledTonalButton(
                    onClick = { onThemeMode(com.example.teamcompass.ui.theme.ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                    enabled = currentTheme != com.example.teamcompass.ui.theme.ThemeMode.LIGHT,
                ) {
                    Text(stringResource(R.string.settings_theme_light))
                }
                FilledTonalButton(
                    onClick = { onThemeMode(com.example.teamcompass.ui.theme.ThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                    enabled = currentTheme != com.example.teamcompass.ui.theme.ThemeMode.DARK,
                ) {
                    Text(stringResource(R.string.settings_theme_dark))
                }
            }
            Text(
                when (state.themeMode) {
                    com.example.teamcompass.ui.theme.ThemeMode.SYSTEM -> {
                        stringResource(R.string.settings_theme_system_description)
                    }
                    com.example.teamcompass.ui.theme.ThemeMode.LIGHT -> {
                        stringResource(R.string.settings_theme_light_description)
                    }
                    com.example.teamcompass.ui.theme.ThemeMode.DARK -> {
                        stringResource(R.string.settings_theme_dark_description)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
