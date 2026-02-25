package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackendHealthDelegateTest {

    @Test
    fun computeBackendStale_noSnapshot_returnsFalse() {
        val delegate = createDelegate()
        assertFalse(delegate.computeBackendStale(lastSnapshotAtMs = 0L, nowMs = 100L))
    }

    @Test
    fun computeBackendStale_afterThreshold_returnsTrue() {
        val delegate = createDelegate(staleWarningMs = 30_000L)
        assertTrue(delegate.computeBackendStale(lastSnapshotAtMs = 1_000L, nowMs = 31_001L))
    }

    @Test
    fun scheduleStaleRefresh_withoutSnapshot_refreshesImmediately() = runTest {
        val delegate = createDelegate()
        val refreshedAt = mutableListOf<Long>()

        delegate.scheduleStaleRefresh(
            scope = this,
            nowMs = 2_000L,
            readLastSnapshotAtMs = { 0L },
            onRefresh = { nowMs -> refreshedAt += nowMs },
        )

        assertEquals(listOf(2_000L), refreshedAt)
    }

    @Test
    fun scheduleStaleRefresh_withSnapshot_refreshesAfterDelay() = runTest {
        var nowMs = 1_000L
        val delegate = createDelegate(
            staleWarningMs = 30_000L,
            nowMsProvider = { nowMs },
        )
        val refreshedAt = mutableListOf<Long>()

        delegate.scheduleStaleRefresh(
            scope = this,
            nowMs = nowMs,
            readLastSnapshotAtMs = { 1_000L },
            onRefresh = { refreshed -> refreshedAt += refreshed },
        )

        advanceTimeBy(30_000L)
        runCurrent()
        assertTrue(refreshedAt.isEmpty())

        nowMs = 31_001L
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(listOf(31_001L), refreshedAt)
    }

    @Test
    fun startHealthMonitor_forwardsSamplesWithPreviousAvailability() = runTest {
        val repository = BackendHealthFakeRepository()
        val delegate = createDelegate(repository = repository)
        val samples = mutableListOf<Sample>()

        repository.backendAvailability.tryEmit(true)
        delegate.startHealthMonitor(scope = this) { available, previous, nowMs ->
            samples += Sample(available = available, previous = previous, nowMs = nowMs)
        }
        runCurrent()

        repository.backendAvailability.emit(false)
        runCurrent()

        assertEquals(2, samples.size)
        assertEquals(Sample(available = true, previous = null, nowMs = 10_000L), samples[0])
        assertEquals(Sample(available = false, previous = true, nowMs = 10_000L), samples[1])
        delegate.stop()
    }

    private fun createDelegate(
        repository: BackendHealthFakeRepository = BackendHealthFakeRepository(),
        staleWarningMs: Long = 30_000L,
        nowMsProvider: () -> Long = { 10_000L },
    ): BackendHealthDelegate {
        return BackendHealthDelegate(
            backendHealthMonitor = BackendHealthMonitor(repository),
            staleWarningMs = staleWarningMs,
            nowMsProvider = nowMsProvider,
        )
    }
}

private data class Sample(
    val available: Boolean,
    val previous: Boolean?,
    val nowMs: Long,
)

private class BackendHealthFakeRepository : TeamRepository {
    val backendAvailability = MutableSharedFlow<Boolean>(replay = 1)

    override fun observeBackendHealth(): Flow<Boolean> = backendAvailability

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
        selfPoint: com.example.teamcompass.core.LocationPoint?,
    ): Flow<TeamSnapshot> = emptyFlow()

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

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> = emptyFlow()

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}
