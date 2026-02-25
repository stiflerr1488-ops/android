package com.airsoft.social.app

import com.airsoft.social.core.tactical.TacticalMigrationStage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTacticalOverviewBridgeTest {

    @Test
    fun legacyShellState_updatesSnapshotForHealthyConnectedTeam() = runBlocking {
        val bridge = LegacyTacticalOverviewBridge()

        bridge.onLegacyShellStateChanged(
            isAuthReady = true,
            teamCode = "TEAM42",
            backendAvailable = true,
            isBackendStale = false,
        )

        val snapshot = bridge.observeOverview().first()
        assertEquals(TacticalMigrationStage.LegacyBridge, snapshot.migrationStage)
        assertEquals("legacy-firebase-rtdb", snapshot.backendProvider)
        assertTrue(snapshot.realtimeConnected)
        assertEquals("TEAM42", snapshot.activeTeamId)
        assertTrue(snapshot.note.contains("active through the bridge"))
    }

    @Test
    fun legacyShellState_marksBackendUnavailableAndDisconnected() = runBlocking {
        val bridge = LegacyTacticalOverviewBridge()

        bridge.onLegacyShellStateChanged(
            isAuthReady = true,
            teamCode = "TEAM42",
            backendAvailable = false,
            isBackendStale = false,
        )

        val snapshot = bridge.observeOverview().first()
        assertFalse(snapshot.realtimeConnected)
        assertTrue(snapshot.note.contains("unavailable"))
    }
}
