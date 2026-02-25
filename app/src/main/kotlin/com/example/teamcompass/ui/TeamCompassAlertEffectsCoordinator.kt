package com.example.teamcompass.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.teamcompass.core.PlayerMode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class TeamCompassAlertEffectsCoordinator(
    private val scope: CoroutineScope,
    private val coroutineExceptionHandler: CoroutineExceptionHandler,
    private val application: Application,
    private val alertsCoordinator: AlertsCoordinator,
    private val eventNotificationManager: EventNotificationManager,
    private val readState: () -> UiState,
    private val vibrator: Vibrator?,
    private val tone: ToneGenerator,
    private val logTag: String,
) {
    private var deadReminderJob: Job? = null

    fun processEnemyPingAlerts(enemyPings: List<EnemyPing>) {
        val tacticalEnemyPings = enemyPings.filterNot { it.isBluetooth }
        val now = System.currentTimeMillis()
        val closeAlerts = alertsCoordinator.consumeNewCloseEnemyPings(
            enemyPings = tacticalEnemyPings,
            me = readState().me,
            nowMs = now,
        )
        repeat(closeAlerts) {
            vibrateAndBeep(strong = true)
        }
    }

    fun processSosAlerts() {
        val now = System.currentTimeMillis()
        val players = readState().players
        players.forEach { player ->
            if (player.sosUntilMs > now) {
                eventNotificationManager.showSosAlert(player)
            }
        }
    }

    fun startDeadReminder() {
        if (deadReminderJob != null) return
        deadReminderJob = scope.launch(coroutineExceptionHandler) {
            while (true) {
                delay(10 * 60_000L)
                if (readState().playerMode != PlayerMode.DEAD) continue
                vibrateAndBeep(strong = true)
            }
        }
    }

    fun stopDeadReminder() {
        deadReminderJob?.cancel()
        deadReminderJob = null
    }

    fun shutdown() {
        stopDeadReminder()
    }

    private fun vibrateAndBeep(strong: Boolean) {
        val hasVibratePermission =
            ContextCompat.checkSelfPermission(application, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED
        if (hasVibratePermission) {
            try {
                if (strong) {
                    val timings = longArrayOf(0, 90, 60, 120, 60, 220)
                    vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
                } else {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (err: Throwable) {
                Log.w(logTag, "Failed to vibrate alert", err)
            }
        }
        try {
            val toneType = if (strong) ToneGenerator.TONE_SUP_ERROR else ToneGenerator.TONE_PROP_BEEP
            val durationMs = if (strong) 450 else 160
            tone.startTone(toneType, durationMs)
        } catch (err: Throwable) {
            Log.w(logTag, "Failed to play alert tone", err)
        }
    }
}
