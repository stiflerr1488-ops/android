package com.example.teamcompass.data.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal fun observeBackendHealthFlow(
    backendClient: RealtimeBackendClient,
    logTag: String = "FirebaseTeamRepo",
): Flow<Boolean> = callbackFlow {
    val connectedRef = backendClient.child(".info/connected")
    var probeJob: Job? = null
    var publishedAvailability: Boolean? = null

    fun publishAvailability(available: Boolean) {
        if (publishedAvailability == available) return
        publishedAvailability = available
        trySend(available)
    }

    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val connected = snapshot.getValue(Boolean::class.java) ?: false
            if (!connected) {
                probeJob?.cancel()
                publishAvailability(false)
                return
            }
            probeJob?.cancel()
            probeJob = launch {
                var consecutiveProbeFailures = 0
                var probeDegradedLogged = false
                var probePermissionDeniedLogged = false
                publishAvailability(true)
                while (true) {
                    val probeResult = runCatching {
                        backendClient.get(path = ".info/serverTimeOffset")
                    }
                    val probeFailure = probeResult.exceptionOrNull()
                    val shouldCountFailure = shouldCountBackendProbeFailure(probeFailure)
                    val probeSucceeded = probeResult.isSuccess || !shouldCountFailure
                    consecutiveProbeFailures = if (probeSucceeded) {
                        if (probeFailure != null && !shouldCountFailure && !probePermissionDeniedLogged) {
                            probePermissionDeniedLogged = true
                            Log.i(
                                logTag,
                                "backend health probe skipped: permission denied for .info/serverTimeOffset",
                            )
                        } else if (probeResult.isSuccess && probePermissionDeniedLogged) {
                            probePermissionDeniedLogged = false
                        }
                        if (probeDegradedLogged) {
                            Log.i(logTag, "backend health probe recovered")
                            probeDegradedLogged = false
                        }
                        0
                    } else {
                        (consecutiveProbeFailures + 1).coerceAtMost(BACKEND_HEALTH_PROBE_FAILURE_THRESHOLD)
                    }
                    val available = computeBackendReachabilitySample(
                        connected = true,
                        consecutiveProbeFailures = consecutiveProbeFailures,
                    )
                    if (!probeSucceeded &&
                        consecutiveProbeFailures >= BACKEND_HEALTH_PROBE_FAILURE_THRESHOLD &&
                        !probeDegradedLogged
                    ) {
                        probeDegradedLogged = true
                        Log.w(
                            logTag,
                            "backend health probe degraded ($consecutiveProbeFailures/$BACKEND_HEALTH_PROBE_FAILURE_THRESHOLD)",
                        )
                    }
                    publishAvailability(available)
                    delay(BACKEND_HEALTH_PROBE_INTERVAL_MS)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            probeJob?.cancel()
            publishAvailability(false)
            close(error.toException())
        }
    }
    connectedRef.addValueEventListener(listener)
    awaitClose {
        probeJob?.cancel()
        connectedRef.removeEventListener(listener)
    }
}
