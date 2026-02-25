package com.airsoft.social.core.tactical

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTacticalOverviewPortTest {

    @Test
    fun update_changesObservedSnapshot() = runBlocking {
        val port = FakeTacticalOverviewPort()
        port.update {
            it.copy(
                migrationStage = TacticalMigrationStage.HybridBridge,
                realtimeConnected = true,
                activeTeamId = "alpha-1",
            )
        }

        val snapshot = port.observeOverview().first()
        assertEquals(TacticalMigrationStage.HybridBridge, snapshot.migrationStage)
        assertTrue(snapshot.realtimeConnected)
        assertEquals("alpha-1", snapshot.activeTeamId)
    }
}
