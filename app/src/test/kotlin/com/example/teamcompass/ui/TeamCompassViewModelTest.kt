package com.example.teamcompass.ui

import android.Manifest
import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.example.teamcompass.MainDispatcherRule
import com.example.teamcompass.auth.IdentityLinkingEligibility
import com.example.teamcompass.auth.IdentityLinkingService
import com.example.teamcompass.auth.NoOpIdentityLinkingService
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPoint
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.domain.TrackingTelemetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.Shadows

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TeamCompassViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun createTeam_success_updatesTeamCode() = runTest {
        val repository = FakeTeamRepository().apply {
            createTeamResult = TeamActionResult.Success("123456")
        }
        val controller = FakeTrackingController()
        val vm = createVm(repository, controller)
        vm.setUiForTest { it.withLegacy(uid = "uid-1", callsign = "Alpha") }

        vm.createTeam()
        advanceUntilIdle()

        assertEquals("123456", vm.ui.value.teamCode)
        assertEquals("uid-1", repository.lastCreateOwnerUid)
        assertEquals("Alpha", repository.lastCreateOwnerCallsign)
    }

    @Test
    fun restoresCriticalState_fromSavedStateHandle() = runTest {
        val savedState = SavedStateHandle(
            mapOf(
                "state_team_code" to "123456",
                "state_default_mode" to TrackingMode.SILENT.name,
                "state_player_mode" to PlayerMode.DEAD.name,
                "state_is_tracking" to true,
                "state_my_sos_until_ms" to 42_000L,
            ),
        )
        val vm = createVm(
            repository = FakeTeamRepository(),
            controller = FakeTrackingController(),
            autoStart = false,
            savedStateHandle = savedState,
        )

        assertEquals("123456", vm.ui.value.teamCode)
        assertTrue(vm.ui.value.isTracking)
        assertEquals(TrackingMode.SILENT, vm.ui.value.defaultMode)
        assertEquals(PlayerMode.DEAD, vm.ui.value.playerMode)
        assertEquals(42_000L, vm.ui.value.mySosUntilMs)
    }

    @Test
    fun updatesSavedStateHandle_whenUiStateChanges() = runTest {
        val savedState = SavedStateHandle()
        val vm = createVm(
            repository = FakeTeamRepository(),
            controller = FakeTrackingController(),
            autoStart = false,
            savedStateHandle = savedState,
        )

        vm.setUiForTest {
            it.copy(
                team = it.team.copy(
                    teamCode = "654321",
                    playerMode = PlayerMode.DEAD,
                    mySosUntilMs = 99_000L,
                ),
                tracking = it.tracking.copy(
                    defaultMode = TrackingMode.SILENT,
                    isTracking = true,
                ),
            )
        }
        runCurrent()

        assertEquals("654321", savedState.get<String>("state_team_code"))
        assertEquals(TrackingMode.SILENT.name, savedState.get<String>("state_default_mode"))
        assertEquals(PlayerMode.DEAD.name, savedState.get<String>("state_player_mode"))
        assertEquals(true, savedState.get<Boolean>("state_is_tracking"))
        assertEquals(99_000L, savedState.get<Long>("state_my_sos_until_ms"))
    }

    @Test
    fun locationServicePollInterval_trackingMode_isFast() {
        assertEquals(2_000L, TeamCompassViewModel.locationServicePollIntervalMs(isTracking = true))
    }

    @Test
    fun locationServicePollInterval_idleMode_isSlow() {
        assertEquals(12_000L, TeamCompassViewModel.locationServicePollIntervalMs(isTracking = false))
    }

    @Test
    fun createTeam_failure_setsDomainError() = runTest {
        val repository = FakeTeamRepository().apply {
            createTeamResult = TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.PERMISSION_DENIED, "permission denied")
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-2", callsign = "Bravo") }

        vm.createTeam()
        advanceUntilIdle()

        assertTrue(vm.ui.value.lastError.orEmpty().isNotBlank())
    }

    @Test
    fun joinTeam_locked_setsFriendlyError() = runTest {
        val repository = FakeTeamRepository().apply {
            joinTeamResult = TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.LOCKED, "locked")
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-3") }

        vm.joinTeam("123456")
        advanceUntilIdle()

        assertTrue(vm.ui.value.lastError.orEmpty().isNotBlank())
    }

    @Test
    fun joinTeam_notFound_setsFriendlyErrorAndDoesNotSetTeam() = runTest {
        val repository = FakeTeamRepository().apply {
            joinTeamResult = TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.NOT_FOUND, "team not found")
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-not-found") }

        vm.joinTeam("123456")
        advanceUntilIdle()

        assertEquals(1, repository.joinCalls.size)
        assertEquals(null, vm.ui.value.teamCode)
        assertTrue(vm.ui.value.lastError.orEmpty().isNotBlank())
    }

    @Test
    fun joinTeam_shortCode_isRejectedBeforeRepositoryCall() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-short") }

        vm.joinTeam("123")
        advanceUntilIdle()

        assertTrue(repository.joinCalls.isEmpty())
        assertTrue(vm.ui.value.lastError.orEmpty().contains("6"))
    }

    @Test
    fun joinTeam_nonDigitCode_isRejectedBeforeRepositoryCall() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-nondigit") }

        vm.joinTeam("12A456")
        advanceUntilIdle()

        assertTrue(repository.joinCalls.isEmpty())
        assertTrue(vm.ui.value.lastError.orEmpty().contains("6"))
    }

    @Test
    fun joinTeam_rateLimited_blocks_excessive_attempts() = runTest {
        val repository = FakeTeamRepository().apply {
            joinTeamResult = TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.NETWORK, "network down")
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-rate-limit") }

        repeat(6) {
            vm.joinTeam("123456")
            advanceUntilIdle()
        }

        assertEquals(5, repository.joinCalls.size)
        assertTrue(vm.ui.value.lastError.orEmpty().contains("Слишком много попыток"))
    }

    @Test
    fun addPointAt_callsRepository() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-4", teamCode = "654321") }

        vm.addPointAt(10.0, 20.0, "P1", "flag", forTeam = true)
        advanceUntilIdle()

        assertEquals(1, repository.addPointCalls.size)
        val call = repository.addPointCalls.single()
        assertEquals("654321", call.teamCode)
        assertEquals("uid-4", call.uid)
        assertEquals("P1", call.payload.label)
        assertTrue(call.forTeam)
    }

    @Test
    fun addPointAt_invalidCoordinates_skipsRepository_and_sets_error() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-invalid-point", teamCode = "654321") }

        vm.addPointAt(120.0, 20.0, "P1", "flag", forTeam = true)
        advanceUntilIdle()

        assertTrue(repository.addPointCalls.isEmpty())
        assertEquals("Некорректные данные", vm.ui.value.lastError)
    }

    @Test
    fun updatePoint_invalidCoordinates_skipsRepository_and_sets_error() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-invalid-update", teamCode = "654321") }

        vm.updatePoint(
            id = "point-1",
            lat = 55.0,
            lon = 200.0,
            label = "Updated",
            icon = "flag",
            isTeam = false,
        )
        advanceUntilIdle()

        assertTrue(repository.updatePointCalls.isEmpty())
        assertEquals("Некорректные данные", vm.ui.value.lastError)
    }

    @Test
    fun addEnemyPing_invalidCoordinates_skipsRepository_and_sets_error() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-invalid-enemy", teamCode = "654321") }

        vm.addEnemyPing(lat = -95.0, lon = 37.0, type = QuickCommandType.ENEMY)
        advanceUntilIdle()

        assertTrue(repository.addEnemyPingCalls.isEmpty())
        assertEquals("Некорректные данные", vm.ui.value.lastError)
    }

    @Test
    fun sendQuickCommand_enables_enemy_mark_mode_with_selected_type() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-5", teamCode = "654321") }

        vm.sendQuickCommand(QuickCommandType.ATTACK)
        advanceUntilIdle()

        assertTrue(vm.ui.value.enemyMarkEnabled)
        assertEquals(QuickCommandType.ATTACK, vm.ui.value.enemyMarkType)
        assertEquals(1, repository.setCommandCalls.size)
        val call = repository.setCommandCalls.single()
        assertEquals("654321", call.teamCode)
        assertEquals("uid-5", call.uid)
        assertEquals("ATTACK", call.type)
    }

    @Test
    fun sendQuickCommand_backendFailure_preserves_enemy_mark_mode_and_emits_error() = runTest {
        val repository = FakeTeamRepository().apply {
            setCommandResult = TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.NETWORK, "network down")
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest { it.withLegacy(uid = "uid-quick-fail", teamCode = "654321") }

        vm.sendQuickCommand(QuickCommandType.DEFENSE)
        advanceUntilIdle()

        assertTrue(vm.ui.value.enemyMarkEnabled)
        assertEquals(QuickCommandType.DEFENSE, vm.ui.value.enemyMarkType)
        assertEquals(1, repository.setCommandCalls.size)
        assertTrue(vm.ui.value.lastError.orEmpty().isNotBlank())
    }

    @Test
    fun observeTeam_maps_activeCommand_from_snapshot_to_ui_state() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-active-command",
                callsign = "ActiveUser",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        repository.snapshots.value = TeamSnapshot(
            activeCommand = TeamActiveCommand(
                id = "cmd-1",
                type = "ATTACK",
                createdAtMs = 1234L,
                createdBy = "uid-lead",
            ),
        )
        advanceUntilIdle()

        val command = vm.ui.value.activeCommand
        assertTrue(command != null)
        assertEquals("cmd-1", command?.id)
        assertEquals(QuickCommandType.ATTACK, command?.type)
        assertEquals(1234L, command?.createdAtMs)
        assertEquals("uid-lead", command?.createdBy)
    }

    @Test
    fun setPlayerMode_updatesTrackingStatus() = runTest {
        val controller = FakeTrackingController()
        val vm = createVm(FakeTeamRepository(), controller)
        vm.setUiForTest { it.withLegacy(mySosUntilMs = 1000L) }

        vm.setPlayerMode(PlayerMode.GAME)
        advanceUntilIdle()

        assertEquals(PlayerMode.GAME, vm.ui.value.playerMode)
        assertEquals(1, controller.statusUpdates.size)
        val status = controller.statusUpdates.single()
        assertEquals(PlayerMode.GAME, status.playerMode)
        assertEquals(1000L, status.sosUntilMs)
        assertTrue(status.forceSend)
    }

    @Test
    fun startTracking_startsControllerWhenContextReady() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val locationManager = app.getSystemService(LocationManager::class.java)
        Shadows.shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val controller = FakeTrackingController()
        val vm = createVm(FakeTeamRepository(), controller)
        vm.setUiForTest {
            it.withLegacy(
                uid = "uid-7",
                teamCode = "111222",
                hasLocationPermission = true,
                isLocationServiceEnabled = true,
                callsign = "Echo",
                playerMode = PlayerMode.GAME,
            )
        }

        vm.startTracking(TrackingMode.GAME)
        advanceUntilIdle()

        assertEquals(1, controller.startConfigs.size)
        val config = controller.startConfigs.single()
        assertEquals("111222", config.teamCode)
        assertEquals("uid-7", config.uid)
        assertEquals("Echo", config.callsign)
    }

    @Test
    fun stopTracking_callsControllerStop() = runTest {
        val controller = FakeTrackingController()
        val vm = createVm(FakeTeamRepository(), controller)

        vm.stopTracking()
        advanceUntilIdle()

        assertEquals(1, controller.stopCalls)
    }

    @Test
    fun remoteMemberPrefs_areAppliedAfterJoin() = runTest {
        val repository = FakeTeamRepository().apply {
            memberPrefsFlow.value = TeamMemberPrefs(
                preset = "NEAR",
                nearRadiusM = 220,
                showDead = false,
                showStale = true,
                focusMode = true,
                updatedAtMs = 100L,
            )
        }
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-8",
                callsign = "FilterUser",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()

        assertEquals(TargetFilterPreset.NEAR, vm.ui.value.targetFilterState.preset)
        assertEquals(220, vm.ui.value.targetFilterState.nearRadiusM)
        assertTrue(vm.ui.value.targetFilterState.focusMode)
        assertTrue(!vm.ui.value.targetFilterState.showDead)
    }

    @Test
    fun filterChange_updatesDisplayTargets_andDebouncedPrefsSync() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        val now = System.currentTimeMillis()

        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-9",
                callsign = "Alpha",
                teamCode = "111222",
                me = LocationPoint(55.0, 37.0, 5.0, 0.0, 0.0, now),
            )
        }

        vm.joinTeam("111222", alsoCreateMember = false)
        repository.snapshots.value = TeamSnapshot(
            players = listOf(
                PlayerState(
                    uid = "sos",
                    nick = "SOS",
                    point = LocationPoint(55.0004, 37.0004, 5.0, 0.0, 0.0, now - 2_000L),
                    sosUntilMs = now + 60_000L,
                ),
                PlayerState(
                    uid = "normal",
                    nick = "Normal",
                    point = LocationPoint(55.0012, 37.0002, 5.0, 0.0, 0.0, now - 2_000L),
                ),
            )
        )
        advanceUntilIdle()

        vm.setTargetPreset(TargetFilterPreset.SOS)
        val display = vm.computeTargets(now)
        advanceTimeBy(600L)
        advanceUntilIdle()

        assertEquals(listOf("sos"), display.map { it.uid })
        assertTrue(repository.upsertPrefsCalls.isNotEmpty())
        assertEquals("SOS", repository.upsertPrefsCalls.last().preset)
    }

    @Test
    fun observeTeam_populates_unified_markers() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        val now = System.currentTimeMillis()

        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-10",
                callsign = "MarkerUser",
            )
        }
        vm.joinTeam("111222", alsoCreateMember = false)
        repository.snapshots.value = TeamSnapshot(
            teamPoints = listOf(
                TeamPoint(
                    id = "t1",
                    lat = 55.0,
                    lon = 37.0,
                    label = "Team",
                    icon = TacticalIconId.FLAG.raw,
                    createdAtMs = now,
                    createdBy = "uid-10",
                    isTeam = true,
                )
            ),
            privatePoints = listOf(
                TeamPoint(
                    id = "p1",
                    lat = 55.0001,
                    lon = 37.0001,
                    label = "Private",
                    icon = TacticalIconId.OBJECTIVE.raw,
                    createdAtMs = now,
                    createdBy = "uid-10",
                    isTeam = false,
                )
            ),
            enemyPings = listOf(
                TeamEnemyPing(
                    id = "e1",
                    lat = 55.0002,
                    lon = 37.0002,
                    createdAtMs = now,
                    createdBy = "uid-10",
                    expiresAtMs = now + 120_000L,
                    type = "DANGER",
                )
            ),
        )
        advanceUntilIdle()

        val markers = vm.ui.value.markers
        assertEquals(3, markers.size)
        assertTrue(markers.any { it.kind == MarkerKind.POINT && it.scope == MarkerScope.TEAM })
        assertTrue(markers.any { it.kind == MarkerKind.POINT && it.scope == MarkerScope.PRIVATE })
        assertTrue(markers.any { it.kind == MarkerKind.ENEMY_PING && it.scope == MarkerScope.TEAM_EVENT })
    }

    @Test
    fun backendHealth_unavailable_then_recovered_updates_telemetry_and_error_state() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-health",
                callsign = "HealthUser",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()
        assertTrue(vm.ui.value.telemetry.backendAvailable)

        repository.backendHealthFlow.value = false
        advanceUntilIdle()
        // Fresh snapshot keeps backend "available" until it becomes stale.
        assertTrue(vm.ui.value.telemetry.backendAvailable)
        assertEquals(null, vm.ui.value.lastError)

        val staleAt = System.currentTimeMillis() - TeamCompassViewModel.STALE_WARNING_MS - 1L
        vm.setUiForTest { state ->
            state.copy(
                tracking = state.tracking.copy(
                    telemetry = state.telemetry.copy(
                        lastSnapshotAtMs = staleAt,
                    )
                )
            )
        }
        vm.refreshBackendStaleFlagForTest(nowMs = System.currentTimeMillis())
        assertTrue(!vm.ui.value.telemetry.backendAvailable)
        assertTrue(vm.ui.value.lastError.orEmpty().contains("Backend"))

        repository.backendHealthFlow.value = true
        advanceUntilIdle()
        assertTrue(vm.ui.value.telemetry.backendAvailable)
        assertEquals(null, vm.ui.value.lastError)
    }

    @Test
    fun backendHealth_signal_does_not_claim_read_only_without_write_gates() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-write",
                callsign = "Writer",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()
        repository.backendHealthFlow.value = false
        advanceUntilIdle()

        vm.addPointAt(
            lat = 55.0,
            lon = 37.0,
            label = "Point while backend down",
            icon = TacticalIconId.FLAG.raw,
            forTeam = true,
        )
        advanceUntilIdle()

        // Writes stay enabled regardless of health signal semantics.
        assertEquals(1, repository.addPointCalls.size)
    }

    @Test
    fun backend_unavailable_sets_stale_after_30s() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-stale",
                callsign = "StaleUser",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()
        val nowMs = System.currentTimeMillis()
        vm.setUiForTest { state ->
            state.copy(
                tracking = state.tracking.copy(
                    telemetry = state.telemetry.copy(
                        lastSnapshotAtMs = nowMs,
                        isBackendStale = false,
                    )
                )
            )
        }

        repository.backendHealthFlow.value = false
        runCurrent()
        assertTrue(vm.ui.value.telemetry.backendAvailable)
        assertFalse(vm.ui.value.telemetry.isBackendStale)

        vm.refreshBackendStaleFlagForTest(
            nowMs = nowMs + TeamCompassViewModel.STALE_WARNING_MS + 1L
        )

        assertTrue(vm.ui.value.telemetry.backendUnavailableSinceMs > 0L)
        assertFalse(vm.ui.value.telemetry.backendAvailable)
        assertTrue(vm.ui.value.telemetry.isBackendStale)
    }

    @Test
    fun initialize_failure_emits_error_and_trace() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ensureFirebaseApp(app)
        val logger = RecordingStructuredLogger()
        val vm = TeamCompassViewModel(
            app = app,
            teamRepository = FakeTeamRepository(),
            trackingController = FakeTrackingController(),
            prefs = UserPrefs(app),
            autoStart = true,
            actionTraceIdProvider = FixedTraceIdProvider("trace-init"),
            structuredLogger = logger,
            initializeAutoStartOverride = {
                error("init boom")
            },
        )

        advanceUntilIdle()

        assertTrue(vm.ui.value.lastError.orEmpty().contains("Initialization failed"))
        assertTrue(
            logger.entries.any {
                it.action == "initializeAutoStart" &&
                    it.phase == "failure" &&
                    it.traceId == "trace-init"
            }
        )
    }

    @Test
    fun initialize_cancellation_does_not_emit_error_or_failure_trace() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ensureFirebaseApp(app)
        val logger = RecordingStructuredLogger()
        val vm = TeamCompassViewModel(
            app = app,
            teamRepository = FakeTeamRepository(),
            trackingController = FakeTrackingController(),
            prefs = UserPrefs(app),
            autoStart = true,
            actionTraceIdProvider = FixedTraceIdProvider("trace-init-cancel"),
            structuredLogger = logger,
            initializeAutoStartOverride = {
                throw CancellationException("cancel init")
            },
        )

        advanceUntilIdle()

        assertTrue(vm.ui.value.lastError.isNullOrEmpty())
        assertFalse(
            logger.entries.any {
                it.action == "initializeAutoStart" &&
                    it.phase == "failure" &&
                    it.traceId == "trace-init-cancel"
            }
        )
    }

    @Test
    fun trace_id_present_for_join_flow() = runTest {
        val repository = FakeTeamRepository()
        val logger = RecordingStructuredLogger()
        val vm = createVm(
            repository = repository,
            controller = FakeTrackingController(),
            actionTraceIdProvider = FixedTraceIdProvider("trace-join"),
            structuredLogger = logger,
        )
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-join-trace",
                callsign = "JoinTrace",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()

        assertTrue(
            logger.entries.any {
                it.action == "joinTeam" &&
                    it.phase == "start" &&
                    it.traceId == "trace-join"
            }
        )
        assertTrue(
            logger.entries.any {
                it.action == "joinTeam" &&
                    it.phase == "success" &&
                    it.traceId == "trace-join"
            }
        )
    }

    @Test
    fun joinTeam_success_evaluates_identity_linking_eligibility() = runTest {
        val repository = FakeTeamRepository()
        val identityLinkingService = RecordingIdentityLinkingService(
            eligibility = IdentityLinkingEligibility(
                shouldPrompt = true,
                reason = "anonymous_user",
            )
        )
        val vm = createVm(
            repository = repository,
            controller = FakeTrackingController(),
            identityLinkingService = identityLinkingService,
        )
        vm.setUiForTest {
            it.withLegacy(
                isAuthReady = true,
                uid = "uid-linking",
                callsign = "LinkUser",
            )
        }

        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()
        vm.joinTeam("123456", alsoCreateMember = false)
        advanceUntilIdle()

        assertEquals(1, identityLinkingService.evaluateCalls)
    }

    @Test
    fun updatePoint_team_marker_not_author_sets_error() = runTest {
        val repository = FakeTeamRepository()
        val vm = createVm(repository, FakeTrackingController())
        val now = System.currentTimeMillis()
        vm.setUiForTest {
            it.withLegacy(
                uid = "uid-other",
                teamCode = "111222",
                teamPoints = listOf(
                    MapPoint(
                        id = "team-1",
                        lat = 55.0,
                        lon = 37.0,
                        label = "Team marker",
                        icon = TacticalIconId.FLAG.raw,
                        createdAtMs = now,
                        createdBy = "uid-author",
                        isTeam = true,
                    )
                )
            )
        }

        vm.updatePoint(
            id = "team-1",
            lat = 55.1,
            lon = 37.1,
            label = "New",
            icon = TacticalIconId.FLAG.raw,
            isTeam = true,
        )
        advanceUntilIdle()

        assertTrue(vm.ui.value.lastError.orEmpty().contains("Только автор"))
    }

    private fun createVm(
        repository: FakeTeamRepository,
        controller: FakeTrackingController,
        autoStart: Boolean = false,
        actionTraceIdProvider: ActionTraceIdProvider = FixedTraceIdProvider("trace-default"),
        structuredLogger: StructuredLogger = RecordingStructuredLogger(),
        identityLinkingService: IdentityLinkingService = NoOpIdentityLinkingService,
        initializeAutoStartOverride: (suspend TeamCompassViewModel.() -> Unit)? = null,
        savedStateHandle: SavedStateHandle? = null,
    ): TeamCompassViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ensureFirebaseApp(app)
        val prefs = UserPrefs(app)
        return TeamCompassViewModel(
            app = app,
            teamRepository = repository,
            trackingController = controller,
            prefs = prefs,
            autoStart = autoStart,
            actionTraceIdProvider = actionTraceIdProvider,
            structuredLogger = structuredLogger,
            identityLinkingService = identityLinkingService,
            initializeAutoStartOverride = initializeAutoStartOverride,
            savedStateHandle = savedStateHandle,
        )
    }

    private fun ensureFirebaseApp(app: Application) {
        if (FirebaseApp.getApps(app).isNotEmpty()) return
        val options = FirebaseOptions.Builder()
            .setProjectId("demo-teamcompass")
            .setApplicationId("1:1234567890:android:test")
            .setApiKey("test-api-key")
            .build()
        FirebaseApp.initializeApp(app, options)
    }
}

