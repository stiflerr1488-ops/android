package com.example.teamcompass.ui

import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.domain.TrackingSessionConfig

internal class TrackingCoordinator(
    private val trackingController: TrackingController,
    private val fallbackCallsign: String = DEFAULT_CALLSIGN,
) {
    data class StartRequest(
        val teamCode: String,
        val uid: String,
        val callsign: String,
        val mode: TrackingMode,
        val gameIntervalSec: Int,
        val gameDistanceM: Int,
        val silentIntervalSec: Int,
        val silentDistanceM: Int,
        val playerMode: PlayerMode,
        val sosUntilMs: Long,
    )

    fun start(
        request: StartRequest,
        isCurrentlyTracking: Boolean,
        restartIfActive: Boolean = true,
    ) {
        if (restartIfActive && isCurrentlyTracking) {
            trackingController.stop()
        }

        val config = TrackingSessionConfig(
            teamCode = request.teamCode,
            uid = request.uid,
            callsign = request.callsign.ifBlank { fallbackCallsign },
            mode = request.mode,
            gamePolicy = TrackingPolicy(
                minIntervalMs = request.gameIntervalSec * 1_000L,
                minDistanceMeters = request.gameDistanceM.toDouble(),
            ),
            silentPolicy = TrackingPolicy(
                minIntervalMs = request.silentIntervalSec * 1_000L,
                minDistanceMeters = request.silentDistanceM.toDouble(),
            ),
            playerMode = request.playerMode,
            sosUntilMs = request.sosUntilMs,
        )

        trackingController.start(config)
    }

    fun stop() {
        trackingController.stop()
    }

    private companion object {
        private const val DEFAULT_CALLSIGN = "Player"
    }
}
