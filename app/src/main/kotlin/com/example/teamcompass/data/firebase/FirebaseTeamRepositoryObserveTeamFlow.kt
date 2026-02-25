package com.example.teamcompass.data.firebase

import android.util.Log
import com.example.teamcompass.BuildConfig
import com.example.teamcompass.core.GeoCell
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPoint
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.perf.TeamCompassPerfMetrics
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

internal fun observeTeamFlow(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    viewMode: TeamViewMode,
    selfPoint: LocationPoint?,
    lastStateCellByUid: MutableMap<String, StateCellEntry>,
    stateCellCachePruneInProgress: AtomicBoolean,
    initialStateCellsReadDeniedInSession: Boolean,
    setStateCellsReadDeniedInSession: (Boolean) -> Unit,
    logTag: String,
): Flow<TeamSnapshot> = callbackFlow {
        var stateCellsReadDeniedInSession = initialStateCellsReadDeniedInSession
        val code = validatedTeamCodeOrNull(teamCode)
        if (code == null) {
            close(IllegalArgumentException("Invalid team code format"))
            return@callbackFlow
        }
        val base = backendClient.child("teams/$code")

        val players = linkedMapOf<String, PlayerState>()
        val roleProfiles = linkedMapOf<String, TeamMemberRoleProfile>()
        val roleByUid = linkedMapOf<String, Role>()
        val teamPoints = linkedMapOf<String, TeamPoint>()
        val privatePoints = linkedMapOf<String, TeamPoint>()
        val enemyPings = linkedMapOf<String, TeamEnemyPing>()
        val teamPointsSorted = mutableListOf<TeamPoint>()
        val privatePointsSorted = mutableListOf<TeamPoint>()
        val enemyPingsSorted = mutableListOf<TeamEnemyPing>()
        var activeCommand: TeamActiveCommand? = null
        var playersDirty = true
        var cachedPlayers: List<PlayerState> = emptyList()
        var rolesDirty = true
        var cachedRoleProfiles: List<TeamMemberRoleProfile> = emptyList()
        var enemyDirty = true
        var cachedEnemyPings: List<TeamEnemyPing> = emptyList()
        var pendingEmitJob: Job? = null

        fun rebuildCachesIfNeeded(nowMs: Long) {
            if (playersDirty) {
                cachedPlayers = players.values.map { player ->
                    val legacyRole = roleByUid[player.uid] ?: Role.FIGHTER
                    if (player.role == legacyRole) player else player.copy(role = legacyRole)
                }
                playersDirty = false
            }
            if (rolesDirty) {
                cachedRoleProfiles = roleProfiles.values.toList()
                rolesDirty = false
            }
            if (enemyDirty) {
                cachedEnemyPings = enemyPingsSorted.filter { it.expiresAtMs > nowMs }
                enemyDirty = false
            }
        }

        fun emitSnapshotNow() {
            val now = System.currentTimeMillis()
            rebuildCachesIfNeeded(nowMs = now)
            TeamCompassPerfMetrics.recordRtdbSnapshotEmit()
            trySend(
                TeamSnapshot(
                    players = cachedPlayers,
                    teamPoints = teamPointsSorted.toList(),
                    privatePoints = privatePointsSorted.toList(),
                    enemyPings = cachedEnemyPings,
                    roleProfiles = cachedRoleProfiles,
                    activeCommand = activeCommand?.takeIf { now - it.createdAtMs <= 60_000L },
                )
            )
        }

        fun scheduleEmit() {
            if (pendingEmitJob?.isActive == true) return
            pendingEmitJob = launch {
                delay(SNAPSHOT_DEBOUNCE_MS)
                emitSnapshotNow()
            }
        }

        val stateRefsByKey = linkedMapOf<String, DatabaseReference>()
        val stateListenersByKey = linkedMapOf<String, ChildEventListener>()
        var currentSelfCellId = selfPoint?.let { GeoCell.encode(it.lat, it.lon, STATE_CELL_PRECISION) }
        val stateCellsEnabled = BuildConfig.STATE_CELLS_V1_ENABLED
        var stateCellsReadEnabled = shouldEnableStateCellsRead(
            stateCellsEnabled = stateCellsEnabled,
            stateCellsReadDeniedInSession = stateCellsReadDeniedInSession,
        )
        var stateCellsFallbackApplied = false
        var stateSubscriptionJob: Job? = null
        var selfStateRef: DatabaseReference? = null
        var selfStateListener: ValueEventListener? = null
        lateinit var syncStateListeners: (Map<String, DatabaseReference>) -> Unit

        fun onStateSnapshotChanged(snapshot: DataSnapshot) {
            parsePlayer(snapshot, uid)?.let { players[it.uid] = it } ?: run { players.remove(snapshot.key) }
            playersDirty = true
            scheduleEmit()
        }

        fun addStateListener(key: String, ref: DatabaseReference) {
            if (stateListenersByKey.containsKey(key)) return
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    onStateSnapshotChanged(snapshot)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    onStateSnapshotChanged(snapshot)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot.key?.let { players.remove(it) }
                    playersDirty = true
                    scheduleEmit()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

                override fun onCancelled(error: DatabaseError) {
                    if (isStateCellPermissionDenied(
                            listenerKey = key,
                            errorCode = error.code,
                        )
                    ) {
                        if (!stateCellsReadEnabled) {
                            return
                        }
                    }
                    if (shouldFallbackToLegacyStateListener(
                            listenerKey = key,
                            errorCode = error.code,
                            stateCellsReadEnabled = stateCellsReadEnabled,
                        )
                    ) {
                        stateCellsReadEnabled = false
                        stateCellsReadDeniedInSession = true
                        setStateCellsReadDeniedInSession(true)
                        if (!stateCellsFallbackApplied) {
                            stateCellsFallbackApplied = true
                            Log.w(
                                logTag,
                                "stateCells read denied for team=$code key=$key; fallback to /state listener",
                            )
                        }
                        syncStateListeners(mapOf("state" to base.child("state")))
                        return
                    }
                    close(error.toException())
                }
            }
            ref.addChildEventListener(listener)
            stateRefsByKey[key] = ref
            stateListenersByKey[key] = listener
        }

        fun removeStateListener(key: String) {
            val ref = stateRefsByKey.remove(key) ?: return
            val listener = stateListenersByKey.remove(key) ?: return
            ref.removeEventListener(listener)
        }

        suspend fun preflightStateCellsRead(cellId: String?) {
            if (viewMode == TeamViewMode.COMMAND || !stateCellsReadEnabled) return
            val probeCellId = cellId ?: return
            val probePath = "teams/$code/stateCells/$probeCellId"
            var probeTimedOut = false
            val probeResult = withTimeoutOrNull(STATE_CELL_PREFLIGHT_TIMEOUT_MS) {
                runCatching { backendClient.get(path = probePath) }
            } ?: run {
                probeTimedOut = true
                null
            }
            val probeFailure = probeResult?.exceptionOrNull()
            if (!shouldFallbackAfterStateCellsPreflight(timedOut = probeTimedOut, failure = probeFailure)) return

            stateCellsReadEnabled = false
            stateCellsReadDeniedInSession = true
                        setStateCellsReadDeniedInSession(true)
            if (!stateCellsFallbackApplied) {
                stateCellsFallbackApplied = true
                val reason = when {
                    probeTimedOut -> "timeout"
                    probeFailure == null -> "unknown"
                    isStateCellsProbePermissionDenied(probeFailure) -> "permission_denied"
                    else -> "probe_failure"
                }
                Log.w(
                    logTag,
                    "stateCells preflight fallback for team=$code cell=$probeCellId reason=$reason; fallback to /state listener",
                )
            }
        }

        syncStateListeners = { nextRefs ->
            val currentKeys = stateListenersByKey.keys.toSet()
            val nextKeys = nextRefs.keys
            if (currentKeys == nextKeys) {
                Unit
            } else {
                (currentKeys - nextKeys).forEach { key -> removeStateListener(key) }
                nextRefs.forEach { (key, ref) -> addStateListener(key = key, ref = ref) }

                players.clear()
                playersDirty = true
                scheduleEmit()
            }
        }

        fun applyStateSubscription(nextSelfPoint: LocationPoint?) {
            val nextCellId = nextSelfPoint?.let { GeoCell.encode(it.lat, it.lon, STATE_CELL_PRECISION) }
            if (nextCellId == currentSelfCellId && stateListenersByKey.isNotEmpty()) return
            currentSelfCellId = nextCellId
            stateSubscriptionJob?.cancel()
            stateSubscriptionJob = launch {
                preflightStateCellsRead(cellId = nextCellId)
                syncStateListeners(
                    desiredStateRefsForCell(
                        viewMode = viewMode,
                        stateCellsReadEnabled = stateCellsReadEnabled,
                        cellId = nextCellId,
                        base = base,
                    )
                )
            }
        }

        fun maybeAttachSelfStateListener() {
            if (viewMode == TeamViewMode.COMMAND || !stateCellsReadEnabled) return
            if (selfStateListener != null) return
            val ref = base.child("state").child(uid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lat = snapshot.child("lat").doubleOrNull() ?: return
                    val lon = snapshot.child("lon").doubleOrNull() ?: return
                    val acc = snapshot.child("acc").doubleOrNull() ?: 0.0
                    val speed = snapshot.child("speed").doubleOrNull() ?: 0.0
                    val heading = snapshot.child("heading").doubleOrNull()
                    val ts = snapshot.child("ts").longOrZero()
                    applyStateSubscription(
                        LocationPoint(
                            lat = lat,
                            lon = lon,
                            accMeters = acc,
                            speedMps = speed,
                            headingDeg = heading,
                            timestampMs = ts,
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            ref.addValueEventListener(listener)
            selfStateRef = ref
            selfStateListener = listener
        }

        preflightStateCellsRead(currentSelfCellId)
        applyStateSubscription(selfPoint)
        maybeAttachSelfStateListener()

        val roleRef = base.child("memberRoles")
        val roleListener = buildRoleProfilesChildEventListener(
            roleProfiles = roleProfiles,
            roleByUid = roleByUid,
            onDirtyFlagsChanged = {
                rolesDirty = true
                playersDirty = true
            },
            onChanged = ::scheduleEmit,
            onCancelled = { error -> close(error.toException()) },
        )
        roleRef.addChildEventListener(roleListener)

        val pointsRef = base.child("points").orderByChild("createdAtMs").limitToLast(TEAM_POINTS_LIMIT_TO_LAST)
        val pointsListener = buildSnapshotChildEventListener(
            parse = { parsePoint(it, isTeam = true) },
            map = teamPoints,
            sorted = teamPointsSorted,
            idOf = TeamPoint::id,
            createdAtOf = TeamPoint::createdAtMs,
            maxItems = TEAM_POINTS_LIMIT_TO_LAST,
            onChanged = ::scheduleEmit,
            onCancelled = { error -> close(error.toException()) },
        )
        pointsRef.addChildEventListener(pointsListener)

        val privateRef = base.child("privatePoints").child(uid).orderByChild("createdAtMs").limitToLast(PRIVATE_POINTS_LIMIT_TO_LAST)
        val privateListener = buildSnapshotChildEventListener(
            parse = { parsePoint(it, isTeam = false) },
            map = privatePoints,
            sorted = privatePointsSorted,
            idOf = TeamPoint::id,
            createdAtOf = TeamPoint::createdAtMs,
            maxItems = PRIVATE_POINTS_LIMIT_TO_LAST,
            onChanged = ::scheduleEmit,
            onCancelled = { error -> close(error.toException()) },
        )
        privateRef.addChildEventListener(privateListener)

        val enemyRef = base.child("enemyPings").orderByChild("createdAtMs").limitToLast(MAX_ENEMY_PINGS_CACHE)
        val enemyListener = buildSnapshotChildEventListener(
            parse = ::parseEnemyPing,
            map = enemyPings,
            sorted = enemyPingsSorted,
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
            maxItems = MAX_ENEMY_PINGS_CACHE,
            beforeMutation = { enemyDirty = true },
            onChanged = ::scheduleEmit,
            onCancelled = { error -> close(error.toException()) },
        )
        enemyRef.addChildEventListener(enemyListener)

        val commandRef = base.child("commands").child("active")
        val commandListener = buildActiveCommandValueListener(
            onCommandChanged = {
                activeCommand = it
                scheduleEmit()
            },
            onCancelled = { error -> close(error.toException()) },
        )
        commandRef.addValueEventListener(commandListener)

        emitSnapshotNow()
        val sweepJob = launch {
            // Keep TTL cleanup off hot path and still refresh UI for expirations.
            while (true) {
                delay(ENEMY_CLEANUP_TICK_MS)
                val now = System.currentTimeMillis()
                val cleanupPlan = planEnemyPingCleanupAndEmit(
                    enemyPings = enemyPings.values,
                    nowMs = now,
                    maxDeletes = ENEMY_CLEANUP_MAX_OPS,
                )
                val deleted = cleanupExpiredEnemyPings(
                    backendClient = backendClient,
                    teamCode = code,
                    idsToDelete = cleanupPlan.idsToDelete,
                    logTag = logTag,
                )
                TeamCompassPerfMetrics.recordRtdbCleanupSweep(deleteWrites = deleted)
                enemyDirty = true
                if (cleanupPlan.emitStrategy == EnemyCleanupEmitStrategy.SCHEDULE_DEBOUNCED) {
                    scheduleEmit()
                } else {
                    emitSnapshotNow()
                }
            }
        }

        awaitClose {
            pendingEmitJob?.cancel()
            sweepJob.cancel()
            stateSubscriptionJob?.cancel()
            stateRefsByKey.forEach { (key, ref) ->
                val listener = stateListenersByKey[key] ?: return@forEach
                ref.removeEventListener(listener)
            }
            selfStateRef?.let { ref ->
                selfStateListener?.let { listener -> ref.removeEventListener(listener) }
            }
            roleRef.removeEventListener(roleListener)
            pointsRef.removeEventListener(pointsListener)
            privateRef.removeEventListener(privateListener)
            enemyRef.removeEventListener(enemyListener)
            commandRef.removeEventListener(commandListener)
        }
    }
