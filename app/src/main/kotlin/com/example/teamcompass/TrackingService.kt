package com.example.teamcompass

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.teamcompass.tracking.TrackingCommands
import com.example.teamcompass.tracking.TrackingControllerImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {
    @Inject
    lateinit var trackingController: TrackingControllerImpl
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_tracking_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
        try {
            startForeground(NOTIFICATION_ID, buildNotification())
            foregroundStarted = true
        } catch (err: SecurityException) {
            Log.e(TAG, "Failed to start foreground service (permissions/state)", err)
            stopSelf()
        } catch (err: IllegalStateException) {
            Log.e(TAG, "Failed to start foreground service (illegal state)", err)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "TrackingService started, startId=$startId flags=$flags action=${intent?.action}")
        if (intent?.action == TrackingCommands.ACTION_STOP) {
            trackingController.handleServiceIntent(intent)
            if (foregroundStarted) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                foregroundStarted = false
            }
            stopSelf()
            return START_NOT_STICKY
        }
        trackingController.handleServiceIntent(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.w(TAG, "TrackingService destroyed")
        trackingController.onServiceDestroyed()
        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_compass)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_tracking_active))
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "TrackingService"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 101
    }
}
