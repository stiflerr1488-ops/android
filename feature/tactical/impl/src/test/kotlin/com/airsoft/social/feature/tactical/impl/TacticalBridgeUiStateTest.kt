package com.airsoft.social.feature.tactical.impl

import com.airsoft.social.core.tactical.TacticalMigrationStage
import com.airsoft.social.core.tactical.TacticalOverviewSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TacticalBridgeUiStateTest {

    @Test
    fun defaults_matchBridgeMessagingContract() {
        val state = TacticalBridgeUiState()

        assertEquals("Радар", state.title)
        assertEquals("В БОЙ!", state.primaryActionLabel)
        assertEquals(
            "Точка входа в режим радара. Временный мост к легаси-тактике.",
            state.body,
        )
    }

    @Test
    fun mapping_includesPortStatusDetails() {
        val snapshot = TacticalOverviewSnapshot(
            migrationStage = TacticalMigrationStage.HybridBridge,
            backendProvider = "custom-api+ws",
            realtimeConnected = true,
            activeTeamId = "team-red",
            note = "Мост активен.",
        )

        val uiState = snapshot.toBridgeUiState()

        assertTrue(uiState.body.contains("Примечание: Мост активен."))
        assertTrue(uiState.body.contains("Этап: Гибридный мост"))
        assertTrue(uiState.body.contains("Бэкенд: custom-api+ws"))
        assertTrue(uiState.body.contains("Реалтайм: подключен"))
        assertTrue(uiState.body.contains("Активная команда: team-red"))
    }
}
