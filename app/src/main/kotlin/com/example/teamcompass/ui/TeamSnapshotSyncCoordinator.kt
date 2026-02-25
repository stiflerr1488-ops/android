package com.example.teamcompass.ui

/**
 * Coordinates team snapshot stream collection, UI state updates and backend health flags.
 */
internal class TeamSnapshotSyncCoordinator(
    private val teamSnapshotObserver: TeamSnapshotObserver,
    private val backendAvailabilityCoordinator: BackendAvailabilityCoordinator,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (message: String, cause: Throwable?) -> Unit,
    private val onSnapshotSideEffects: (enemyPings: List<EnemyPing>) -> Unit,
) {
    suspend fun collect(
        teamCode: String,
        uid: String,
        backendDownMessage: String,
    ) {
        teamSnapshotObserver.collectWithReconnect(
            teamCode = teamCode,
            uid = uid,
            viewModeProvider = { readState().viewMode },
            selfPointProvider = { readState().me },
            onSnapshot = { snapshot ->
                val nowMs = System.currentTimeMillis()
                val mappedTeam = snapshot.teamPoints.map { it.toUiMapPoint() }
                val mappedPrivate = snapshot.privatePoints.map { it.toUiMapPoint() }
                val mappedEnemy = snapshot.enemyPings.map { it.toUiEnemyPing() }
                val mappedMarkers = buildUnifiedMarkersForView(
                    teamPoints = mappedTeam,
                    privatePoints = mappedPrivate,
                    enemyPings = mappedEnemy,
                    viewerUid = uid,
                )
                updateState { state ->
                    state.copy(
                        team = state.team.copy(
                            teamCode = teamCode,
                            players = snapshot.players,
                            roleProfiles = snapshot.roleProfiles,
                            activeCommand = snapshot.activeCommand?.toUiQuickCommand(),
                        ),
                        map = state.map.copy(
                            teamPoints = mappedTeam,
                            privatePoints = mappedPrivate,
                            enemyPings = mappedEnemy,
                            markers = mappedMarkers,
                        ),
                        tracking = state.tracking.copy(
                            telemetry = state.tracking.telemetry.copy(
                                backendAvailable = true,
                                backendUnavailableSinceMs = 0L,
                                lastSnapshotAtMs = nowMs,
                                isBackendStale = false,
                            ),
                        ),
                        lastError = null,
                    )
                }
                backendAvailabilityCoordinator.scheduleStaleRefresh(
                    nowMs = nowMs,
                    backendDownMessage = backendDownMessage,
                )
                onSnapshotSideEffects(mappedEnemy)
            },
            onStreamError = { err, _ ->
                val nowMs = System.currentTimeMillis()
                updateState { state ->
                    val previous = state.tracking.telemetry
                    val backendUnavailableSinceMs = previous.backendUnavailableSinceMs
                        .takeIf { value -> value > 0L }
                        ?: nowMs
                    state.copy(
                        tracking = state.tracking.copy(
                            telemetry = state.tracking.telemetry.copy(
                                rtdbReadErrors = state.tracking.telemetry.rtdbReadErrors + 1,
                                backendAvailable = false,
                                backendUnavailableSinceMs = backendUnavailableSinceMs,
                                isBackendStale = backendAvailabilityCoordinator.computeBackendStale(
                                    lastSnapshotAtMs = previous.lastSnapshotAtMs,
                                    nowMs = nowMs,
                                ),
                            ),
                        ),
                    )
                }
                backendAvailabilityCoordinator.scheduleStaleRefresh(
                    nowMs = nowMs,
                    backendDownMessage = backendDownMessage,
                )
                if (readState().lastError != backendDownMessage) {
                    emitError(backendDownMessage, err)
                }
            },
        )
    }
}
