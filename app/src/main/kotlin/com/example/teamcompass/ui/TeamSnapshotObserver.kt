package com.example.teamcompass.ui

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

internal class TeamSnapshotObserver(
    private val teamRepository: TeamRepository,
    private val initialRetryDelayMs: Long,
    private val maxRetryDelayMs: Long,
    private val delayFn: suspend (Long) -> Unit = { delay(it) },
) {
    suspend fun collectWithReconnect(
        teamCode: String,
        uid: String,
        viewModeProvider: () -> TeamViewMode,
        selfPointProvider: () -> LocationPoint?,
        onSnapshot: suspend (TeamSnapshot) -> Unit,
        onStreamError: suspend (Throwable, Long) -> Unit,
    ) {
        var retryDelayMs = initialRetryDelayMs
        while (true) {
            try {
                teamRepository.observeTeam(
                    teamCode = teamCode,
                    uid = uid,
                    viewMode = viewModeProvider(),
                    selfPoint = selfPointProvider(),
                ).collectLatest { snapshot ->
                    onSnapshot(snapshot)
                    retryDelayMs = initialRetryDelayMs
                }
                return
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (err: Throwable) {
                onStreamError(err, retryDelayMs)
                delayFn(retryDelayMs)
                retryDelayMs = (retryDelayMs * 2L).coerceAtMost(maxRetryDelayMs)
            }
        }
    }
}

