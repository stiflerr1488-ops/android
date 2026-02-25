package com.example.teamcompass.ui

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.teamcompass.MainActivity
import com.example.teamcompass.R
import com.example.teamcompass.core.PlayerState

/**
 * Notification manager for critical team events:
 * - SOS from teammates
 * - enemy pings
 * - quick commands
 * - connection lost alerts
 */
class EventNotificationManager(private val app: Application) {

    private val notificationManager = NotificationManagerCompat.from(app)
    private val lastSosNotificationByKey = LinkedHashMap<String, Long>()

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val systemNotificationManager =
            app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val criticalChannel = NotificationChannel(
            CHANNEL_CRITICAL,
            string(R.string.notification_channel_critical_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = string(R.string.notification_channel_critical_description)
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        }

        val importantChannel = NotificationChannel(
            CHANNEL_IMPORTANT,
            string(R.string.notification_channel_important_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = string(R.string.notification_channel_important_description)
            enableVibration(true)
            enableLights(false)
        }

        val infoChannel = NotificationChannel(
            CHANNEL_INFO,
            string(R.string.notification_channel_info_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = string(R.string.notification_channel_info_description)
            enableVibration(false)
            enableLights(false)
        }

        systemNotificationManager.createNotificationChannels(
            listOf(criticalChannel, importantChannel, infoChannel),
        )
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showSosAlert(playerState: PlayerState) {
        if (!shouldSendSosNotification(playerState, System.currentTimeMillis())) {
            return
        }
        if (!hasNotificationPermission()) {
            Log.w(TAG, "No notification permission, skipping SOS alert")
            return
        }
        val notificationKey = sosNotificationKeyFor(playerState)
        val notificationId = sosNotificationIdForKey(notificationKey)
        val pendingIntent = createPendingIntentOrNull() ?: return

        val notification = NotificationCompat.Builder(app, CHANNEL_CRITICAL)
            .setSmallIcon(R.drawable.ic_compass)
            .setContentTitle(string(R.string.notification_sos_title_format, playerState.nick))
            .setContentText(string(R.string.notification_sos_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show SOS notification", e)
        }
    }

    fun showEnemyPingAlert(lat: Double, lon: Double, distanceMeters: Double) {
        if (!hasNotificationPermission()) return
        val pendingIntent = createPendingIntentOrNull() ?: return

        val notification = NotificationCompat.Builder(app, CHANNEL_CRITICAL)
            .setSmallIcon(R.drawable.ic_compass)
            .setContentTitle(string(R.string.notification_enemy_title))
            .setContentText(string(R.string.notification_enemy_distance_format, distanceMeters.toInt()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ENEMY_PING, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show enemy ping notification", e)
        }
    }

    fun showQuickCommandAlert(type: QuickCommandType, createdBy: String) {
        if (!hasNotificationPermission()) return
        val pendingIntent = createPendingIntentOrNull() ?: return

        val (titleResId, messageResId) = when (type) {
            QuickCommandType.ENEMY -> {
                R.string.notification_quick_enemy_title to R.string.notification_quick_enemy_message_format
            }

            QuickCommandType.ATTACK -> {
                R.string.notification_quick_attack_title to R.string.notification_quick_attack_message_format
            }

            QuickCommandType.DEFENSE -> {
                R.string.notification_quick_defense_title to R.string.notification_quick_defense_message_format
            }

            QuickCommandType.DANGER -> {
                R.string.notification_quick_danger_title to R.string.notification_quick_danger_message_format
            }
        }

        val notification = NotificationCompat.Builder(app, CHANNEL_IMPORTANT)
            .setSmallIcon(R.drawable.ic_compass)
            .setContentTitle(string(titleResId))
            .setContentText(string(messageResId, createdBy))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_QUICK_COMMAND, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show quick command notification", e)
        }
    }

    fun showConnectionLostAlert(playerNick: String, lastSeenSeconds: Long) {
        if (!hasNotificationPermission()) return
        val pendingIntent = createPendingIntentOrNull() ?: return

        val notification = NotificationCompat.Builder(app, CHANNEL_INFO)
            .setSmallIcon(R.drawable.ic_compass)
            .setContentTitle(string(R.string.notification_connection_lost_title))
            .setContentText(
                string(
                    R.string.notification_connection_lost_message_format,
                    playerNick,
                    lastSeenSeconds,
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_CONNECTION_LOST, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show connection lost notification", e)
        }
    }

    fun cancelSosAlert() {
        val ids = lastSosNotificationByKey.keys
            .map(::sosNotificationIdForKey)
            .distinct()
        if (ids.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_SOS_LEGACY)
            return
        }
        ids.forEach { id -> notificationManager.cancel(id) }
        notificationManager.cancel(NOTIFICATION_SOS_LEGACY)
    }

    internal fun shouldSendSosNotification(playerState: PlayerState, nowMs: Long): Boolean {
        if (playerState.sosUntilMs <= nowMs) return false

        val key = sosNotificationKeyFor(playerState)
        val lastAt = lastSosNotificationByKey[key]
        if (lastAt != null && (nowMs - lastAt) < SOS_NOTIFICATION_SUPPRESSION_MS) {
            return false
        }

        pruneOldSosKeys(nowMs)
        lastSosNotificationByKey[key] = nowMs
        return true
    }

    private fun pruneOldSosKeys(nowMs: Long) {
        val threshold = nowMs - SOS_NOTIFICATION_SUPPRESSION_MS * 2
        val iterator = lastSosNotificationByKey.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value < threshold) {
                iterator.remove()
            }
        }
    }

    internal fun sosNotificationKeyFor(playerState: PlayerState): String {
        val uid = playerState.uid.trim()
        if (uid.isNotEmpty()) return uid
        val nick = playerState.nick.trim()
        return if (nick.isNotEmpty()) nick else "unknown"
    }

    internal fun sosNotificationIdForKey(key: String): Int {
        return sosNotificationIdFromHash(key.hashCode())
    }

    @VisibleForTesting
    internal fun sosNotificationIdFromHash(hash: Int): Int {
        val normalized = hash.toLong() and 0x7fffffffL
        return NOTIFICATION_SOS_BASE + ((normalized % NOTIFICATION_SOS_ID_RANGE).toInt())
    }

    private fun createPendingIntentOrNull(): PendingIntent? {
        val intent = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            setPackage(app.packageName)
        }

        return try {
            PendingIntent.getActivity(
                app,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        } catch (err: SecurityException) {
            Log.e(TAG, "Failed to create PendingIntent due to security policy", err)
            null
        } catch (err: IllegalArgumentException) {
            Log.e(TAG, "Failed to create PendingIntent due to invalid arguments", err)
            null
        }
    }

    private fun string(resId: Int): String = app.getString(resId)

    private fun string(resId: Int, vararg args: Any): String = app.getString(resId, *args)

    companion object {
        private const val TAG = "EventNotificationMgr"

        private const val CHANNEL_CRITICAL = "critical_events"
        private const val CHANNEL_IMPORTANT = "important_commands"
        private const val CHANNEL_INFO = "info"

        private const val NOTIFICATION_SOS_LEGACY = 1001
        private const val NOTIFICATION_SOS_BASE = 11_000
        private const val NOTIFICATION_SOS_ID_RANGE = 8_000
        private const val NOTIFICATION_ENEMY_PING = 1002
        private const val NOTIFICATION_QUICK_COMMAND = 1003
        private const val NOTIFICATION_CONNECTION_LOST = 1004
        private const val SOS_NOTIFICATION_SUPPRESSION_MS = 15_000L
    }
}
