package com.example.teamcompass.ui

import com.example.teamcompass.core.PlayerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamSessionStateReducerTest {

    @Test
    fun clearTeamSession_resetsTeamBoundCollections_andDisablesEnemyMark_whenRequested() {
        val initial = UiState(
            team = TeamUiState(
                teamCode = "123456",
                players = listOf(),
                roleProfiles = listOf(),
                playerMode = PlayerMode.GAME,
            ),
            map = MapUiState(
                teamPoints = listOf(MapPoint("p1", 55.0, 37.0, "A", "flag", 1L, isTeam = true)),
                privatePoints = listOf(MapPoint("p2", 55.1, 37.1, "B", "flag", 2L, isTeam = false)),
                enemyPings = listOf(EnemyPing(id = "e1", lat = 55.0, lon = 37.0, createdAtMs = 3L)),
                markers = listOf(),
                enemyMarkEnabled = true,
            ),
            filter = FilterUiState(
                prioritizedTargets = listOf(),
                displayTargets = listOf(),
            ),
        )

        val cleared = TeamSessionStateReducer.clearTeamSession(
            state = initial,
            clearEnemyMarkEnabled = true,
        )

        assertEquals(null, cleared.team.teamCode)
        assertTrue(cleared.team.players.isEmpty())
        assertTrue(cleared.team.roleProfiles.isEmpty())
        assertTrue(cleared.map.teamPoints.isEmpty())
        assertTrue(cleared.map.privatePoints.isEmpty())
        assertTrue(cleared.map.enemyPings.isEmpty())
        assertTrue(cleared.map.markers.isEmpty())
        assertFalse(cleared.map.enemyMarkEnabled)
        assertTrue(cleared.filter.prioritizedTargets.isEmpty())
        assertTrue(cleared.filter.displayTargets.isEmpty())
    }

    @Test
    fun clearTeamSession_preservesEnemyMark_whenFlagIsFalse() {
        val initial = UiState(
            team = TeamUiState(teamCode = "123456"),
            map = MapUiState(enemyMarkEnabled = true),
        )

        val cleared = TeamSessionStateReducer.clearTeamSession(
            state = initial,
            clearEnemyMarkEnabled = false,
        )

        assertTrue(cleared.map.enemyMarkEnabled)
    }
}
