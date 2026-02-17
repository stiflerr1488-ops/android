package com.example.teamcompass

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.ArrayDeque
import kotlin.math.roundToInt

/**
 * MVP экран:
 *  - Анонимный логин
 *  - Вход в команду по 6-значному коду
 *  - Позывной (сохраняется между перезапусками)
 *  - Отправка своей позиции в Firebase
 *  - Список тиммейтов: стрелка/дистанция/last seen
 */
class MainActivity : Activity(), SensorEventListener {

    private val prefs by lazy { getSharedPreferences("teamcompass", Context.MODE_PRIVATE) }

    private lateinit var statusView: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var codeEdit: EditText
    private lateinit var callsignEdit: EditText
    private lateinit var btnJoin: Button
    private lateinit var btnCreate: Button
    private lateinit var btnLeave: Button
    private lateinit var btnGrantLocation: Button

    private var uid: String? = null
    private var teamCode: String? = null
    private var callsign: String = ""
    private var authInProgress = false
    private var autoJoinAttempted = false
    private val pendingAuthActions = ArrayDeque<(String) -> Unit>()

    // Location
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var myLocation: Location? = null

    // Heading
    private val sensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private var rotationVector: Sensor? = null
    private var sensorHeadingDeg: Float? = null
    private var gpsHeadingDeg: Float? = null
    private var effectiveHeadingDeg: Float? = null

    // Firebase
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private var stateListener: ValueEventListener? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            myLocation = loc

            // GPS bearing is very stable when moving
            if (loc.hasSpeed() && loc.speed > 1.5f && loc.hasBearing()) {
                gpsHeadingDeg = normalize360(loc.bearing)
            }

            effectiveHeadingDeg = chooseEffectiveHeading()
            renderTeammatesLastKnown() // update arrows even if teammates unchanged
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load saved
        callsign = prefs.getString("callsign", "") ?: ""
        teamCode = prefs.getString("teamCode", "")?.takeIf { it.isNotBlank() }

        // UI
        setContentView(R.layout.activity_main)

        statusView = findViewById<TextView>(R.id.statusView).apply {
            text = "TeamCompass MVP\n"
        }

        callsignEdit = findViewById<EditText>(R.id.callsignEdit).apply {
            setText(callsign)
        }

