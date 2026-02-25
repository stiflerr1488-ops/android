package com.example.teamcompass.ui

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.domain.TrackingTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingCoordinatorTest {

    @Test
    fun start_buildsConfig_andUsesFallbackCallsign() {
        val controller = TrackingCoordinatorFakeController()
        val coordinator = TrackingCoordinator(controller)

        coordinator.start(
            request = TrackingCoordinator.StartRequest(
                teamCode = "123456",
                uid = "u-1",
                callsign = "",
                mode = TrackingMode.GAME,
                gameIntervalSec = 3,
                gameDistanceM = 10,
                silentIntervalSec = 12,
                silentDistanceM = 35,
                playerMode = PlayerMode.GAME,
                sosUntilMs = 100L,
            ),
            isCurrentlyTracking = false,
        )

        assertEquals(1, controller.startConfigs.size)
        val config = controller.startConfigs.single()
        assertEquals("123456", config.teamCode)
        assertEquals("u-1", config.uid)
        assertEquals("Player", config.callsign)
        assertEquals(3_000L, config.gamePolicy.minIntervalMs)
        assertEquals(10.0, config.gamePolicy.minDistanceMeters, 0.0)
        assertEquals(12_000L, config.silentPolicy.minIntervalMs)
        assertEquals(35.0, config.silentPolicy.minDistanceMeters, 0.0)
    }

    @Test
    fun start_restartsActiveTracking_whenFlagEnabled() {
        val controller = TrackingCoordinatorFakeController()
        val coordinator = TrackingCoordinator(controller)

        coordinator.start(
            request = defaultRequest(),
            isCurrentlyTracking = true,
            restartIfActive = true,
        )

        assertEquals(1, controller.stopCalls)
        assertEquals(1, controller.startConfigs.size)
    }

    @Test
    fun start_doesNotStop_whenRestartDisabled() {
        val controller = TrackingCoordinatorFakeController()
        val coordinator = TrackingCoordinator(controller)

        coordinator.start(
            request = defaultRequest(),
            isCurrentlyTracking = true,
            restartIfActive = false,
        )

        assertEquals(0, controller.stopCalls)
        assertEquals(1, controller.startConfigs.size)
    }

    @Test
    fun stop_delegatesToTrackingController() {
        val controller = TrackingCoordinatorFakeController()
        val coordinator = TrackingCoordinator(controller)

        coordinator.stop()

        assertEquals(1, controller.stopCalls)
    }

    private fun defaultRequest(): TrackingCoordinator.StartRequest {
        return TrackingCoordinator.StartRequest(
            teamCode = "123456",
            uid = "u-1",
            callsign = "Alpha",
            mode = TrackingMode.GAME,
            gameIntervalSec = 3,
            gameDistanceM = 10,
            silentIntervalSec = 12,
            silentDistanceM = 35,
            playerMode = PlayerMode.GAME,
            sosUntilMs = 0L,
        )
    }
}

private class TrackingCoordinatorFakeController : TrackingController {
    override val isTracking: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val location: MutableStateFlow<LocationPoint?> = MutableStateFlow(null)
    override val isAnchored: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val telemetry: MutableStateFlow<TrackingTelemetry> = MutableStateFlow(TrackingTelemetry())

    val startConfigs = mutableListOf<TrackingSessionConfig>()
    var stopCalls: Int = 0

    override fun start(config: TrackingSessionConfig) {
        startConfigs += config
        isTracking.value = true
    }

    override fun stop() {
        stopCalls += 1
        isTracking.value = false
    }

    override fun updateHeading(headingDeg: Double?) = Unit

    override fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean) = Unit
}
