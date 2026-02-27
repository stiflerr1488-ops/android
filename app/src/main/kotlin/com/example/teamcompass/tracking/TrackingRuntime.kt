package com.example.teamcompass.tracking

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.MovementState
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingPolicies
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.domain.TrackingTelemetry
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * Location tracking engine running behind [TrackingControllerImpl].
 *
 * Scope ownership:
 * - Creates and owns its internal CoroutineScope.
 * - Child jobs are cancelled in [stop].
 * - The scope itself is cancelled in [close], called from
 *   [TrackingControllerImpl.onServiceDestroyed] when [com.example.teamcompass.TrackingService] dies.
 */
class TrackingRuntime(
    private val app: Application,
    private val repository: TeamRepository,
    private val coroutineExceptionHandler: CoroutineExceptionHandler,
) {
    private var scope = trackingRuntimeNewScope(coroutineExceptionHandler)
    private val sendMutex = Mutex()
    private val lock = Any()

    private val fused = LocationServices.getFusedLocationProviderClient(app)

    // Adaptive movement-state tracker.
    private val adaptiveSpeedTracker = AdaptiveSpeedTracker()
    private var currentMovementState: MovementState = MovementState.STATIONARY

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _location = MutableStateFlow<LocationPoint?>(null)
    val location: StateFlow<LocationPoint?> = _location.asStateFlow()

    private val _isAnchored = MutableStateFlow(false)
    val isAnchored: StateFlow<Boolean> = _isAnchored.asStateFlow()

    private val _telemetry = MutableStateFlow(TrackingTelemetry())
    val telemetry: StateFlow<TrackingTelemetry> = _telemetry.asStateFlow()

    private var session: TrackingSessionConfig? = null
    private var playerMode: PlayerMode = PlayerMode.GAME
    private var sosUntilMs: Long = 0L
    private var headingDeg: Double? = null

    private var locationCallback: LocationCallback? = null
    private var watchdogJob: Job? = null

    private var lastSentMs: Long = 0L
    private var lastMoveMs: Long = 0L
    private var lastMoveLoc: Location? = null
    private var forceSendNext: Boolean = false
    private var lastWatchdogRestartAtMs: Long = 0L
    
    // Adaptive tracking telemetry.
    private var stateEnterTimeMs: Long = 0L
    private var stationaryStartTimeMs: Long = 0L
    private var walkingStartTimeMs: Long = 0L
    private var vehicleStartTimeMs: Long = 0L

    fun start(config: TrackingSessionConfig) {
        ensureScope()
        stop()
        synchronized(lock) {
            session = config
            playerMode = config.playerMode
            sosUntilMs = config.sosUntilMs
            lastSentMs = 0L
            lastMoveMs = 0L
            lastMoveLoc = null
            forceSendNext = true
            lastWatchdogRestartAtMs = 0L
            _isAnchored.value = false
            // Reset adaptive tracker state.
            adaptiveSpeedTracker.reset()
            currentMovementState = MovementState.STATIONARY
            // Reset telemetry counters.
            stateEnterTimeMs = System.currentTimeMillis()
            stationaryStartTimeMs = 0L
            walkingStartTimeMs = 0L
            vehicleStartTimeMs = 0L
        }
        if (!trackingRuntimeHasLocationPermission(app)) {
            _telemetry.trackingRuntimeRecordErrorReason("Location permission missing")
            return
        }
        if (!trackingRuntimeIsLocationEnabled(app)) {
            _telemetry.trackingRuntimeRecordErrorReason("Location disabled in system settings")
            return
        }

        _isTracking.value = true
        if (!startLocationUpdates()) {
            _isTracking.value = false
            return
        }
        startWatchdog()
    }

    fun stop() {
        _isTracking.value = false
        watchdogJob?.cancel()
        watchdogJob = null
        locationCallback?.let { cb -> fused.removeLocationUpdates(cb) }
        locationCallback = null
        scope.coroutineContext.cancelChildren()
        synchronized(lock) {
            lastWatchdogRestartAtMs = 0L
            // Reset adaptive tracker state.
            adaptiveSpeedTracker.reset()
            currentMovementState = MovementState.STATIONARY
            // Reset telemetry counters.
            stateEnterTimeMs = 0L
            stationaryStartTimeMs = 0L
            walkingStartTimeMs = 0L
            vehicleStartTimeMs = 0L
        }
    }

    fun close() { stop(); scope.cancel() }

    fun updateHeading(heading: Double?) = synchronized(lock) { headingDeg = heading }

    fun updateStatus(mode: PlayerMode, sosUntil: Long, forceSend: Boolean) {
        synchronized(lock) {
            playerMode = mode
            sosUntilMs = sosUntil
            if (forceSend) {
                forceSendNext = true
            }
        }
        if (forceSend) {
            _location.value?.let { point ->
                sendState(point, anchored = _isAnchored.value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(): Boolean {
        val cfg = session ?: return false
        if (!trackingRuntimeHasLocationPermission(app) || !trackingRuntimeIsLocationEnabled(app)) return false

        // Base policy from selected mode (GAME/SILENT).
        val basePolicy = trackingRuntimePolicyFor(cfg.mode, cfg.gamePolicy, cfg.silentPolicy)

        val movementStateNow = synchronized(lock) { currentMovementState }
        // Adaptive policy based on current movement state.
        val adaptivePolicy = TrackingPolicies.adaptiveForMovement(basePolicy, movementStateNow)

        val modeNow = synchronized(lock) { playerMode }

        // Adaptive location priority based on movement state.
        val priority = trackingRuntimeLocationPriorityFor(movementStateNow)
        val request = trackingRuntimeBuildLocationRequest(
            priority = priority,
            minIntervalMs = adaptivePolicy.minIntervalMs,
            mode = modeNow,
            movementState = movementStateNow,
        )
        val cb = trackingRuntimeLocationCallback(::onLocation)
        return try {
            fused.requestLocationUpdates(request, cb, Looper.getMainLooper())
                .addOnFailureListener { e ->
                    _telemetry.trackingRuntimeRecordErrorReason("Location update failed: ${e.message}")
                }
            locationCallback = cb
            true
        } catch (err: SecurityException) {
            locationCallback = null
            _telemetry.trackingRuntimeRecordErrorReason(
                "Location permission revoked: ${err.message.orEmpty()}",
            )
            false
        }
    }

    private fun restartLocationUpdates(reason: String) {
        _telemetry.trackingRuntimeRecordRestart(reason)
        locationCallback?.let { cb -> fused.removeLocationUpdates(cb) }
        locationCallback = null
        if (!startLocationUpdates()) {
            _isTracking.value = false
            watchdogJob?.cancel()
            watchdogJob = null
        }
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = ensureScope().launch {
            while (true) {
                delay(20_000L)
                if (!_isTracking.value) continue
                val last = _telemetry.value.lastLocationAtMs
                val nowMs = System.currentTimeMillis()
                when (decideWatchdogAction(lastLocationAtMs = last, lastRestartAtMs = lastWatchdogRestartAtMs, nowMs = nowMs)) {
                    WatchdogAction.RESTART -> {
                        val staleMs = nowMs - last
                        lastWatchdogRestartAtMs = nowMs
                        restartLocationUpdates("No location update for ${staleMs} ms")
                    }

                    WatchdogAction.THROTTLED -> {
                        _telemetry.trackingRuntimeRecordWatchdogThrottled()
                    }

                    WatchdogAction.NO_ACTION -> Unit
                }
                // Publish current movement-state telemetry.
                val movementSnapshot = movementTelemetrySnapshot(nowMs)
                _telemetry.update {
                    it.copy(
                        currentMovementState = movementSnapshot.state.name,
                        stationaryTimeMs = movementSnapshot.stationaryMs,
                        walkingTimeMs = movementSnapshot.walkingMs,
                        vehicleTimeMs = movementSnapshot.vehicleMs,
                    )
                }
            }
        }
    }

    private fun movementTelemetrySnapshot(
        nowMs: Long = System.currentTimeMillis(),
    ): TrackingRuntimeMovementTelemetrySnapshot {
        return synchronized(lock) {
            trackingRuntimeMovementTelemetrySnapshot(
                currentState = currentMovementState,
                nowMs = nowMs,
                stateEnterTimeMs = stateEnterTimeMs,
                stationaryStartTimeMs = stationaryStartTimeMs,
                walkingStartTimeMs = walkingStartTimeMs,
                vehicleStartTimeMs = vehicleStartTimeMs,
            )
        }
    }

    private fun onLocation(loc: Location) {
        val cfg = session ?: return
        val now = System.currentTimeMillis()
        val currentHeading = synchronized(lock) { headingDeg } ?: if (loc.hasBearing()) loc.bearing.toDouble() else null
        val point = LocationPoint(
            lat = loc.latitude,
            lon = loc.longitude,
            accMeters = loc.accuracy.toDouble(),
            speedMps = loc.speed.toDouble(),
            headingDeg = currentHeading,
            timestampMs = now,
        )
        _location.value = point
        _telemetry.trackingRuntimeRecordLastLocation(now)

        // Update movement-state classification from new location.
        val newMovementState = adaptiveSpeedTracker.update(loc)
        val movementStateChanged = synchronized(lock) {
            val previousState = currentMovementState
            if (newMovementState != previousState) {
                // Accumulate time spent in previous state.
                val stateDuration = now - stateEnterTimeMs
                when (previousState) {
                    MovementState.STATIONARY -> stationaryStartTimeMs += stateDuration
                    MovementState.WALKING_SLOW, MovementState.WALKING_FAST -> walkingStartTimeMs += stateDuration
                    MovementState.VEHICLE -> vehicleStartTimeMs += stateDuration
                }
                stateEnterTimeMs = now
                currentMovementState = newMovementState
                true
            } else {
                false
            }
        }

        val prevAnchored = _isAnchored.value
        val moveDistanceM = lastMoveLoc?.distanceTo(loc)?.toDouble() ?: Double.MAX_VALUE
        val moved = if (moveDistanceM == Double.MAX_VALUE || moveDistanceM >= 5.0) {
            lastMoveLoc = Location(loc)
            lastMoveMs = now
            true
        } else {
            false
        }

        val modeNow = synchronized(lock) { playerMode }
        val anchored = (modeNow == PlayerMode.GAME) && (now - lastMoveMs >= 3 * 60_000L)
        _isAnchored.value = anchored

        val movementStateNow = synchronized(lock) { currentMovementState }
        // Resolve adaptive send interval for current movement state.
        val adaptivePolicy = TrackingPolicies.adaptiveForMovement(
            trackingRuntimePolicyFor(cfg.mode, cfg.gamePolicy, cfg.silentPolicy),
            movementStateNow
        )
        
        val intervalMs = resolveSendIntervalMs(
            mode = modeNow,
            anchored = anchored,
            adaptiveIntervalMs = adaptivePolicy.minIntervalMs,
        )
        val jitteredIntervalMs = applyIntervalJitter(
            intervalMs = intervalMs,
            uid = cfg.uid,
            nowMs = now,
        )
        val minDistanceMeters = resolveMinDistanceMeters(
            anchored = anchored,
            adaptiveMinDistanceMeters = adaptivePolicy.minDistanceMeters,
        )
        val shouldForce = synchronized(lock) { forceSendNext }
        val shouldSend = shouldSendState(
            shouldForce = shouldForce,
            lastSentMs = lastSentMs,
            nowMs = now,
            intervalMs = jitteredIntervalMs,
            movedDistanceMeters = moveDistanceM,
            minDistanceMeters = minDistanceMeters,
        )

        if (shouldSend) {
            sendState(point, anchored)
            lastSentMs = now
            synchronized(lock) { forceSendNext = false }
            // Count adaptive state sends.
            _telemetry.trackingRuntimeIncrementAdaptiveSends()
        } else if (shouldResetLastSentAfterMove(moved, prevAnchored, modeNow)) {
            lastSentMs = 0L
        }
        
        // Restart location updates when movement state changes to apply a new interval.
        if (movementStateChanged && _isTracking.value) {
            restartLocationUpdates("Movement state changed: ${newMovementState.name}")
        }
    }

    private fun sendState(point: LocationPoint, anchored: Boolean) {
        val cfg = session ?: return
        val modeNow = synchronized(lock) { playerMode }
        val sosNow = synchronized(lock) { sosUntilMs }
        val headingNow = synchronized(lock) { headingDeg } ?: point.headingDeg
        trackingRuntimeSendStateAsync(
            cfg = cfg,
            point = point,
            anchored = anchored,
            modeNow = modeNow,
            sosNow = sosNow,
            headingNow = headingNow,
            scope = ensureScope(),
            sendMutex = sendMutex,
            repository = repository,
            telemetry = _telemetry,
        )
    }

    private fun ensureScope(): CoroutineScope =
        if (scope.coroutineContext.isActive) {
            scope
        } else {
            trackingRuntimeNewScope(coroutineExceptionHandler).also { scope = it }
        }

    internal enum class WatchdogAction {
        NO_ACTION,
        RESTART,
        THROTTLED,
    }

    companion object {
        internal fun isWatchdogRestartAllowed(lastRestartAtMs: Long, nowMs: Long): Boolean =
            trackingIsWatchdogRestartAllowed(lastRestartAtMs, nowMs)

        internal fun decideWatchdogAction(
            lastLocationAtMs: Long,
            lastRestartAtMs: Long,
            nowMs: Long,
        ): WatchdogAction = trackingDecideWatchdogAction(lastLocationAtMs, lastRestartAtMs, nowMs)

        internal fun resolveSendIntervalMs(
            mode: PlayerMode,
            anchored: Boolean,
            adaptiveIntervalMs: Long,
        ): Long = trackingResolveSendIntervalMs(mode, anchored, adaptiveIntervalMs)

        internal fun applyIntervalJitter(
            intervalMs: Long,
            uid: String,
            nowMs: Long,
            jitterRatio: Double = 0.10,
        ): Long = trackingApplyIntervalJitter(intervalMs, uid, nowMs, jitterRatio)

        internal fun resolveMinDistanceMeters(
            anchored: Boolean,
            adaptiveMinDistanceMeters: Double,
        ): Double = trackingResolveMinDistanceMeters(anchored, adaptiveMinDistanceMeters)

        internal fun shouldSendState(
            shouldForce: Boolean,
            lastSentMs: Long,
            nowMs: Long,
            intervalMs: Long,
            movedDistanceMeters: Double,
            minDistanceMeters: Double,
        ): Boolean = trackingShouldSendState(
            shouldForce,
            lastSentMs,
            nowMs,
            intervalMs,
            movedDistanceMeters,
            minDistanceMeters,
        )

        internal fun shouldResetLastSentAfterMove(
            moved: Boolean,
            wasAnchored: Boolean,
            mode: PlayerMode,
        ): Boolean = trackingShouldResetLastSentAfterMove(moved, wasAnchored, mode)
    }
}
