package com.example.teamcompass.ui

import android.app.Application
import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamcompass.core.CompassCalculator
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicies
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class UiState(
    val isAuthReady: Boolean = false,
    val uid: String? = null,

    val callsign: String = "",
    val teamCode: String? = null,

    val isTracking: Boolean = false,
    val hasLocationPermission: Boolean = false,

    val me: LocationPoint? = null,
    val myHeadingDeg: Double? = null,

    val players: List<PlayerState> = emptyList(),
    val lastError: String? = null,
)

class TeamCompassViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    private val prefs = UserPrefs(app)

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var statesListener: ValueEventListener? = null
    private var statesRef: DatabaseReference? = null

    private val fused = LocationServices.getFusedLocationProviderClient(app)
    private var locationCallback: LocationCallback? = null

    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var sensorListener: SensorEventListener? = null

    private val calculator = CompassCalculator()

    init {
        viewModelScope.launch {
            // Load saved prefs
            val callsign = prefs.callsignFlow.first()
            val team = prefs.teamCodeFlow.first()
            _ui.update { it.copy(callsign = callsign, teamCode = team) }
        }
        ensureAuth()
    }

    fun ensureAuth() {
        val current = auth.currentUser
        if (current != null) {
            _ui.update { it.copy(isAuthReady = true, uid = current.uid) }
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { res ->
                _ui.update { it.copy(isAuthReady = true, uid = res.user?.uid) }
            }
            .addOnFailureListener { e ->
                _ui.update { it.copy(isAuthReady = false, lastError = "Auth error: ${e.message}") }
            }
    }

    fun setCallsign(value: String) {
        _ui.update { it.copy(callsign = value.take(24)) }
        viewModelScope.launch { prefs.setCallsign(value.take(24)) }
    }

    fun setLocationPermission(granted: Boolean) {
        _ui.update { it.copy(hasLocationPermission = granted) }
        if (!granted) stopTracking()
    }

    fun createTeam() {
        val uid = _ui.value.uid ?: return
        val nick = _ui.value.callsign.ifBlank { "Игрок" }
        viewModelScope.launch {
            val code = generateCode()
            val meta = mapOf(
                "createdAtMs" to System.currentTimeMillis(),
                "createdBy" to uid,
                "isLocked" to false,
            )
            val base = db.child("teams").child(code)
            base.child("meta").setValue(meta)
                .addOnSuccessListener {
                    joinTeam(code, alsoCreateMember = true)
                }
                .addOnFailureListener { e ->
                    _ui.update { it.copy(lastError = "Не удалось создать: ${e.message}") }
                }
        }
    }

    fun joinTeam(codeRaw: String, alsoCreateMember: Boolean = true) {
        val uid = _ui.value.uid ?: return
        val nick = _ui.value.callsign.ifBlank { "Игрок" }
        val code = codeRaw.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val base = db.child("teams").child(code)
        base.child("meta").get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    _ui.update { it.copy(lastError = "Код не найден") }
                    return@addOnSuccessListener
                }
                if (alsoCreateMember) {
                    val member = mapOf(
                        "callsign" to nick,
                        "joinedAtMs" to System.currentTimeMillis(),
                    )
                    base.child("members").child(uid).setValue(member)
                }
                _ui.update { it.copy(teamCode = code, lastError = null) }
                viewModelScope.launch { prefs.setTeamCode(code) }
                startListening(code)
            }
            .addOnFailureListener { e ->
                _ui.update { it.copy(lastError = "Ошибка сети: ${e.message}") }
            }
    }

    fun leaveTeam() {
        stopTracking()
        stopListening()
        _ui.update { it.copy(teamCode = null, players = emptyList()) }
        viewModelScope.launch { prefs.setTeamCode(null) }
    }

    private fun startListening(code: String) {
        stopListening()
        val uid = _ui.value.uid ?: return
        val ref = db.child("teams").child(code).child("state")
        statesRef = ref
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PlayerState>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    if (id == uid) continue
                    val callsign = child.child("callsign").getValue(String::class.java) ?: "?"
                    val lat = child.child("lat").getValue(Double::class.java) ?: continue
                    val lon = child.child("lon").getValue(Double::class.java) ?: continue
                    val acc = child.child("acc").getValue(Double::class.java) ?: 999.0
                    val speed = child.child("speed").getValue(Double::class.java) ?: 0.0
                    val heading = child.child("heading").getValue(Double::class.java)
                    val ts = child.child("ts").getValue(Long::class.java) ?: 0L
                    list.add(PlayerState(id, callsign, LocationPoint(lat, lon, acc, speed, heading, ts)))
                }
                _ui.update { it.copy(players = list, lastError = null) }
            }
            override fun onCancelled(error: DatabaseError) {
                _ui.update { it.copy(lastError = "DB: ${error.message}") }
            }
        }
        ref.addValueEventListener(listener)
        statesListener = listener

        // auto resume tracking if permission granted
        if (_ui.value.hasLocationPermission) startTracking(TrackingMode.GAME)
    }

    private fun stopListening() {
        statesListener?.let { l -> statesRef?.removeEventListener(l) }
        statesListener = null
        statesRef = null
    }

    fun startTracking(mode: TrackingMode) {
        if (!_ui.value.hasLocationPermission) {
            _ui.update { it.copy(lastError = "Нужен доступ к геолокации") }
            return
        }
        val code = _ui.value.teamCode ?: return
        val uid = _ui.value.uid ?: return
        val nick = _ui.value.callsign.ifBlank { "Игрок" }

        startHeading()

        val policy = TrackingPolicies.forMode(mode)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, policy.minIntervalMs)
            .setMinUpdateDistanceMeters(policy.minDistanceMeters.toFloat())
            .build()

        @SuppressLint("MissingPermission")
        fun doRequest() {
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    onLocation(loc, code, uid, nick)
                }
            }
            locationCallback = cb
            fused.requestLocationUpdates(request, cb, null)
            _ui.update { it.copy(isTracking = true) }
        }

        // single warm-up to get last known fast
        @SuppressLint("MissingPermission")
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) onLocation(loc, code, uid, nick)
            doRequest()
        }.addOnFailureListener {
            doRequest()
        }
    }

    private fun onLocation(loc: Location, code: String, uid: String, nick: String) {
        val now = System.currentTimeMillis()
        val heading = _ui.value.myHeadingDeg
        val point = LocationPoint(
            lat = loc.latitude,
            lon = loc.longitude,
            accMeters = loc.accuracy.toDouble(),
            speedMps = loc.speed.toDouble(),
            headingDeg = heading,
            timestampMs = now,
        )
        _ui.update { it.copy(me = point) }

        val payload = mapOf(
            "callsign" to nick,
            "lat" to point.lat,
            "lon" to point.lon,
            "acc" to point.accMeters,
            "speed" to point.speedMps,
            "heading" to (heading ?: point.headingDeg),
            "ts" to point.timestampMs,
        )
        db.child("teams").child(code).child("state").child(uid).setValue(payload)
    }

    fun stopTracking() {
        locationCallback?.let { fused.removeLocationUpdates(it) }
        locationCallback = null
        stopHeading()
        _ui.update { it.copy(isTracking = false) }
    }

    fun dismissError() {
        _ui.update { it.copy(lastError = null) }
    }

    fun computeTargets(nowMs: Long): List<com.example.teamcompass.core.CompassTarget> {
        val me = _ui.value.me ?: return emptyList()
        val heading = _ui.value.myHeadingDeg ?: 0.0
        return calculator.buildTargets(me, heading, _ui.value.players, nowMs)
    }

    private fun startHeading() {
        if (rotationSensor == null || sensorManager == null) {
            _ui.update { it.copy(myHeadingDeg = null) }
            return
        }
        if (sensorListener != null) return

        val listener = object : SensorEventListener {
            private val rot = FloatArray(9)
            private val ori = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rot, event.values)
                SensorManager.getOrientation(rot, ori)
                // azimuth in radians, convert to degrees 0..360
                var deg = Math.toDegrees(ori[0].toDouble())
                if (deg < 0) deg += 360.0
                // light smoothing to reduce jitter
                val prev = _ui.value.myHeadingDeg
                val smoothed = if (prev == null) deg else (prev * 0.85 + deg * 0.15)
                _ui.update { it.copy(myHeadingDeg = smoothed) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorListener = listener
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun stopHeading() {
        sensorListener?.let { l -> sensorManager?.unregisterListener(l) }
        sensorListener = null
    }

    private fun generateCode(): String {
        // simple 6-digit code
        return Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
    }
}
