package com.example.teamcompass

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.domain.TeamActionResult
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendHealthBannerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun banner_visibility_transitions_for_backend_down_and_stale_states() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ensureFirebaseApp(app)
        val vm = TeamCompassViewModel(
            app = app,
            teamRepository = BannerFakeRepository(),
            trackingController = BannerFakeTrackingController(),
            prefs = UserPrefs(app),
            autoStart = false,
        )
        vm.setUiForTest {
            it.copy(
                auth = it.auth.copy(isReady = true, uid = "uid-banner"),
                team = it.team.copy(callsign = "BannerUser"),
            )
        }

        composeRule.setContent {
            TeamCompassTheme {
                TeamCompassApp(vm = vm)
            }
        }

        assertTagMissing("backend_banner_down")
        assertTagMissing("backend_banner_stale")

        composeRule.runOnIdle {
            vm.setUiForTest { state ->
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = state.telemetry.copy(
                            backendAvailable = false,
                            isBackendStale = false,
                        )
                    )
                )
            }
        }
        assertTagExists("backend_banner_down")
        assertTagMissing("backend_banner_stale")

        composeRule.runOnIdle {
            vm.setUiForTest { state ->
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = state.telemetry.copy(
                            backendAvailable = true,
                            isBackendStale = true,
                        )
                    )
                )
            }
        }
        assertTagMissing("backend_banner_down")
        assertTagExists("backend_banner_stale")

        composeRule.runOnIdle {
            vm.setUiForTest { state ->
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = state.telemetry.copy(
                            backendAvailable = true,
                            isBackendStale = false,
                        )
                    )
                )
            }
        }
        assertTagMissing("backend_banner_down")
        assertTagMissing("backend_banner_stale")
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

    private fun assertTagExists(tag: String) {
        val exists = composeRule
            .onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
        assertTrue("Expected tag to exist: $tag", exists)
    }

    private fun assertTagMissing(tag: String) {
        val exists = composeRule
            .onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
        assertFalse("Expected tag to be absent: $tag", exists)
    }
}

private class BannerFakeRepository : TeamRepository {
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
    ): Flow<TeamSnapshot> = flowOf(TeamSnapshot())

    override suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit> {
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> = TeamActionResult.Success("point-1")

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

private class BannerFakeTrackingController : TrackingController {
    override val isTracking: StateFlow<Boolean> = MutableStateFlow(false)
    override val location: StateFlow<LocationPoint?> = MutableStateFlow(null)
    override val isAnchored: StateFlow<Boolean> = MutableStateFlow(false)
    override val telemetry: StateFlow<TrackingTelemetry> = MutableStateFlow(TrackingTelemetry())

    override fun start(config: TrackingSessionConfig) = Unit

    override fun stop() = Unit

    override fun updateHeading(headingDeg: Double?) = Unit

    override fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean) = Unit
}
