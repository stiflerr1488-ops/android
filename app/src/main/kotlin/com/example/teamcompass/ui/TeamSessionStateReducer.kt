package com.example.teamcompass.ui

/**
 * Pure reducer for clearing team-bound UI state.
 */
internal object TeamSessionStateReducer {
    fun clearTeamSession(
        state: UiState,
        clearEnemyMarkEnabled: Boolean,
    ): UiState {
        return state.copy(
            team = state.team.copy(
                teamCode = null,
                players = emptyList(),
                roleProfiles = emptyList(),
                activeCommand = null,
            ),
            filter = state.filter.copy(
                prioritizedTargets = emptyList(),
                displayTargets = emptyList(),
            ),
            map = state.map.copy(
                teamPoints = emptyList(),
                privatePoints = emptyList(),
                enemyPings = emptyList(),
                markers = emptyList(),
                enemyMarkEnabled = if (clearEnemyMarkEnabled) false else state.map.enemyMarkEnabled,
            ),
        )
    }
}
