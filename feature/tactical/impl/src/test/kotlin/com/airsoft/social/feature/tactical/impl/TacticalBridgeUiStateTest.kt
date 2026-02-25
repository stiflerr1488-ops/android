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

        assertEquals("Radar", state.title)
        assertEquals("V BOI!", state.primaryActionLabel)
        assertEquals(
            "Radar mode entry point. Temporary bridge to legacy tactical implementation.",
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
            note = "Bridge is active.",
        )

        val uiState = snapshot.toBridgeUiState()

        assertTrue(uiState.body.contains("Note: Bridge is active."))
        assertTrue(uiState.body.contains("Stage: Hybrid bridge"))
        assertTrue(uiState.body.contains("Backend: custom-api+ws"))
        assertTrue(uiState.body.contains("Realtime: connected"))
        assertTrue(uiState.body.contains("Active team: team-red"))
    }
}
