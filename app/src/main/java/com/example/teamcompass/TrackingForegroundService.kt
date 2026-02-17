package com.example.teamcompass

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class TrackingForegroundService : Service() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    private var uid: String? = null
    private var teamCode: String? = null
    private var callsign: String = ""

    private var lastSentLocation: Location? = null
    private var lastSentAtMs: Long = 0L

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            maybeSendState(location)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                uid = intent.getStringExtra(EXTRA_UID)
                teamCode = intent.getStringExtra(EXTRA_TEAM_CODE)
                callsign = intent.getStringExtra(EXTRA_CALLSIGN).orEmpty()

                if (uid.isNullOrBlank() || teamCode.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                startForeground(NOTIFICATION_ID, buildNotification())
                startLocationUpdatesIfPermitted()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    private fun startLocationUpdatesIfPermitted() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            stopSelf()
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setWaitForAccurateLocation(false)
            .build()

        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    private fun maybeSendState(loc: Location) {
        val userId = uid ?: return
        val code = teamCode ?: return

        val now = System.currentTimeMillis()
        val lastLoc = lastSentLocation

        val timeOk = (now - lastSentAtMs) >= SEND_EVERY_MS
        val distOk = lastLoc == null || loc.distanceTo(lastLoc) >= SEND_DISTANCE_M

        if (!timeOk && !distOk) return

        lastSentAtMs = now
        lastSentLocation = Location(loc)

        val heading = if (loc.hasSpeed() && loc.speed > 1.5f && loc.hasBearing()) {
            normalize360(loc.bearing)
        } else {
            0f
        }

        val payload = hashMapOf<String, Any>(
            "lat" to loc.latitude,
            "lon" to loc.longitude,
            "acc" to loc.accuracy.toDouble(),
            "speed" to (if (loc.hasSpeed()) loc.speed.toDouble() else 0.0),
            "heading" to heading.toDouble(),
            "callsign" to callsign,
            "ts" to ServerValue.TIMESTAMP
        )

        db.child("teams").child(code).child("state").child(userId).updateChildren(payload)
    }

    private fun buildNotification(): Notification {
        ensureChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("TeamCompass: трекинг активен")
            .setContentText("Координаты отправляются даже с погашенным экраном")
            .setOngoing(true)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "TeamCompass Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    private fun normalize360(deg: Float): Float {
        var d = deg % 360f
        if (d < 0f) d += 360f
        return d
    }

    companion object {
        private const val CHANNEL_ID = "tracking"
        private const val NOTIFICATION_ID = 3011

        private const val SEND_EVERY_MS = 3000L
        private const val SEND_DISTANCE_M = 10f

        private const val EXTRA_UID = "uid"
        private const val EXTRA_TEAM_CODE = "team_code"
        private const val EXTRA_CALLSIGN = "callsign"

        private const val ACTION_START = "tracking_start"
        private const val ACTION_STOP = "tracking_stop"

        fun start(context: Context, uid: String, teamCode: String, callsign: String) {
            val intent = Intent(context, TrackingForegroundService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_UID, uid)
                .putExtra(EXTRA_TEAM_CODE, teamCode)
                .putExtra(EXTRA_CALLSIGN, callsign)

            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java)
                .setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
