package com.example.teamcompass.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.example.teamcompass.ui.components.TeamRosterItemUi

@Composable
internal fun BoxScope.CompassHudOverlay(
    state: UiState,
    teamRoster: List<TeamRosterItemUi>,
    bluetoothDevicesCount: Int,
    nowMs: Long,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    showMarkerPalette: Boolean,
    onShowMarkerPaletteChange: (Boolean) -> Unit,
    armedEnemyMarkType: QuickCommandType?,
    onArmedEnemyMarkTypeChange: (QuickCommandType?) -> Unit,
    formatCoord: (Double) -> String,
    onShowStatusDialog: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onAddPointAtCurrentLocation: () -> Unit,
    onShowMapsDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onCopyCode: () -> Unit,
    onLeave: () -> Unit,
    onEnemyMarkEnabled: (Boolean) -> Unit,
    onTogglePlayerMode: () -> Unit,
    onSos: () -> Unit,
    onRequestBluetoothScan: () -> Unit,
    onOpenTeamRoster: () -> Unit,
) {
    CompassHudTopLeftPanel(
        state = state,
        teamRoster = teamRoster,
        menuExpanded = menuExpanded,
        onMenuExpandedChange = onMenuExpandedChange,
        editMode = editMode,
        onEditModeChange = onEditModeChange,
        showMarkerPalette = showMarkerPalette,
        onShowMarkerPaletteChange = onShowMarkerPaletteChange,
        armedEnemyMarkType = armedEnemyMarkType,
        onArmedEnemyMarkTypeChange = onArmedEnemyMarkTypeChange,
        formatCoord = formatCoord,
        onShowStatusDialog = onShowStatusDialog,
        onShowHelpDialog = onShowHelpDialog,
        onAddPointAtCurrentLocation = onAddPointAtCurrentLocation,
        onShowMapsDialog = onShowMapsDialog,
        onOpenSettings = onOpenSettings,
        onCopyCode = onCopyCode,
        onLeave = onLeave,
        onEnemyMarkEnabled = onEnemyMarkEnabled,
    )
    CompassHudBluetoothCard(
        state = state,
        bluetoothDevicesCount = bluetoothDevicesCount,
        onRequestBluetoothScan = onRequestBluetoothScan,
    )
    CompassHudActionsRail(
        state = state,
        nowMs = nowMs,
        showMarkerPalette = showMarkerPalette,
        armedEnemyMarkType = armedEnemyMarkType,
        onEnemyMarkEnabled = onEnemyMarkEnabled,
        onArmedEnemyMarkTypeChange = onArmedEnemyMarkTypeChange,
        onShowMarkerPaletteChange = onShowMarkerPaletteChange,
        onTogglePlayerMode = onTogglePlayerMode,
        onSos = onSos,
    )
    CompassHudTeamCard(onOpenTeamRoster = onOpenTeamRoster)
}
