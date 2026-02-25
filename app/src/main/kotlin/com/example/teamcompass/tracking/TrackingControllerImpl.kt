package com.example.teamcompass.tracking

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.core.PlayerMode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.StateFlow

class TrackingControllerImpl(
    private val app: Application,
    repository: TeamRepository,
    coroutineExceptionHandler: CoroutineExceptionHandler,
) : TrackingController {
    private val runtime = TrackingRuntime(
        app = app,
        repository = repository,
        coroutineExceptionHandler = coroutineExceptionHandler,
    )

    override val isTracking: StateFlow<Boolean> = runtime.isTracking
    override val location = runtime.location
    override val isAnchored = runtime.isAnchored
    override val telemetry = runtime.telemetry

    override fun start(config: TrackingSessionConfig) {
        ContextCompat.startForegroundService(app, TrackingCommands.startIntent(app, config))
    }

    override fun stop() {
        app.startService(TrackingCommands.stopIntent(app))
    }

    override fun updateHeading(headingDeg: Double?) {
        runtime.updateHeading(headingDeg)
    }

    override fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean) {
        runtime.updateStatus(playerMode, sosUntilMs, forceSend)
    }

    fun onServiceDestroyed() {
        runtime.close()
    }

    fun handleServiceIntent(intent: Intent?) {
        when (intent?.action) {
            TrackingCommands.ACTION_START -> {
                val cfg = TrackingCommands.parseStartConfig(intent) ?: return
                runtime.start(cfg)
            }

            TrackingCommands.ACTION_STOP -> {
                runtime.stop()
            }
        }
    }
}
