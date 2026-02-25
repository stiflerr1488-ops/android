package com.example.teamcompass.tracking

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.teamcompass.TrackingService
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import com.example.teamcompass.domain.TrackingSessionConfig

object TrackingCommands {
    const val ACTION_START = "com.example.teamcompass.action.START_TRACKING"
    const val ACTION_STOP = "com.example.teamcompass.action.STOP_TRACKING"

    private const val EXTRA_TEAM_CODE = "extra_team_code"
    private const val EXTRA_UID = "extra_uid"
    private const val EXTRA_CALLSIGN = "extra_callsign"
    private const val EXTRA_MODE = "extra_mode"
    private const val EXTRA_GAME_INTERVAL_MS = "extra_game_interval_ms"
    private const val EXTRA_GAME_DISTANCE_M = "extra_game_distance_m"
    private const val EXTRA_SILENT_INTERVAL_MS = "extra_silent_interval_ms"
    private const val EXTRA_SILENT_DISTANCE_M = "extra_silent_distance_m"
    private const val EXTRA_PLAYER_MODE = "extra_player_mode"
    private const val EXTRA_SOS_UNTIL_MS = "extra_sos_until_ms"
    private const val TAG = "TrackingCommands"

    fun startIntent(context: Context, config: TrackingSessionConfig): Intent {
        return Intent(context, TrackingService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_TEAM_CODE, config.teamCode)
            putExtra(EXTRA_UID, config.uid)
            putExtra(EXTRA_CALLSIGN, config.callsign)
            putExtra(EXTRA_MODE, config.mode.name)
            putExtra(EXTRA_GAME_INTERVAL_MS, config.gamePolicy.minIntervalMs)
            putExtra(EXTRA_GAME_DISTANCE_M, config.gamePolicy.minDistanceMeters)
            putExtra(EXTRA_SILENT_INTERVAL_MS, config.silentPolicy.minIntervalMs)
            putExtra(EXTRA_SILENT_DISTANCE_M, config.silentPolicy.minDistanceMeters)
            putExtra(EXTRA_PLAYER_MODE, config.playerMode.name)
            putExtra(EXTRA_SOS_UNTIL_MS, config.sosUntilMs)
        }
    }

    fun stopIntent(context: Context): Intent {
        return Intent(context, TrackingService::class.java).apply {
            action = ACTION_STOP
        }
    }

    fun parseStartConfig(intent: Intent?): TrackingSessionConfig? {
        if (intent?.action != ACTION_START) return null
        val teamCode = intent.getStringExtra(EXTRA_TEAM_CODE).orEmpty()
        val uid = intent.getStringExtra(EXTRA_UID).orEmpty()
        val callsign = intent.getStringExtra(EXTRA_CALLSIGN).orEmpty()
        val mode = intent.getStringExtra(EXTRA_MODE)?.let { raw ->
            runCatching { TrackingMode.valueOf(raw) }
                .onFailure { err -> Log.w(TAG, "Invalid tracking mode '$raw', fallback to GAME", err) }
                .getOrDefault(TrackingMode.GAME)
        } ?: TrackingMode.GAME
        val playerMode = intent.getStringExtra(EXTRA_PLAYER_MODE)?.let { raw ->
            runCatching { PlayerMode.valueOf(raw) }
                .onFailure { err -> Log.w(TAG, "Invalid player mode '$raw', fallback to GAME", err) }
                .getOrDefault(PlayerMode.GAME)
        } ?: PlayerMode.GAME
        if (teamCode.isBlank() || uid.isBlank()) return null
        val gamePolicy = TrackingPolicy(
            minIntervalMs = intent.getLongExtra(EXTRA_GAME_INTERVAL_MS, 3_000L).coerceAtLeast(1_000L),
            minDistanceMeters = intent.getDoubleExtra(EXTRA_GAME_DISTANCE_M, 10.0).coerceAtLeast(1.0),
        )
        val silentPolicy = TrackingPolicy(
            minIntervalMs = intent.getLongExtra(EXTRA_SILENT_INTERVAL_MS, 10_000L).coerceAtLeast(1_000L),
            minDistanceMeters = intent.getDoubleExtra(EXTRA_SILENT_DISTANCE_M, 30.0).coerceAtLeast(1.0),
        )
        val sosUntilMs = intent.getLongExtra(EXTRA_SOS_UNTIL_MS, 0L)
        return TrackingSessionConfig(
            teamCode = teamCode,
            uid = uid,
            callsign = callsign,
            mode = mode,
            gamePolicy = gamePolicy,
            silentPolicy = silentPolicy,
            playerMode = playerMode,
            sosUntilMs = sosUntilMs,
        )
    }
}