private fun UiState.withLegacy(
    isAuthReady: Boolean = auth.isReady,
    uid: String? = auth.uid,
    hasLocationPermission: Boolean = tracking.hasLocationPermission,
    isLocationServiceEnabled: Boolean = tracking.isLocationServiceEnabled,
    callsign: String = team.callsign,
    teamCode: String? = team.teamCode,
    playerMode: PlayerMode = team.playerMode,
    mySosUntilMs: Long = team.mySosUntilMs,
    me: LocationPoint? = tracking.me,
    teamPoints: List<MapPoint> = map.teamPoints,
): UiState = copy(
    auth = auth.copy(
        isReady = isAuthReady,
        uid = uid,
    ),
    tracking = tracking.copy(
        hasLocationPermission = hasLocationPermission,
        isLocationServiceEnabled = isLocationServiceEnabled,
        me = me,
    ),
    team = team.copy(
        callsign = callsign,
        teamCode = teamCode,
        playerMode = playerMode,
        mySosUntilMs = mySosUntilMs,
    ),
    map = map.copy(
        teamPoints = teamPoints,
    ),
)

private class FakeTeamRepository : TeamRepository {
    data class AddPointCall(
        val teamCode: String,
        val uid: String,
        val payload: TeamPointPayload,
        val forTeam: Boolean,
    )

