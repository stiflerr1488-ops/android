package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Executes tactical write actions (points/enemy pings/quick commands).
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class TacticalActionsCoordinator(
    private val teamRepository: TeamRepository,
    private val scope: CoroutineScope,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (String) -> Unit,
    private val handleActionFailure: (String, TeamActionFailure) -> Unit,
    private val newTraceId: (String) -> String,
    private val logActionStart: (action: String, traceId: String, teamCode: String?, uid: String?) -> Unit,
    private val logActionSuccess: (action: String, traceId: String, teamCode: String?, uid: String?) -> Unit,
    private val logActionFailure: (
        action: String,
        traceId: String,
        throwable: Throwable?,
        message: String?,
        teamCode: String?,
        uid: String?,
    ) -> Unit,
    private val enemyPingTtlMs: Long = DEFAULT_ENEMY_PING_TTL_MS,
) {
    fun addPointAt(
        lat: Double,
        lon: Double,
        label: String,
        icon: String,
        forTeam: Boolean,
        addPointFailedMessage: String,
        invalidInputMessage: String,
    ) {
        if (!hasValidCoordinates(lat = lat, lon = lon)) {
            emitError(invalidInputMessage)
            return
        }
        val state = readState()
        val code = state.teamCode ?: return
        val uid = state.uid ?: return
        val traceId = newTraceId("addPoint")
        logActionStart("addPoint", traceId, code, uid)

        scope.launch {
            val result = teamRepository.addPoint(
                teamCode = code,
                uid = uid,
                payload = TeamPointPayload(
                    lat = lat,
                    lon = lon,
                    label = label,
                    icon = icon,
                ),
                forTeam = forTeam,
            )
            if (result is TeamActionResult.Failure) {
                handleActionFailure(addPointFailedMessage, result.details)
                logActionFailure(
                    "addPoint",
                    traceId,
                    result.details.cause,
                    result.details.message,
                    code,
                    uid,
                )
            } else {
                logActionSuccess("addPoint", traceId, code, uid)
            }
        }
    }

    fun updatePoint(
        id: String,
        lat: Double,
        lon: Double,
        label: String,
        icon: String,
        isTeam: Boolean,
        onlyAuthorEditMessage: String,
        updatePointFailedMessage: String,
        invalidInputMessage: String,
    ) {
        if (!hasValidCoordinates(lat = lat, lon = lon)) {
            emitError(invalidInputMessage)
            return
        }
        val state = readState()
        val code = state.teamCode ?: return
        val uid = state.uid ?: return

        if (isTeam) {
            val marker = state.teamPoints.firstOrNull { it.id == id }?.toUnifiedMarker()
            if (marker != null && !MarkerCorePolicies.canEdit(marker, uid)) {
                emitError(onlyAuthorEditMessage)
                return
            }
        }

        scope.launch {
            val result = teamRepository.updatePoint(
                teamCode = code,
                uid = uid,
                pointId = id,
                payload = TeamPointUpdatePayload(
                    lat = lat,
                    lon = lon,
                    label = label,
                    icon = icon,
                ),
                isTeam = isTeam,
            )
            if (result is TeamActionResult.Failure) {
                handleActionFailure(updatePointFailedMessage, result.details)
            }
        }
    }

    fun deletePoint(
        id: String,
        isTeam: Boolean,
        onlyAuthorDeleteMessage: String,
        deletePointFailedMessage: String,
    ) {
        val state = readState()
        val code = state.teamCode ?: return
        val uid = state.uid ?: return

        if (isTeam) {
            val marker = state.teamPoints.firstOrNull { it.id == id }?.toUnifiedMarker()
            if (marker != null && !MarkerCorePolicies.canDelete(marker, uid)) {
                emitError(onlyAuthorDeleteMessage)
                return
            }
        }

        scope.launch {
            val result = teamRepository.deletePoint(
                teamCode = code,
                uid = uid,
                pointId = id,
                isTeam = isTeam,
            )
            if (result is TeamActionResult.Failure) {
                handleActionFailure(deletePointFailedMessage, result.details)
            }
        }
    }

    fun sendQuickCommand(
        type: QuickCommandType,
        quickCommandFailedMessage: String,
    ) {
        updateState {
            it.copy(
                map = it.map.copy(enemyMarkType = type, enemyMarkEnabled = true),
            )
        }
        val state = readState()
        val code = state.teamCode ?: return
        val uid = state.uid ?: return
        val traceId = newTraceId("setActiveCommand")
        logActionStart("setActiveCommand", traceId, code, uid)

        scope.launch {
            when (
                val result = teamRepository.setActiveCommand(
                    teamCode = code,
                    uid = uid,
                    type = type.name,
                )
            ) {
                is TeamActionResult.Success -> logActionSuccess("setActiveCommand", traceId, code, uid)
                is TeamActionResult.Failure -> {
                    handleActionFailure(quickCommandFailedMessage, result.details)
                    logActionFailure(
                        "setActiveCommand",
                        traceId,
                        result.details.cause,
                        result.details.message,
                        code,
                        uid,
                    )
                }
            }
        }
    }

    /**
     * Backward-compatible overload for recovered binary TeamCompassViewModel.class.
     * Keeps quick-command backend write behavior enabled after VM source loss recovery.
     */
    fun sendQuickCommand(type: QuickCommandType) {
        sendQuickCommand(
            type = type,
            quickCommandFailedMessage = DEFAULT_QUICK_COMMAND_FAILED_MESSAGE,
        )
    }

    fun addEnemyPing(
        lat: Double,
        lon: Double,
        type: QuickCommandType,
        enemyMarkFailedMessage: String,
        invalidInputMessage: String,
    ) {
        if (!hasValidCoordinates(lat = lat, lon = lon)) {
            emitError(invalidInputMessage)
            return
        }
        val state = readState()
        val code = state.teamCode ?: return
        val uid = state.uid ?: return
        val traceId = newTraceId("addEnemyPing")
        logActionStart("addEnemyPing", traceId, code, uid)

        scope.launch {
            val result = teamRepository.addEnemyPing(
                teamCode = code,
                uid = uid,
                lat = lat,
                lon = lon,
                type = type.name,
                ttlMs = enemyPingTtlMs,
            )
            if (result is TeamActionResult.Failure) {
                handleActionFailure(enemyMarkFailedMessage, result.details)
                logActionFailure(
                    "addEnemyPing",
                    traceId,
                    result.details.cause,
                    result.details.message,
                    code,
                    uid,
                )
            } else {
                logActionSuccess("addEnemyPing", traceId, code, uid)
            }
        }
    }

    private companion object {
        private const val DEFAULT_ENEMY_PING_TTL_MS = 120_000L
        private const val DEFAULT_QUICK_COMMAND_FAILED_MESSAGE = "Не удалось отправить быструю команду"
    }

    private fun hasValidCoordinates(lat: Double, lon: Double): Boolean {
        if (!lat.isFinite() || !lon.isFinite()) return false
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
}
