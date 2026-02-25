package com.example.teamcompass.ui

import com.example.teamcompass.MainDispatcherRule
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BackendAvailabilityCoordinatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun start_unavailableWithFreshSnapshot_keepsBackendAvailable() = runTest {
        var nowMs = 1_000L
        val backendFlow = MutableStateFlow(true)
        val repository = BackendAvailabilityFakeRepository(backendFlow = backendFlow)
        val coordinator = createCoordinator(
            staleWarningMs = 30_000L,
            nowMsProvider = { nowMs },
            repository = repository,
        )
        var state = UiState(
            tracking = TrackingUiState(
                telemetry = TelemetryState(
                    backendAvailable = true,
                    lastSnapshotAtMs = nowMs,
                    isBackendStale = false,
                ),
            ),
        )
        val errors = mutableListOf<String>()
        coordinator.bind(
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { message -> errors += message },
            scope = this,
        )

        coordinator.start(backendDownMessage = "backend down")
        runCurrent()
        backendFlow.value = false
        advanceUntilIdle()

        assertTrue(state.telemetry.backendAvailable)
        assertFalse(state.telemetry.isBackendStale)
        assertTrue(errors.isEmpty())
        coordinator.stop()
    }

    @Test
    fun refreshStaleFlag_marksUnavailable_andEmitsError() = runTest {
        var nowMs = 50_000L
        val backendFlow = MutableStateFlow(false)
        val repository = BackendAvailabilityFakeRepository(backendFlow = backendFlow)
        val coordinator = createCoordinator(
            staleWarningMs = 30_000L,
            nowMsProvider = { nowMs },
            repository = repository,
        )
        var state = UiState(
            tracking = TrackingUiState(
                telemetry = TelemetryState(
                    backendAvailable = true,
                    lastSnapshotAtMs = nowMs - 30_001L,
                    isBackendStale = false,
                ),
            ),
        )
        val errors = mutableListOf<String>()
        coordinator.bind(
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { message -> errors += message },
            scope = this,
        )

        coordinator.start(backendDownMessage = "backend down")
        runCurrent()
        coordinator.refreshStaleFlag(nowMs = nowMs, backendDownMessage = "backend down")
        advanceUntilIdle()

        assertFalse(state.telemetry.backendAvailable)
        assertTrue(state.telemetry.isBackendStale)
        assertTrue(state.telemetry.backendUnavailableSinceMs > 0L)
        assertEquals(listOf("backend down"), errors.distinct())
        coordinator.stop()
    }

    @Test
    fun stop_resetsBackendTelemetryState() = runTest {
        val repository = BackendAvailabilityFakeRepository(backendFlow = MutableStateFlow(true))
        val coordinator = createCoordinator(
            staleWarningMs = 30_000L,
            nowMsProvider = { 1_000L },
            repository = repository,
        )
        var state = UiState(
            tracking = TrackingUiState(
                telemetry = TelemetryState(
                    backendAvailable = false,
                    backendUnavailableSinceMs = 777L,
                    lastSnapshotAtMs = 888L,
                    isBackendStale = true,
                ),
            ),
            lastError = "backend down",
        )
        coordinator.bind(
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { _ -> Unit },
            scope = this,
        )

        coordinator.stop()

        assertTrue(state.telemetry.backendAvailable)
        assertEquals(0L, state.telemetry.backendUnavailableSinceMs)
        assertEquals(0L, state.telemetry.lastSnapshotAtMs)
        assertFalse(state.telemetry.isBackendStale)
    }

    private fun createCoordinator(
        staleWarningMs: Long,
        nowMsProvider: () -> Long,
        repository: TeamRepository,
    ): TestableBackendAvailabilityCoordinator {
        val monitor = BackendHealthMonitor(repository)
        val delegate = BackendHealthDelegate(
            backendHealthMonitor = monitor,
            staleWarningMs = staleWarningMs,
            nowMsProvider = nowMsProvider,
            delayFn = { _ -> Unit },
        )
        return TestableBackendAvailabilityCoordinator(delegate)
    }
}

private class TestableBackendAvailabilityCoordinator(
    private val delegate: BackendHealthDelegate,
) {
    private lateinit var coordinator: BackendAvailabilityCoordinator

    fun bind(
        readState: () -> UiState,
        updateState: ((UiState) -> UiState) -> Unit,
        emitError: (String) -> Unit,
        scope: kotlinx.coroutines.CoroutineScope,
    ) {
        coordinator = BackendAvailabilityCoordinator(
            backendHealthDelegate = delegate,
            scope = scope,
            readState = readState,
            updateState = updateState,
            emitError = emitError,
        )
    }

    fun start(backendDownMessage: String) {
        coordinator.start(backendDownMessage)
    }

    fun refreshStaleFlag(nowMs: Long, backendDownMessage: String) {
        coordinator.refreshStaleFlag(nowMs = nowMs, backendDownMessage = backendDownMessage)
    }

    fun stop() {
        coordinator.stop()
    }
}

private class BackendAvailabilityFakeRepository(
    private val backendFlow: MutableStateFlow<Boolean>,
) : TeamRepository {
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

    override fun observeBackendHealth(): Flow<Boolean> = backendFlow

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

    override suspend fun setActiveCommand(
        teamCode: String,
        uid: String,
        type: String,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

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