        codeEdit = findViewById<EditText>(R.id.codeEdit).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(6))
            setText(teamCode ?: "")
        }

        btnJoin = findViewById<Button>(R.id.btnJoin).apply {
            isEnabled = false
            setOnClickListener { joinTeam() }
        }

        btnCreate = findViewById<Button>(R.id.btnCreate).apply {
            isEnabled = false
            setOnClickListener { createTeamAndJoin() }
        }

        btnLeave = findViewById<Button>(R.id.btnLeave).apply {
            isEnabled = false
            setOnClickListener { leaveTeam() }
        }

        btnGrantLocation = findViewById<Button>(R.id.btnGrantLocation).apply {
            visibility = View.GONE
            setOnClickListener { requestLocationPermission() }
        }

        listContainer = findViewById<LinearLayout>(R.id.listContainer)

        // Sensors
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVector == null) {
            append("⚠️ Нет датчика компаса (rotation vector). Стрелки будут норм только в движении (по GPS-курсу).\n")
        }

        // Auto sign-in if possible
        FirebaseAuth.getInstance().currentUser?.let {
            uid = it.uid
            append("✅ Уже авторизован. uid=${it.uid}\n")
            btnJoin.isEnabled = true
            btnCreate.isEnabled = true
            tryAutoJoinSavedTeam()
        } ?: run {
            append("Выполняем анонимный вход...\n")
            signInIfNeeded()
            tryAutoJoinSavedTeam()
        }
    }

    private fun tryAutoJoinSavedTeam() {
        if (autoJoinAttempted) return

        val savedCode = teamCode
        val savedCallsign = callsign
        if (savedCode.isNullOrBlank() || savedCallsign.isBlank()) {
            return
        }

        autoJoinAttempted = true
        append("↪️ Автозаход в команду $savedCode...\n")
        joinTeam()
    }

    override fun onResume() {
        super.onResume()
        // If already in team, resume sensors/location
        val userId = uid
        val code = teamCode
        if (!code.isNullOrBlank() && !userId.isNullOrBlank() && btnLeave.isEnabled) {
            startSensors()
            startLocationUpdatesIfPermitted()
            TrackingForegroundService.start(this, userId, code, callsign)
        }
    }

    override fun onPause() {
        super.onPause()
        stopSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensors()
        stopLocationUpdates()
        detachStateListener()
    }

    private fun append(msg: String) {
        statusView.append(msg)
    }

    private fun signInIfNeeded() {
        if (uid != null || authInProgress) return
        authInProgress = true
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                uid = it.user?.uid
                append("✅ Вошёл. uid=$uid\n")
                btnJoin.isEnabled = true
                btnCreate.isEnabled = true
                authInProgress = false
                val signedUid = uid
                while (pendingAuthActions.isNotEmpty() && !signedUid.isNullOrBlank()) {
                    pendingAuthActions.removeFirst().invoke(signedUid)
                }
            }
            .addOnFailureListener { e ->
                authInProgress = false
                append("❌ Ошибка входа: ${e.message}\n")
            }
    }

    private fun withSignedInUser(action: (String) -> Unit) {
        val currentUid = uid
        if (!currentUid.isNullOrBlank()) {
            action(currentUid)
            return
        }

        pendingAuthActions.addLast(action)
        if (!authInProgress) {
            append("Выполняем анонимный вход...\n")
        }
        signInIfNeeded()
    }

    private fun joinTeam() {
        withSignedInUser { u ->

            val code = codeEdit.text.toString().trim()
            val cs = callsignEdit.text.toString().trim()

            if (!code.matches(Regex("\\d{6}"))) {
                append("❗ Код должен быть из 6 цифр (например 012345)\n")
                return@withSignedInUser
            }
            if (cs.isBlank()) {
                append("❗ Введи позывной\n")
                return@withSignedInUser
            }

        // Save locally
            prefs.edit().putString("callsign", cs).putString("teamCode", code).apply()
            callsign = cs
            teamCode = code

            append("Подключаемся к команде $code...\n")

            val member = hashMapOf(
                "callsign" to cs,
                "joinedAt" to ServerValue.TIMESTAMP
            )

            db.child("teams").child(code).child("members").child(u)
                .setValue(member)
                .addOnSuccessListener {
                    append("✅ В команде $code как '$cs'\n")
                    btnLeave.isEnabled = true
                    startSensors()
                    startLocationUpdatesIfPermitted()
                    TrackingForegroundService.start(this, u, code, callsign)
                    append("🛰️ Foreground-service трекинг активирован\n")
                    attachStateListener(code)
                }
                .addOnFailureListener { e ->
                    append("❌ Не удалось войти в команду: ${e.message}\n")
                }
        }
    }

    private fun createTeamAndJoin() {
        withSignedInUser { u ->

            val cs = callsignEdit.text.toString().trim()
            if (cs.isBlank()) {
                append("❗ Введи позывной (он сохранится)\n")
                return@withSignedInUser
            }

            val code = (0..999999).random().toString().padStart(6, '0')
            codeEdit.setText(code)
            append("🆕 Создан код команды: $code (отправь его ребятам)\n")

        // Best-effort meta (не критично, но удобно видеть в базе)
            db.child("teams").child(code).child("meta").updateChildren(
                mapOf(
                    "createdAt" to ServerValue.TIMESTAMP,
                    "createdBy" to u
                )
            )

            joinTeam()
        }
    }

    private fun leaveTeam() {
        stopSensors()
        stopLocationUpdates()
        TrackingForegroundService.stop(this)
        detachStateListener()

        append("\n— Вышел (локально).\n")
        btnLeave.isEnabled = false

        // На сервере мы членство не удаляем (чтобы случайно не сломать матч).
        // Если захотите — добавим кнопку "Покинуть" которая удаляет members/{uid}.
    }

    // -----------------------------
    // Location
    // -----------------------------

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQ_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                btnGrantLocation.visibility = View.GONE
                append("✅ Геолокация разрешена\n")
                startLocationUpdatesIfPermitted()
                val u = uid
                val code = teamCode
                if (!u.isNullOrBlank() && !code.isNullOrBlank() && btnLeave.isEnabled) {
                    TrackingForegroundService.start(this, u, code, callsign)
                }
            } else {
                append("❌ Без геолокации стрелки/дистанция не будут работать\n")
            }
        }
    }

    private fun startLocationUpdatesIfPermitted() {
        if (!btnLeave.isEnabled) return // not joined

        if (!hasLocationPermission()) {
            btnGrantLocation.visibility = View.VISIBLE
            append("Нужно разрешение на геолокацию\n")
            return
        }
        btnGrantLocation.visibility = View.GONE

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .setWaitForAccurateLocation(false)
            .build()

        fused.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        append("📍 Локальные обновления экрана включены\n")
    }

    private fun stopLocationUpdates() {
        fused.removeLocationUpdates(locationCallback)
    }

    // -----------------------------
    // Sensors (heading)
    // -----------------------------

    private fun startSensors() {
        rotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotMat = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotMat, event.values)

        val orientations = FloatArray(3)
        SensorManager.getOrientation(rotMat, orientations)

        // orientations[0] = azimuth in radians
        val azimuthRad = orientations[0]
        var deg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        deg = normalize360(deg)

        sensorHeadingDeg = smoothAngle(sensorHeadingDeg, deg, 0.15f)
        effectiveHeadingDeg = chooseEffectiveHeading()
        renderTeammatesLastKnown()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }

    private fun chooseEffectiveHeading(): Float? {
        val loc = myLocation
        val gps = gpsHeadingDeg
        return if (loc != null && loc.hasSpeed() && loc.speed > 1.5f && gps != null) {
            gps
        } else {
            sensorHeadingDeg
        }
    }

    // -----------------------------
    // Firebase listener + rendering
    // -----------------------------

    private var lastStatesSnapshot: Map<String, PlayerState> = emptyMap()

    private fun attachStateListener(code: String) {
        detachStateListener()

        val ref = db.child("teams").child(code).child("state")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, PlayerState>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val lat = child.child("lat").getValue(Double::class.java)
                    val lon = child.child("lon").getValue(Double::class.java)
                    val acc = child.child("acc").getValue(Double::class.java)
                    val ts = child.child("ts").getValue(Long::class.java)
                    val cs = child.child("callsign").getValue(String::class.java) ?: ""
                    if (lat != null && lon != null && acc != null && ts != null) {
                        map[id] = PlayerState(id, cs, lat, lon, acc, ts)
                    }
                }
                lastStatesSnapshot = map
                renderTeammatesLastKnown()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                append("❌ Listener cancelled: ${error.message}\n")
            }
        }

        ref.addValueEventListener(listener)
        stateListener = listener
        append("📡 Слушаем /teams/$code/state\n")
    }

    private fun detachStateListener() {
        val code = teamCode ?: return
        val listener = stateListener ?: return
        db.child("teams").child(code).child("state").removeEventListener(listener)
        stateListener = null
        lastStatesSnapshot = emptyMap()
        listContainer.removeAllViews()
    }

    private fun renderTeammatesLastKnown() {
        if (!btnLeave.isEnabled) return

        val u = uid ?: return
        val cs = callsign

        val myLoc = myLocation
        val myHeading = effectiveHeadingDeg

        // We'll also read callsigns (best-effort) - MVP: use uid if no callsign
        val now = System.currentTimeMillis()

        listContainer.removeAllViews()

        // Add "me" line
        val meLine = TextView(this).apply {
            textSize = 15f
            text = buildString {
                append("Я: ")
                append(if (cs.isNotBlank()) cs else u.take(6))
                if (myLoc != null) {
                    append(" • acc=")
                    append(myLoc.accuracy.roundToInt())
                    append("м")
                } else {
                    append(" • ждём GPS...")
                }
                if (myHeading != null) {
                    append(" • heading=")
                    append(myHeading.roundToInt())
                    append("°")
                }
            }
        }
        listContainer.addView(meLine)

        if (lastStatesSnapshot.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "(пока нет данных)"
                setPadding(0, 8, 0, 0)
            })
            return
        }

        if (myLoc == null) {
            listContainer.addView(TextView(this).apply {
                text = "Ждём координаты, чтобы считать дистанции..."
                setPadding(0, 8, 0, 0)
            })
            return
        }

        // Render others
        val others = lastStatesSnapshot.values
            .filter { it.uid != u }
            .sortedBy { it.ageSec(now) }

        for (st in others) {
            val ageSec = st.ageSec(now)
            if (ageSec > STALE_HIDE_AFTER_SEC) {
                // hide fully
                continue
            }

            val bearing = bearingDeg(myLoc.latitude, myLoc.longitude, st.lat, st.lon)
            val distM = distanceMeters(myLoc.latitude, myLoc.longitude, st.lat, st.lon)

            val rel = if (myHeading != null) {
                normalize180(bearing - myHeading)
            } else {
                // No heading available (no compass + not moving). Show bearing from North.
                bearing
            }

            val arrow = if (myHeading != null) arrow8(rel) else "N→" // indicate bearing reference

            val staleMark = when {
                ageSec <= 20 -> ""
                ageSec <= 60 -> " (сомн)"
                else -> " (старые)"
            }

            val accMark = if (st.acc > 50.0) " ⚠️acc" else ""

            val name = (st.callsign.takeIf { it.isNotBlank() } ?: "uid=${st.uid.take(6)}")

            val line = TextView(this).apply {
                textSize = 15f
                setPadding(0, 10, 0, 0)
                text = "$arrow $name • ${distM}м • ${ageSec}с назад$staleMark$accMark"
            }
            listContainer.addView(line)
        }

        if (others.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "(тиммейты не присылали точки)"
                setPadding(0, 8, 0, 0)
            })
        }
    }

    // -----------------------------
    // Geometry helpers
    // -----------------------------

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val res = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, res)
        return res[0].roundToInt()
    }

    private fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val a = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        val b = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        return normalize360(a.bearingTo(b))
    }

    private fun normalize360(deg: Float): Float {
        var d = deg % 360f
        if (d < 0) d += 360f
        return d
    }

    private fun normalize180(deg: Float): Float {
        var d = deg % 360f
        if (d > 180f) d -= 360f
        if (d < -180f) d += 360f
        return d
    }

    private fun smoothAngle(old: Float?, new: Float, alpha: Float): Float {
        if (old == null) return new
        // shortest path around the circle
        var delta = normalize180(new - old)
        return normalize360(old + alpha * delta)
    }

    private fun arrow8(relDeg: Float): String {
        // relDeg in -180..180: 0 = straight ahead
        val dirs = arrayOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
        val angle = normalize360(relDeg)
        val idx = ((angle / 45f).roundToInt()) % 8
        return dirs[idx]
    }

    data class PlayerState(
        val uid: String,
        val callsign: String,
        val lat: Double,
        val lon: Double,
        val acc: Double,
        val ts: Long
    ) {
        fun ageSec(nowMs: Long): Long = ((nowMs - ts) / 1000L).coerceAtLeast(0L)
    }

    companion object {
        private const val REQ_LOCATION = 1001

        // Staleness UI
        private const val STALE_HIDE_AFTER_SEC = 120L
    }
}
