package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.teamcompass.ui.components.TeamRosterItemUi
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun BoxScope.CompassHudTopLeftPanel(
    state: UiState,
    teamRoster: List<TeamRosterItemUi>,
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
) {
    val nearestRosterTop10 = remember(teamRoster) { teamRoster.take(10) }

    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(start = Spacing.xs, top = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.Top,
        ) {
            CompassHudMenuCard(
                state = state,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = onMenuExpandedChange,
                editMode = editMode,
                onEditModeChange = onEditModeChange,
                showMarkerPalette = showMarkerPalette,
                onShowMarkerPaletteChange = onShowMarkerPaletteChange,
                armedEnemyMarkType = armedEnemyMarkType,
                onArmedEnemyMarkTypeChange = onArmedEnemyMarkTypeChange,
                onShowStatusDialog = onShowStatusDialog,
                onShowHelpDialog = onShowHelpDialog,
                onAddPointAtCurrentLocation = onAddPointAtCurrentLocation,
                onShowMapsDialog = onShowMapsDialog,
                onOpenSettings = onOpenSettings,
                onCopyCode = onCopyCode,
                onLeave = onLeave,
                onEnemyMarkEnabled = onEnemyMarkEnabled,
            )
            CompassHudPositionCard(state = state, formatCoord = formatCoord)
        }

        CompassHudNearestAlliesCard(nearestRosterTop10 = nearestRosterTop10)
    }
}
