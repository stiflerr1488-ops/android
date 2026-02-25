package com.example.teamcompass.ui

import android.app.Application
import android.util.Log
import com.example.teamcompass.R
import com.example.teamcompass.domain.TeamActionFailure
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class TeamCompassSessionListeningCoordinator(
    private val scope: CoroutineScope,
    private val coroutineExceptionHandler: CoroutineExceptionHandler,
    private val application: Application,
    private val teamSessionDelegate: TeamSessionDelegate,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val normalizeTeamCode: (String?) -> String?,
    private val emitError: (String) -> Unit,
    private val clearStoredTeamCode: () -> Unit,
    private val startBackendAvailability: (String) -> Unit,
    private val stopBackendAvailability: () -> Unit,
    private val startP2PInbound: (String, String) -> Unit,
    private val stopP2PInbound: () -> Unit,
    private val startMemberPrefsSync: (String, String) -> Unit,
    private val stopMemberPrefsSync: () -> Unit,
    private val collectTeamSnapshot: suspend (String, String, String) -> Unit,
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
    private val logTag: String,
) {
    private var teamObserverJob: Job? = null

    fun startListening(codeRaw: String) {
        startListeningInternal(codeRaw = codeRaw, ensureMembership = true)
    }

    fun startListeningWithoutMembershipEnsure(codeRaw: String) {
        startListeningInternal(codeRaw = codeRaw, ensureMembership = false)
    }

    private fun startListeningInternal(
        codeRaw: String,
        ensureMembership: Boolean,
    ) {
        if (!readState().isAuthReady) return
        val uid = readState().uid ?: return
        val traceId = newTraceId("startListening")
        logActionStart("startListening", traceId, codeRaw, uid)

        val code = normalizeTeamCode(codeRaw)
        if (code == null) {
            emitError(application.getString(R.string.vm_error_team_code_invalid))
            logActionFailure("startListening", traceId, null, "invalid team code", codeRaw, uid)
            stopListening()
            return
        }

        teamObserverJob?.cancel()
        teamObserverJob = scope.launch(coroutineExceptionHandler) {
            val callsign = readState().callsign.ifBlank { application.getString(R.string.default_callsign_player) }
            if (ensureMembership) {
                when (
                    val preflightResult = teamSessionDelegate.preflightStartListening(
                        codeRaw = code,
                        uid = uid,
                        callsign = callsign,
                    )
                ) {
                    is TeamListeningPreflightResult.Ready -> Unit
                    is TeamListeningPreflightResult.InvalidCode -> {
                        emitError(application.getString(R.string.vm_error_team_code_invalid))
                        logActionFailure(
                            "startListening",
                            traceId,
                            null,
                            "invalid team code",
                            preflightResult.rawCode,
                            uid,
                        )
                        stopListening()
                        return@launch
                    }

                    is TeamListeningPreflightResult.JoinFailure -> {
                        if (
                            handleStartListeningTerminalFailure(
                                failure = preflightResult.failure,
                                teamCode = preflightResult.teamCode,
                                uid = uid,
                                traceId = traceId,
                            )
                        ) {
                            return@launch
                        }
                        Log.w(logTag, "Membership ensure failed for team=$code", preflightResult.failure.cause)
                        logActionFailure(
                            "startListening",
                            traceId,
                            preflightResult.failure.cause,
                            preflightResult.failure.message,
                            code,
                            uid,
                        )
                    }
                }
            }

            val backendDownMessage = application.getString(R.string.vm_error_backend_unavailable_retrying)
            startBackendAvailability(backendDownMessage)
            startP2PInbound(code, uid)
            logActionSuccess("startListening", traceId, code, uid)
            collectTeamSnapshot(code, uid, backendDownMessage)
        }

        startMemberPrefsSync(code, uid)
    }

    fun stopListening() {
        teamObserverJob?.cancel()
        teamObserverJob = null
        stopP2PInbound()
        stopBackendAvailability()
        stopMemberPrefsSync()
    }

    fun clearTeamSessionState(clearEnemyMarkEnabled: Boolean) {
        updateState { state ->
            TeamSessionStateReducer.clearTeamSession(
                state = state,
                clearEnemyMarkEnabled = clearEnemyMarkEnabled,
            )
        }
    }

    private fun clearTeamSessionStateForTerminalFailure() {
        clearTeamSessionState(clearEnemyMarkEnabled = true)
    }

    private fun handleStartListeningTerminalFailure(
        failure: TeamActionFailure,
        teamCode: String,
        uid: String,
        traceId: String,
    ): Boolean {
        val policy = TeamListeningFailurePolicy.resolve(failure.error) ?: return false
        val userMessage = application.getString(policy.userMessageResId, teamCode)
        emitError(userMessage)
        logActionFailure("startListening", traceId, null, policy.logReason, teamCode, uid)
        clearTeamSessionStateForTerminalFailure()
        clearStoredTeamCode()
        return true
    }
}
