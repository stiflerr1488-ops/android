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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.ui.components.BinaryChoiceButtons
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun SettingsDefaultModeCard(
    state: UiState,
    onDefaultMode: (TrackingMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(stringResource(R.string.settings_default_mode_title), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.settings_default_mode_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val gameSelected = state.defaultMode == TrackingMode.GAME
            BinaryChoiceButtons(
                modifier = Modifier.fillMaxWidth(),
                leftText = stringResource(R.string.settings_mode_game),
                rightText = stringResource(R.string.settings_mode_silent),
                leftSelected = gameSelected,
                onLeftClick = { if (!gameSelected) onDefaultMode(TrackingMode.GAME) },
                onRightClick = { if (gameSelected) onDefaultMode(TrackingMode.SILENT) },
            )
        }
    }
}

@Composable
internal fun SettingsControlsLayoutCard(
    state: UiState,
    onControlLayoutEdit: (Boolean) -> Unit,
    onResetControlPositions: () -> Unit,
    onApplyRightHandLayout: () -> Unit,
    onApplyLeftHandLayout: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(stringResource(R.string.settings_controls_layout_title), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.settings_controls_layout_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.settings_controls_move_mode))
                Switch(
                    checked = state.controlLayoutEditEnabled,
                    onCheckedChange = onControlLayoutEdit,
                )
            }
            OutlinedButton(
                onClick = onResetControlPositions,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_controls_reset_layout))
            }
            val isLeftLayout = (state.controlPositions[CompassControlId.LIST]?.xNorm ?: 0.5f) < 0.5f
            Text(
                stringResource(R.string.settings_grip_profile_title),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                FilledTonalButton(
                    onClick = onApplyRightHandLayout,
                    modifier = Modifier.weight(1f),
                    enabled = isLeftLayout,
                ) {
                    Text(stringResource(R.string.settings_grip_right))
                }
                FilledTonalButton(
                    onClick = onApplyLeftHandLayout,
                    modifier = Modifier.weight(1f),
                    enabled = !isLeftLayout,
                ) {
                    Text(stringResource(R.string.settings_grip_left))
                }
            }
        }
    }
}

@Composable
internal fun SettingsDiagnosticsCard(state: UiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(R.string.settings_diagnostics_title), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.settings_diagnostics_network),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(
                    R.string.settings_diagnostics_read_write_errors_format,
                    state.telemetry.rtdbReadErrors,
                    state.telemetry.rtdbWriteErrors,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.settings_diagnostics_tracking),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(
                    R.string.settings_diagnostics_tracking_restarts_format,
                    state.telemetry.trackingRestarts,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.telemetry.lastTrackingRestartReason?.let { reason ->
                Text(
                    stringResource(R.string.settings_diagnostics_last_restart_reason_format, reason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
