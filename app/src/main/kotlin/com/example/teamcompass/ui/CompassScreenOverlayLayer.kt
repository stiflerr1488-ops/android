package com.example.teamcompass.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.example.teamcompass.ui.components.TeamRosterBottomSheet
import com.example.teamcompass.ui.components.TeamRosterItemUi

@Composable
internal fun BoxScope.CompassScreenOverlayLayer(
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
    onPaletteSelect: (QuickCommandType) -> Unit,
    onPaletteCancel: () -> Unit,
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
    showAllTeamMembers: Boolean,
    onShowAllTeamMembersChange: (Boolean) -> Unit,
    showTargetsSheet: Boolean,
    onShowTargetsSheetChange: (Boolean) -> Unit,
    onOpenTeamRoster: () -> Unit,
    onEnableLocation: () -> Unit,
) {
    if (showMarkerPalette) {
        MarkerPaletteOverlay(
            onSelectEnemy = { onPaletteSelect(QuickCommandType.ENEMY) },
            onSelectDanger = { onPaletteSelect(QuickCommandType.DANGER) },
            onSelectAttack = { onPaletteSelect(QuickCommandType.ATTACK) },
            onSelectDefense = { onPaletteSelect(QuickCommandType.DEFENSE) },
            onCancel = onPaletteCancel,
        )
    }

    CompassHudOverlay(
        state = state,
        teamRoster = teamRoster,
        bluetoothDevicesCount = bluetoothDevicesCount,
        nowMs = nowMs,
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
        onTogglePlayerMode = onTogglePlayerMode,
        onSos = onSos,
        onRequestBluetoothScan = onRequestBluetoothScan,
        onOpenTeamRoster = onOpenTeamRoster,
    )

    LocationServicesDisabledCta(
        visible = state.hasLocationPermission && !state.isLocationServiceEnabled,
        onEnableLocation = onEnableLocation,
    )

    ActiveQuickCommandBanner(
        command = state.activeCommand,
        nowMs = nowMs,
    )

    if (showTargetsSheet) {
        TeamRosterBottomSheet(
            members = teamRoster,
            showAll = showAllTeamMembers,
            onShowAllChange = onShowAllTeamMembersChange,
            onEditMember = null,
            onDismiss = { onShowTargetsSheetChange(false) },
        )
    }
}