    data class SetCommandCall(
        val teamCode: String,
        val uid: String,
        val type: String,
    )

    data class UpdatePointCall(
        val teamCode: String,
        val uid: String,
        val pointId: String,
        val payload: TeamPointUpdatePayload,
        val isTeam: Boolean,
    )

    data class AddEnemyPingCall(
        val teamCode: String,
        val uid: String,
        val lat: Double,
        val lon: Double,
        val type: String,
        val ttlMs: Long,
    )

    data class JoinCall(
        val teamCode: String,
        val uid: String,
        val callsign: String,
    )

    var createTeamResult: TeamActionResult<String> = TeamActionResult.Success("000001")
    var joinTeamResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var addPointResult: TeamActionResult<String> = TeamActionResult.Success("point-1")
    var updatePointResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var deletePointResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var setCommandResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var addEnemyResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var stateResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var upsertPrefsResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    var lastCreateOwnerUid: String? = null
    var lastCreateOwnerCallsign: String? = null
    val addPointCalls = mutableListOf<AddPointCall>()
    val updatePointCalls = mutableListOf<UpdatePointCall>()
    val addEnemyPingCalls = mutableListOf<AddEnemyPingCall>()
    val setCommandCalls = mutableListOf<SetCommandCall>()
    val joinCalls = mutableListOf<JoinCall>()
    val snapshots = MutableStateFlow(TeamSnapshot())
    val memberPrefsFlow = MutableStateFlow<TeamMemberPrefs?>(null)
    val backendHealthFlow = MutableStateFlow(true)
    val upsertPrefsCalls = mutableListOf<TeamMemberPrefs>()

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> {
        lastCreateOwnerUid = ownerUid
        lastCreateOwnerCallsign = ownerCallsign
        return createTeamResult
    }

