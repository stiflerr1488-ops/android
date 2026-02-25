package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun CompassHudMenuCard(
    state: UiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    showMarkerPalette: Boolean,
    onShowMarkerPaletteChange: (Boolean) -> Unit,
    armedEnemyMarkType: QuickCommandType?,
    onArmedEnemyMarkTypeChange: (QuickCommandType?) -> Unit,
    onShowStatusDialog: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onAddPointAtCurrentLocation: () -> Unit,
    onShowMapsDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onCopyCode: () -> Unit,
    onLeave: () -> Unit,
    onEnemyMarkEnabled: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlay),
        ),
    ) {
        Box(modifier = Modifier.padding(Spacing.xs)) {
            RailButton(
                icon = Icons.Default.Menu,
                label = stringResource(R.string.label_menu),
                onClick = { onMenuExpandedChange(true) },
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_status)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onShowStatusDialog()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_how_to_use)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onShowHelpDialog()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.label_how_to_use),
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (editMode) {
                                stringResource(R.string.label_edit_mode_on)
                            } else {
                                stringResource(R.string.label_edit_mode_off)
                            },
                        )
                    },
                    onClick = {
                        onMenuExpandedChange(false)
                        val next = !editMode
                        onEditModeChange(next)
                        if (next && (state.enemyMarkEnabled || armedEnemyMarkType != null || showMarkerPalette)) {
                            onEnemyMarkEnabled(false)
                            onArmedEnemyMarkTypeChange(null)
                            onShowMarkerPaletteChange(false)
                        }
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_add_point_here)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onAddPointAtCurrentLocation()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_markers)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onArmedEnemyMarkTypeChange(null)
                        onShowMarkerPaletteChange(true)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_maps_kmz)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onShowMapsDialog()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (state.enemyMarkEnabled || armedEnemyMarkType != null || showMarkerPalette) {
                                stringResource(R.string.label_markers_on)
                            } else {
                                stringResource(R.string.label_markers_off)
                            },
                        )
                    },
                    onClick = {
                        onMenuExpandedChange(false)
                        val next = !(state.enemyMarkEnabled || armedEnemyMarkType != null || showMarkerPalette)
                        onEnemyMarkEnabled(false)
                        onArmedEnemyMarkTypeChange(null)
                        onShowMarkerPaletteChange(next)
                        if (next) onEditModeChange(false)
                    },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_settings)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onOpenSettings()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.label_settings),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_copy_code)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onCopyCode()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.label_copy_code),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_leave_team)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onLeave()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.label_leave_team),
                        )
                    },
                )
            }
        }
    }
}
