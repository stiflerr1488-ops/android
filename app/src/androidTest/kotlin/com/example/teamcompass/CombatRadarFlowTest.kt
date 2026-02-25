package com.example.teamcompass

import android.Manifest
import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.domain.TrackingTelemetry
import com.example.teamcompass.ui.QuickCommandType
import com.example.teamcompass.ui.TeamCompassApp
import com.example.teamcompass.ui.TeamCompassViewModel
import com.example.teamcompass.ui.UserPrefs
import com.example.teamcompass.ui.theme.TeamCompassTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CombatRadarFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val prefs = UserPrefs(app)

    @Before
    fun setUp() {
        runCatching {
            InstrumentationRegistry.getInstrumentation().uiAutomation.adoptShellPermissionIdentity(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        }
        ensureFirebaseApp(app)
    }

    @After
    fun tearDown() {
        runCatching {
            InstrumentationRegistry.getInstrumentation().uiAutomation.dropShellPermissionIdentity()
        }
    }

    @Test
    fun markFlow_selectType_andTapRadar_callsAddEnemyPing() {
        val repository = SharedCombatRepository()
        val vm = buildVm(
            uid = "fighter-1",
            callsign = "Alpha",
            teamCode = "123456",
            repository = repository,
        )

        composeRule.setContent {
            TeamCompassTheme {
                TeamCompassApp(vm = vm)
            }
        }

        waitForTag("radar_canvas", timeoutMs = 30_000L)
        composeRule.onNodeWithTag("mark_toggle_button", useUnmergedTree = true).performClick()
        waitForTag("mark_palette", timeoutMs = 10_000L)
        composeRule.onNodeWithTag("mark_type_enemy_button", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("radar_canvas", useUnmergedTree = true)
            .performTouchInput {
                down(center)
                up()
            }

        composeRule.waitUntil(10_000L) { repository.enemyCallsCount() >= 1 }
        assertEquals("ENEMY", repository.lastEnemyCallType())
        composeRule.waitUntil(10_000L) { vm.ui.value.enemyPings.isNotEmpty() }
        assertTrue(vm.ui.value.enemyPings.any { it.type == QuickCommandType.ENEMY })
    }

    @Test
    fun markBroadcast_betweenTwoClients_visibleForBothViewModels() {
        val repository = SharedCombatRepository()
        val vmA = buildVm(
            uid = "fighter-a",
            callsign = "Alpha",
            teamCode = "654321",
            repository = repository,
        )
        val vmB = buildVm(
            uid = "fighter-b",
            callsign = "Bravo",
            teamCode = "654321",
            repository = repository,
        )

        vmA.joinTeam("654321", alsoCreateMember = false)
        vmB.joinTeam("654321", alsoCreateMember = false)
        waitUntil(8_000L) { repository.joinCallsCount() >= 2 }

        vmA.addEnemyPing(59.81041, 30.36703, QuickCommandType.DANGER)
        waitUntil(10_000L) { vmA.ui.value.enemyPings.isNotEmpty() && vmB.ui.value.enemyPings.isNotEmpty() }

        assertTrue(vmA.ui.value.enemyPings.any { it.type == QuickCommandType.DANGER })
        assertTrue(vmB.ui.value.enemyPings.any { it.type == QuickCommandType.DANGER })
    }

    private fun buildVm(
        uid: String,
        callsign: String,
        teamCode: String,
        repository: SharedCombatRepository,
    ): TeamCompassViewModel {
        val vm = TeamCompassViewModel(
            app = app,
            teamRepository = repository,
            trackingController = FakeTrackingController(),
            prefs = prefs,
            autoStart = false,
        )
        vm.setUiForTest { current ->
            current.copy(
                auth = current.auth.copy(isReady = true, uid = uid),
                tracking = current.tracking.copy(
                    hasLocationPermission = true,
                    isLocationServiceEnabled = true,
                    me = LocationPoint(
                        lat = 59.81041,
                        lon = 30.36703,
                        accMeters = 5.0,
                        speedMps = 0.0,
                        headingDeg = 0.0,
                        timestampMs = System.currentTimeMillis(),
                    ),
                ),
                team = current.team.copy(
                    callsign = callsign,
                    teamCode = teamCode,
                    playerMode = PlayerMode.GAME,
                ),
                settings = current.settings.copy(
                    showCompassHelpOnce = false,
                    showOnboardingOnce = false,
                ),
            )
        }
        return vm
    }

    private fun waitForTag(tag: String, timeoutMs: Long) {
        composeRule.waitUntil(timeoutMs) {
            composeRule
                .onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(60L)
        }
        throw AssertionError("Condition not met in ${timeoutMs}ms")
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

private class SharedCombatRepository : TeamRepository {
    private val snapshots = MutableStateFlow(TeamSnapshot())
    private val enemyCalls = mutableListOf<EnemyCall>()
    private val joinCalls = mutableListOf<JoinCall>()

    data class EnemyCall(
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

    fun enemyCallsCount(): Int = synchronized(enemyCalls) { enemyCalls.size }

    fun lastEnemyCallType(): String = synchronized(enemyCalls) { enemyCalls.last().type }

    fun joinCallsCount(): Int = synchronized(joinCalls) { joinCalls.size }

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
    ): TeamActionResult<Unit> {
        synchronized(joinCalls) {
            joinCalls += JoinCall(teamCode, uid, callsign)
        }
        return TeamActionResult.Success(Unit)
    }

    override fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode,
        selfPoint: LocationPoint?,
    ): Flow<TeamSnapshot> = snapshots

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

    override suspend fun setActiveCommand(
        teamCode: String,
        uid: String,
        type: String,
    ): TeamActionResult<Unit> {
        val now = System.currentTimeMillis()
        snapshots.value = snapshots.value.copy(
            activeCommand = TeamActiveCommand(
                id = "cmd_$now",
                type = type,
                createdAtMs = now,
                createdBy = uid,
            )
        )
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long,
    ): TeamActionResult<Unit> {
        val now = System.currentTimeMillis()
        synchronized(enemyCalls) {
            enemyCalls += EnemyCall(teamCode, uid, lat, lon, type, ttlMs)
        }
        val ping = TeamEnemyPing(
            id = "enemy_$now",
            lat = lat,
            lon = lon,
            createdAtMs = now,
            createdBy = uid,
            expiresAtMs = now + ttlMs,
            type = type,
        )
        val nextEnemy = (snapshots.value.enemyPings + ping)
            .sortedByDescending { it.createdAtMs }
        snapshots.value = snapshots.value.copy(enemyPings = nextEnemy)
        return TeamActionResult.Success(Unit)
    }

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> = flowOf(null)

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}

private class FakeTrackingController : TrackingController {
    override val isTracking: StateFlow<Boolean> = MutableStateFlow(false)
    override val location: StateFlow<LocationPoint?> = MutableStateFlow(null)
    override val isAnchored: StateFlow<Boolean> = MutableStateFlow(false)
    override val telemetry: StateFlow<TrackingTelemetry> = MutableStateFlow(TrackingTelemetry())

    override fun start(config: TrackingSessionConfig) = Unit

    override fun stop() = Unit

    override fun updateHeading(headingDeg: Double?) = Unit

    override fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean) = Unit
}