    override suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long,
    ): TeamActionResult<Unit> {
        joinCalls += JoinCall(teamCode, uid, callsign)
        return joinTeamResult
    }

    override fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode,
        selfPoint: LocationPoint?,
    ): Flow<TeamSnapshot> = snapshots

    override fun observeBackendHealth(): Flow<Boolean> = backendHealthFlow

    override suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit> {
        return stateResult
    }

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> {
        addPointCalls += AddPointCall(teamCode, uid, payload, forTeam)
        return addPointResult
    }

    override suspend fun updatePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        payload: TeamPointUpdatePayload,
        isTeam: Boolean,
    ): TeamActionResult<Unit> {
        updatePointCalls += UpdatePointCall(
            teamCode = teamCode,
            uid = uid,
            pointId = pointId,
            payload = payload,
            isTeam = isTeam,
        )
        return updatePointResult
    }

    override suspend fun deletePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = deletePointResult

    override suspend fun setActiveCommand(teamCode: String, uid: String, type: String): TeamActionResult<Unit> {
        setCommandCalls += SetCommandCall(teamCode, uid, type)
        return setCommandResult
    }

    override suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long,
    ): TeamActionResult<Unit> {
        addEnemyPingCalls += AddEnemyPingCall(
            teamCode = teamCode,
            uid = uid,
            lat = lat,
            lon = lon,
            type = type,
            ttlMs = ttlMs,
        )
        return addEnemyResult
    }

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> = memberPrefsFlow

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> {
        upsertPrefsCalls += prefs
        return upsertPrefsResult
    }
}

