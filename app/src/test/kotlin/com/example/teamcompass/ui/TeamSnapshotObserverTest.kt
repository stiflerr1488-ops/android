package com.example.teamcompass.ui

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamSnapshotObserverTest {

    @Test
    fun teamObserver_reconnect_backoff_caps_at_20s() = runTest {
        val repository = TeamSnapshotObserverFakeRepository(failuresBeforeSuccess = 6)
        val delays = mutableListOf<Long>()
        val seenRetryDelays = mutableListOf<Long>()
        val observer = TeamSnapshotObserver(
            teamRepository = repository,
            initialRetryDelayMs = 1_500L,
            maxRetryDelayMs = 20_000L,
            delayFn = { delayMs -> delays += delayMs },
        )

        observer.collectWithReconnect(
            teamCode = "123456",
            uid = "uid-1",
            viewModeProvider = { TeamViewMode.COMBAT },
            selfPointProvider = { null },
            onSnapshot = { },
            onStreamError = { _, retryDelayMs -> seenRetryDelays += retryDelayMs },
        )

        assertEquals(listOf(1_500L, 3_000L, 6_000L, 12_000L, 20_000L, 20_000L), delays)
        assertEquals(delays, seenRetryDelays)
        assertEquals(7, repository.observeCalls)
        assertTrue(repository.successDelivered)
    }
}

private class TeamSnapshotObserverFakeRepository(
    private val failuresBeforeSuccess: Int,
) : TeamRepository {
    var observeCalls: Int = 0
    var successDelivered: Boolean = false

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> = TeamActionResult.Success("000001")

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
    ): Flow<TeamSnapshot> {
        observeCalls += 1
        if (observeCalls <= failuresBeforeSuccess) {
            return flow { throw IllegalStateException("simulated stream failure #$observeCalls") }
        }
        return flowOf(TeamSnapshot()).also { successDelivered = true }
    }

    override suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit> {
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> = TeamActionResult.Success("p-1")

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

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> = flowOf(null)

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}

