package com.airsoft.social.feature.tactical.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airsoft.social.core.tactical.TacticalMigrationStage
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.core.tactical.TacticalOverviewSnapshot
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.tactical.api.TacticalFeatureApi

data class TacticalBridgeUiState(
    val title: String = "Radar",
    val body: String = "Radar mode entry point. Temporary bridge to legacy tactical implementation.",
    val primaryActionLabel: String = "V BOI!",
)

sealed interface TacticalBridgeAction {
    data object OpenLegacyTacticalClicked : TacticalBridgeAction
}

@Composable
fun TacticalBridgeRoute(
    tacticalOverviewPort: TacticalOverviewPort,
    onOpenLegacyTactical: () -> Unit = {},
) {
    val snapshot by tacticalOverviewPort
        .observeOverview()
        .collectAsStateWithLifecycle(initialValue = TacticalOverviewSnapshot())

    TacticalBridgeScreen(
        uiState = snapshot.toBridgeUiState(),
        onAction = { action ->
            when (action) {
                TacticalBridgeAction.OpenLegacyTacticalClicked -> onOpenLegacyTactical()
            }
        },
    )
}

@Composable
fun TacticalBridgeScreen(
    uiState: TacticalBridgeUiState,
    onAction: (TacticalBridgeAction) -> Unit,
) {
    WireframePage(
        title = uiState.title,
        subtitle = uiState.body,
        primaryActionLabel = uiState.primaryActionLabel,
        onPrimaryAction = { onAction(TacticalBridgeAction.OpenLegacyTacticalClicked) },
    ) {
        WireframeSection(
            title = TacticalFeatureApi.contract.title,
            subtitle = "Skeleton of the new tactical feature. Real radar logic is still launched via legacy bridge.",
        ) {
            WireframeChipRow(
                labels = listOf("Radar", "Team", "Map", "Positions", "Comms"),
            )
        }
        WireframeSection(
            title = "Bridge State",
            subtitle = "Data comes from TacticalOverviewPort (currently via app seam).",
        ) {
            uiState.body.lines()
                .filter { it.isNotBlank() }
                .forEachIndexed { index, line ->
                    val parts = line.split(":", limit = 2)
                    val title = parts.firstOrNull()?.trim().orEmpty()
                    val value = parts.getOrNull(1)?.trim() ?: line
                    WireframeItemRow(
                        title = if (title.isBlank()) "Status ${index + 1}" else title,
                        subtitle = value,
                    )
                }
        }
    }
}

internal fun TacticalOverviewSnapshot.toBridgeUiState(): TacticalBridgeUiState {
    val stageLabel = when (migrationStage) {
        TacticalMigrationStage.LegacyBridge -> "Legacy bridge"
        TacticalMigrationStage.HybridBridge -> "Hybrid bridge"
        TacticalMigrationStage.NativeFeature -> "Native feature"
    }
    val realtimeLabel = if (realtimeConnected) "connected" else "offline"
    val teamLabel = activeTeamId ?: "none"

    return TacticalBridgeUiState(
        body = buildString {
            appendLine("Note: $note")
            appendLine("Stage: $stageLabel")
            appendLine("Backend: $backendProvider")
            appendLine("Realtime: $realtimeLabel")
            append("Active team: $teamLabel")
        },
    )
}
