package com.airsoft.social.feature.tactical.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airsoft.social.core.tactical.TacticalMigrationStage
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.core.tactical.TacticalOverviewSnapshot
import com.airsoft.social.core.ui.ForceLandscapeOrientation
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.tactical.api.TacticalFeatureApi

data class TacticalBridgeUiState(
    val title: String = "Радар",
    val body: String = "Точка входа в режим радара. Временный мост к легаси-тактике.",
    val primaryActionLabel: String = "В БОЙ!",
)

sealed interface TacticalBridgeAction {
    data object OpenLegacyTacticalClicked : TacticalBridgeAction
}

@Composable
fun TacticalBridgeRoute(
    tacticalOverviewPort: TacticalOverviewPort,
    onOpenLegacyTactical: () -> Unit = {},
) {
    ForceLandscapeOrientation()
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
            subtitle = "Каркас новой тактической фичи. Реальная логика радара пока запускается через легаси-мост.",
        ) {
            WireframeChipRow(
                labels = listOf("Радар", "Команда", "Карта", "Позиции", "Связь"),
            )
        }
        WireframeSection(
            title = "Состояние моста",
            subtitle = "Данные приходят из порта тактики (сейчас через переходный слой в приложении).",
        ) {
            uiState.body.lines()
                .filter { it.isNotBlank() }
                .forEachIndexed { index, line ->
                    val parts = line.split(":", limit = 2)
                    val title = parts.firstOrNull()?.trim().orEmpty()
                    val value = parts.getOrNull(1)?.trim() ?: line
                    WireframeItemRow(
                        title = if (title.isBlank()) "Статус ${index + 1}" else title,
                        subtitle = value,
                    )
                }
        }
    }
}

internal fun TacticalOverviewSnapshot.toBridgeUiState(): TacticalBridgeUiState {
    val stageLabel = when (migrationStage) {
        TacticalMigrationStage.LegacyBridge -> "Легаси-мост"
        TacticalMigrationStage.HybridBridge -> "Гибридный мост"
        TacticalMigrationStage.NativeFeature -> "Нативная фича"
    }
    val realtimeLabel = if (realtimeConnected) "подключен" else "офлайн"
    val teamLabel = activeTeamId ?: "нет"

    return TacticalBridgeUiState(
        body = buildString {
            appendLine("Примечание: $note")
            appendLine("Этап: $stageLabel")
            appendLine("Бэкенд: $backendProvider")
            appendLine("Реалтайм: $realtimeLabel")
            append("Активная команда: $teamLabel")
        },
    )
}