private class FakeTrackingController : TrackingController {
    data class StatusUpdate(
        val playerMode: PlayerMode,
        val sosUntilMs: Long,
        val forceSend: Boolean,
    )

    override val isTracking: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val location: MutableStateFlow<LocationPoint?> = MutableStateFlow(null)
    override val isAnchored: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val telemetry: MutableStateFlow<TrackingTelemetry> = MutableStateFlow(TrackingTelemetry())

    val startConfigs = mutableListOf<TrackingSessionConfig>()
    var stopCalls: Int = 0
    val headingUpdates = mutableListOf<Double?>()
    val statusUpdates = mutableListOf<StatusUpdate>()

    override fun start(config: TrackingSessionConfig) {
        startConfigs += config
        isTracking.value = true
    }

    override fun stop() {
        stopCalls += 1
        isTracking.value = false
    }

    override fun updateHeading(headingDeg: Double?) {
        headingUpdates += headingDeg
    }

    override fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean) {
        statusUpdates += StatusUpdate(playerMode, sosUntilMs, forceSend)
    }
}

private data class LogEntry(
    val action: String,
    val phase: String,
    val traceId: String,
)

private class RecordingStructuredLogger : StructuredLogger {
    val entries = mutableListOf<LogEntry>()

    override fun logStart(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    ) {
        entries += LogEntry(action = action, phase = "start", traceId = traceId)
    }

    override fun logSuccess(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    ) {
        entries += LogEntry(action = action, phase = "success", traceId = traceId)
    }

    override fun logFailure(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
        throwable: Throwable?,
        message: String?,
    ) {
        entries += LogEntry(action = action, phase = "failure", traceId = traceId)
    }
}

private class FixedTraceIdProvider(
    private val traceId: String,
) : ActionTraceIdProvider {
    override fun nextTraceId(action: String): String = traceId
}

private class RecordingIdentityLinkingService(
    private val eligibility: IdentityLinkingEligibility,
) : IdentityLinkingService {
    var evaluateCalls: Int = 0

    override fun evaluateEligibility(): IdentityLinkingEligibility {
        evaluateCalls += 1
        return eligibility
    }

    override suspend fun linkWithEmail(
        email: String,
        password: String,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}


