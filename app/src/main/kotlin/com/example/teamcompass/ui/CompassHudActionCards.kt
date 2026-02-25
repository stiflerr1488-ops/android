package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.teamcompass.R
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun BoxScope.CompassHudBluetoothCard(
    state: UiState,
    bluetoothDevicesCount: Int,
    onRequestBluetoothScan: () -> Unit,
) {
    Card(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(end = Spacing.xs, top = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(
                alpha = AlphaTokens.overlay,
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RailButton(
                modifier = Modifier.testTag("bt_scan_button"),
                icon = Icons.AutoMirrored.Filled.BluetoothSearching,
                label = when {
                    state.isBluetoothScanning -> stringResource(R.string.label_bt_scanning)
                    bluetoothDevicesCount > 0 -> stringResource(R.string.label_bt_count, bluetoothDevicesCount)
                    else -> stringResource(R.string.label_bt_idle)
                },
                onClick = onRequestBluetoothScan,
            )
        }
    }
}

@Composable
internal fun BoxScope.CompassHudActionsRail(
    state: UiState,
    nowMs: Long,
    showMarkerPalette: Boolean,
    armedEnemyMarkType: QuickCommandType?,
    onEnemyMarkEnabled: (Boolean) -> Unit,
    onArmedEnemyMarkTypeChange: (QuickCommandType?) -> Unit,
    onShowMarkerPaletteChange: (Boolean) -> Unit,
    onTogglePlayerMode: () -> Unit,
    onSos: () -> Unit,
) {
    Card(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(
                alpha = AlphaTokens.overlay,
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RailButton(
                modifier = Modifier.testTag("mark_toggle_button"),
                icon = Icons.Default.Warning,
                label = if (state.enemyMarkEnabled || armedEnemyMarkType != null || showMarkerPalette) {
                    stringResource(R.string.label_markers_on)
                } else {
                    stringResource(R.string.label_markers)
                },
                onClick = {
                    if (state.enemyMarkEnabled || armedEnemyMarkType != null || showMarkerPalette) {
                        onEnemyMarkEnabled(false)
                        onArmedEnemyMarkTypeChange(null)
                        onShowMarkerPaletteChange(false)
                    } else {
                        onEnemyMarkEnabled(false)
                        onArmedEnemyMarkTypeChange(null)
                        onShowMarkerPaletteChange(true)
                    }
                },
            )
            RailButton(
                modifier = Modifier.testTag("player_mode_button"),
                icon = Icons.Default.SwapHoriz,
                label = if (state.playerMode == PlayerMode.DEAD) {
                    stringResource(R.string.label_dead)
                } else {
                    stringResource(R.string.label_alive)
                },
                onClick = onTogglePlayerMode,
            )
            val sosActive = state.mySosUntilMs > nowMs
            RailButton(
                modifier = Modifier.testTag("sos_button"),
                icon = Icons.Default.Warning,
                label = if (sosActive) stringResource(R.string.label_sos_on) else stringResource(R.string.label_sos),
                onClick = onSos,
            )
        }
    }
}

@Composable
internal fun BoxScope.CompassHudTeamCard(
    onOpenTeamRoster: () -> Unit,
) {
    Card(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = Spacing.xs, bottom = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(
                alpha = AlphaTokens.overlay,
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RailButton(
                modifier = Modifier.testTag("team_button"),
                icon = Icons.Default.Groups,
                label = stringResource(R.string.label_team),
                onClick = onOpenTeamRoster,
            )
        }
    }
}
