package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.core.PlayerMode
import kotlin.math.roundToInt

@Composable
internal fun CompassHelpDialog(
    onDismissAndAcknowledge: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissAndAcknowledge,
        confirmButton = {
            FilledTonalButton(onClick = onDismissAndAcknowledge) {
                Text(stringResource(R.string.dialog_understood))
            }
        },
        title = { Text(stringResource(R.string.help_title_radar)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(stringResource(R.string.help_section_gestures), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.help_line_pinch), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.help_line_mark_button), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.help_line_mark_tap), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.help_line_long_press), style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text(stringResource(R.string.help_section_legend), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.help_line_team_points), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.help_line_private_points), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.help_line_sos_priority), style = MaterialTheme.typography.bodySmall)
                Text(
                    stringResource(R.string.help_tip_switch),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}

@Composable
internal fun CompassPointActionDialog(
    action: PointActionState,
    currentUid: String?,
    onDismiss: () -> Unit,
    onCopyToMine: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    val marker = action.marker
    val isAuthor = (!marker.isTeam) || (marker.createdBy == null || marker.createdBy == currentUid)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(marker.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                val scope = if (marker.isTeam) stringResource(R.string.scope_team) else stringResource(R.string.scope_private)
                Text(
                    stringResource(R.string.scope_point_format, scope),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (marker.isTeam && !isAuthor) {
                    Text(
                        stringResource(R.string.team_point_edit_restricted),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (marker.isTeam && !isAuthor) {
                FilledTonalButton(onClick = onCopyToMine) {
                    Text(stringResource(R.string.button_copy_mine))
                }
            } else {
                FilledTonalButton(onClick = onEdit) {
                    Text(stringResource(R.string.button_edit))
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!(marker.isTeam && !isAuthor)) {
                    FilledTonalButton(onClick = onMove) { Text(stringResource(R.string.button_move)) }
                    FilledTonalButton(onClick = onDelete) { Text(stringResource(R.string.button_delete)) }
                }
                FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
            }
        }
    )
}

@Composable
internal fun CompassPointEditDialog(
    dialog: PointDialogState,
    defaultPointLabel: String,
    defaultPointIconRaw: String,
    defaultPointForTeam: Boolean,
    onDismiss: () -> Unit,
    onAddPointAt: (lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) -> Unit,
    onUpdatePoint: (id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) -> Unit,
    onDefaultPointPrefsChanged: (iconRaw: String, forTeam: Boolean) -> Unit,
) {
    var label by remember(dialog.id, dialog.lat, dialog.lon) { mutableStateOf(dialog.initialLabel) }
    var iconRaw by remember(dialog.id, dialog.lat, dialog.lon) {
        mutableStateOf(dialog.initialIconRaw.ifBlank { defaultPointIconRaw })
    }
    var forTeam by remember(dialog.id, dialog.lat, dialog.lon) { mutableStateOf(dialog.isTeam || defaultPointForTeam) }
    val icons = TacticalIconId.entries
    val isEdit = dialog.id != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEdit) stringResource(R.string.point_title) else stringResource(R.string.point_new_title)
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    val name = label.trim().ifBlank { defaultPointLabel }.take(24)
                    if (dialog.id == null) {
                        onAddPointAt(dialog.lat, dialog.lon, name, iconRaw, forTeam)
                        onDefaultPointPrefsChanged(iconRaw, forTeam)
                    } else {
                        onUpdatePoint(dialog.id, dialog.lat, dialog.lon, name, iconRaw, dialog.isTeam)
                    }
                    onDismiss()
                }
            ) {
                Text(if (isEdit) stringResource(R.string.button_save) else stringResource(R.string.button_add))
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.label_cancel)) }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(24) },
                    label = { Text(stringResource(R.string.label_caption)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { ic ->
                                FilledTonalIconButton(
                                    onClick = { iconRaw = ic.raw },
                                    modifier = Modifier.size(54.dp),
                                    shape = RoundedCornerShape(Spacing.md)
                                ) {
                                    Icon(ic.vector, contentDescription = ic.label)
                                }
                            }
                        }
                    }
                }

                if (!isEdit) {
                    BinaryChoiceButtons(
                        modifier = Modifier.fillMaxWidth(),
                        leftText = stringResource(R.string.point_scope_team),
                        rightText = stringResource(R.string.point_scope_private),
                        leftSelected = forTeam,
                        onLeftClick = { forTeam = true },
                        onRightClick = { forTeam = false },
                    )
                } else {
                    Text(
                        if (dialog.isTeam) stringResource(R.string.point_kind_team) else stringResource(R.string.point_kind_private),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
internal fun CompassMapsDialog(
    state: UiState,
    onDismiss: () -> Unit,
    onImportClick: () -> Unit,
    onMapEnabled: (Boolean) -> Unit,
    onOpenFullscreenMap: () -> Unit,
    onMapOpacity: (Float) -> Unit,
    onClearMap: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onImportClick,
                enabled = !state.isBusy
            ) { Text(stringResource(R.string.maps_import_kmz_kml)) }
        },
        title = { Text(stringResource(R.string.label_maps_kmz)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                val activeMap = state.activeMap
                if (activeMap == null) {
                    Text(
                        stringResource(R.string.maps_empty_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(stringResource(R.string.maps_active_format, activeMap.name))

                    val overlay = activeMap.groundOverlay
                    if (overlay != null) {
                        val (wM, hM) = latLonBoxSizeMeters(overlay.north, overlay.south, overlay.east, overlay.west)
                        Text(
                            stringResource(R.string.maps_overlay_size_format, wM.toInt(), hM.toInt()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            stringResource(R.string.maps_overlay_missing),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(stringResource(R.string.maps_show_toggle))
                        Switch(
                            checked = state.mapEnabled,
                            onCheckedChange = onMapEnabled
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            onDismiss()
                            onOpenFullscreenMap()
                        }
                    ) {
                        Text(stringResource(R.string.maps_open_fullscreen))
                    }

                    Text(
                        stringResource(R.string.maps_opacity_format, (state.mapOpacity * 100).roundToInt()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = state.mapOpacity,
                        onValueChange = onMapOpacity,
                        valueRange = 0.1f..1.0f
                    )

                    FilledTonalButton(onClick = onClearMap) { Text(stringResource(R.string.maps_remove)) }
                }

                Text(
                    stringResource(R.string.maps_offline_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
internal fun CompassStatusDialog(
    state: UiState,
    rangeMeters: Float,
    showMarkerPalette: Boolean,
    onDismiss: () -> Unit,
    onCopyCode: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
        title = { Text(stringResource(R.string.label_status)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.status_team_format, state.teamCode.orEmpty()))
                Text(
                    stringResource(
                        R.string.status_callsign_format,
                        state.callsign.ifBlank { stringResource(R.string.default_callsign_player) },
                    )
                )
                val trackingStatus = if (state.isTracking) {
                    stringResource(R.string.status_tracking_on)
                } else {
                    stringResource(R.string.status_tracking_off)
                }
                Text(stringResource(R.string.status_tracking_format, trackingStatus))
                val modeTxt = if (state.playerMode == PlayerMode.DEAD) {
                    stringResource(R.string.status_mode_dead)
                } else if (state.isAnchored) {
                    stringResource(R.string.status_mode_game_anchored)
                } else {
                    stringResource(R.string.status_mode_game)
                }
                Text(stringResource(R.string.status_mode_format, modeTxt))
                Text(stringResource(R.string.status_zoom_format, rangeMeters.roundToInt()))
                CoordinatesLine(me = state.me, headingDeg = state.myHeadingDeg)
                Text(
                    if (state.enemyMarkEnabled || showMarkerPalette) {
                        stringResource(R.string.status_markers_enabled_hint)
                    } else {
                        stringResource(R.string.status_markers_disabled_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(onClick = onCopyCode) { Text(stringResource(R.string.label_copy_code)) }
            }
        }
    )
}
