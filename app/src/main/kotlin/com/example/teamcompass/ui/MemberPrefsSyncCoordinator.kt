package com.example.teamcompass.ui

import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamMemberPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Owns lifecycle of member prefs observer/sync jobs.
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class MemberPrefsSyncCoordinator(
    private val worker: MemberPrefsSyncWorker,
    private val scope: CoroutineScope,
    private val toMemberPrefs: (TargetFilterState) -> TeamMemberPrefs,
    private val onRemotePrefs: (TeamMemberPrefs?) -> Unit,
    private val onObserverFailure: (Throwable) -> Unit,
    private val isUserDirty: () -> Boolean,
    private val onSyncSuccess: () -> Unit,
    private val onSyncFailure: (failure: TeamActionFailure, userInitiated: Boolean) -> Unit,
) {
    private var observerJob: Job? = null
    private var syncJob: Job? = null

    fun start(
        teamCode: String,
        uid: String,
        targetFilterStateFlow: Flow<TargetFilterState>,
    ) {
        stop()
        val jobs = worker.start(
            scope = scope,
            teamCode = teamCode,
            uid = uid,
            targetFilterStateFlow = targetFilterStateFlow,
            toMemberPrefs = toMemberPrefs,
            onRemotePrefs = onRemotePrefs,
            onObserverFailure = onObserverFailure,
            isUserDirty = isUserDirty,
            onSyncSuccess = onSyncSuccess,
            onSyncFailure = onSyncFailure,
        )
        observerJob = jobs.observerJob
        syncJob = jobs.syncJob
    }

    fun stop() {
        observerJob?.cancel()
        observerJob = null
        syncJob?.cancel()
        syncJob = null
    }
}
