package com.example.teamcompass.ui

import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

internal data class MemberPrefsSyncJobs(
    val observerJob: Job? = null,
    val syncJob: Job? = null,
)

internal class MemberPrefsSyncWorker(
    private val teamRepository: TeamRepository,
    private val tacticalFiltersEnabled: Boolean,
    private val observerRetryInitialDelayMs: Long = 1_000L,
    private val observerRetryMaxDelayMs: Long = 10_000L,
) {
    constructor(
        teamRepository: TeamRepository,
        tacticalFiltersEnabled: Boolean,
    ) : this(
        teamRepository = teamRepository,
        tacticalFiltersEnabled = tacticalFiltersEnabled,
        observerRetryInitialDelayMs = 1_000L,
        observerRetryMaxDelayMs = 10_000L,
    )

    @OptIn(FlowPreview::class)
    /**
     * Launches observer/sync jobs in a scope owned by TeamCompassViewModel (viewModelScope).
     * The owner is responsible for cancelling returned jobs in onCleared() or session stop.
     */
    fun start(
        scope: CoroutineScope,
        teamCode: String,
        uid: String,
        targetFilterStateFlow: Flow<TargetFilterState>,
        toMemberPrefs: (TargetFilterState) -> TeamMemberPrefs,
        onRemotePrefs: (TeamMemberPrefs?) -> Unit,
        onObserverFailure: (Throwable) -> Unit,
        isUserDirty: () -> Boolean,
        onSyncSuccess: () -> Unit,
        onSyncFailure: (TeamActionFailure, Boolean) -> Unit,
    ): MemberPrefsSyncJobs {
        if (!tacticalFiltersEnabled) return MemberPrefsSyncJobs()

        val observerJob = scope.launch {
            var retryDelayMs = observerRetryInitialDelayMs
            while (true) {
                try {
                    var emittedThisAttempt = false
                    teamRepository.observeMemberPrefs(teamCode, uid).collectLatest { remotePrefs ->
                        if (!emittedThisAttempt) {
                            emittedThisAttempt = true
                            retryDelayMs = observerRetryInitialDelayMs
                        }
                        onRemotePrefs(remotePrefs)
                    }
                    return@launch
                } catch (err: CancellationException) {
                    throw err
                } catch (err: Throwable) {
                    onObserverFailure(err)
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(observerRetryMaxDelayMs)
                }
            }
        }
        val syncJob = scope.launch {
            targetFilterStateFlow
                .drop(1)
                .distinctUntilChanged()
                .debounce(500L)
                .collectLatest { filterState ->
                    val userInitiated = isUserDirty()
                    when (
                        val result = teamRepository.upsertMemberPrefs(
                            teamCode = teamCode,
                            uid = uid,
                            prefs = toMemberPrefs(filterState),
                        )
                    ) {
                        is TeamActionResult.Success -> onSyncSuccess()
                        is TeamActionResult.Failure -> onSyncFailure(result.details, userInitiated)
                    }
                }
        }
        return MemberPrefsSyncJobs(
            observerJob = observerJob,
            syncJob = syncJob,
        )
    }

    @OptIn(FlowPreview::class)
    fun start(
        scope: CoroutineScope,
        teamCode: String,
        uid: String,
        targetFilterStateFlow: Flow<TargetFilterState>,
        toMemberPrefs: (TargetFilterState) -> TeamMemberPrefs,
        onRemotePrefs: (TeamMemberPrefs?) -> Unit,
        isUserDirty: () -> Boolean,
        onSyncSuccess: () -> Unit,
        onSyncFailure: (TeamActionFailure, Boolean) -> Unit,
    ): MemberPrefsSyncJobs = start(
        scope = scope,
        teamCode = teamCode,
        uid = uid,
        targetFilterStateFlow = targetFilterStateFlow,
        toMemberPrefs = toMemberPrefs,
        onRemotePrefs = onRemotePrefs,
        onObserverFailure = {},
        isUserDirty = isUserDirty,
        onSyncSuccess = onSyncSuccess,
        onSyncFailure = onSyncFailure,
    )
}
