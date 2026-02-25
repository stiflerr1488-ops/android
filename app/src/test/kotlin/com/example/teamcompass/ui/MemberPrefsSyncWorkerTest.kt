package com.example.teamcompass.ui

import com.example.teamcompass.MainDispatcherRule
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPoint
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MemberPrefsSyncWorkerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun observer_retries_silently_after_failure_and_delivers_remote_prefs() = runTest {
        val expectedPrefs = TeamMemberPrefs(
            preset = "NEAR",
            nearRadiusM = 150,
            showDead = false,
            showStale = true,
            focusMode = true,
            updatedAtMs = 1L,
        )
        val repository = FakeMemberPrefsRepository(
            observeMemberPrefsFlowFactory = {
                flow {
                    if (observeMemberPrefsCalls == 1) {
                        throw IllegalStateException("boom")
                    }
                    emit(expectedPrefs)
                }
            },
        )
        val worker = MemberPrefsSyncWorker(
            teamRepository = repository,
            tacticalFiltersEnabled = true,
            observerRetryInitialDelayMs = 1L,
            observerRetryMaxDelayMs = 4L,
        )
        val observedRemotePrefs = mutableListOf<TeamMemberPrefs?>()
        val observerFailures = mutableListOf<Throwable>()

        val jobs = worker.start(
            scope = this,
            teamCode = "123456",
            uid = "uid-1",
            targetFilterStateFlow = MutableStateFlow(TargetFilterState()),
            toMemberPrefs = ::defaultMemberPrefs,
            onRemotePrefs = { observedRemotePrefs += it },
            onObserverFailure = { observerFailures += it },
            isUserDirty = { false },
            onSyncSuccess = {},
            onSyncFailure = { _, _ -> error("sync failure not expected") },
        )
        advanceUntilIdle()

        jobs.syncJob?.cancel()

        assertEquals(2, repository.observeMemberPrefsCalls)
        assertEquals(1, observerFailures.size)
        assertEquals(listOf(expectedPrefs), observedRemotePrefs)
    }

    @Test
    fun observer_cancellation_does_not_retry() = runTest {
        val repository = FakeMemberPrefsRepository(
            observeMemberPrefsFlowFactory = {
                flow {
                    awaitCancellation()
                }
            },
        )
        val worker = MemberPrefsSyncWorker(
            teamRepository = repository,
            tacticalFiltersEnabled = true,
            observerRetryInitialDelayMs = 1L,
            observerRetryMaxDelayMs = 4L,
        )
        val observerFailures = mutableListOf<Throwable>()

        val jobs = worker.start(
            scope = this,
            teamCode = "123456",
            uid = "uid-2",
            targetFilterStateFlow = MutableStateFlow(TargetFilterState()),
            toMemberPrefs = ::defaultMemberPrefs,
            onRemotePrefs = {},
            onObserverFailure = { observerFailures += it },
            isUserDirty = { false },
            onSyncSuccess = {},
            onSyncFailure = { _, _ -> error("sync failure not expected") },
        )

        runCurrent()
        jobs.observerJob?.cancel()
        jobs.syncJob?.cancel()
        advanceUntilIdle()

        assertEquals(1, repository.observeMemberPrefsCalls)
        assertTrue(observerFailures.isEmpty())
    }

    private fun defaultMemberPrefs(@Suppress("UNUSED_PARAMETER") state: TargetFilterState): TeamMemberPrefs {
        return TeamMemberPrefs(
            preset = "ALL",
            nearRadiusM = 200,
            showDead = true,
            showStale = true,
            focusMode = false,
            updatedAtMs = 0L,
        )
    }
}

private class FakeMemberPrefsRepository(
    private val observeMemberPrefsFlowFactory: FakeMemberPrefsRepository.() -> Flow<TeamMemberPrefs?>,
) : TeamRepository {
    var observeMemberPrefsCalls: Int = 0

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> = TeamActionResult.Success("123456")

    override suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode,
        selfPoint: LocationPoint?,
    ): Flow<TeamSnapshot> = MutableStateFlow(TeamSnapshot())

    override suspend fun upsertState(
        teamCode: String,
        uid: String,
        payload: TeamStatePayload,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> = TeamActionResult.Success("point")

    override suspend fun updatePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        payload: TeamPointUpdatePayload,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override suspend fun deletePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override suspend fun setActiveCommand(teamCode: String, uid: String, type: String): TeamActionResult<Unit> {
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> {
        observeMemberPrefsCalls += 1
        return observeMemberPrefsFlowFactory()
    }

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}
