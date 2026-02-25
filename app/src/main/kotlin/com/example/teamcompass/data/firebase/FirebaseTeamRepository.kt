package com.example.teamcompass.data.firebase

import com.example.teamcompass.BuildConfig
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamPoint
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamRolePatch
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.applyPatch
import com.example.teamcompass.domain.canAssignRole
import com.example.teamcompass.perf.TeamCompassPerfMetrics
import android.util.Log
import com.example.teamcompass.core.GeoCell
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.Role
import com.example.teamcompass.core.TeamCodeValidator
import com.example.teamcompass.core.TeamCodeSecurity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

internal const val MARKER_KIND_POINT = "POINT"
internal const val MARKER_KIND_ENEMY_PING = "ENEMY_PING"
internal const val MARKER_SCOPE_TEAM = "TEAM"
internal const val MARKER_SCOPE_PRIVATE = "PRIVATE"
internal const val MARKER_SCOPE_TEAM_EVENT = "TEAM_EVENT"
internal const val MARKER_STATE_ACTIVE = "ACTIVE"
internal const val MARKER_STATE_EXPIRED = "EXPIRED"
internal const val MARKER_STATE_DISABLED = "DISABLED"
internal const val TEAM_CODE_LENGTH = 6
internal const val SNAPSHOT_DEBOUNCE_MS = 75L
internal const val ENEMY_CLEANUP_TICK_MS = 7_500L
internal const val ENEMY_CLEANUP_MAX_OPS = 12
internal const val TEAM_POINTS_LIMIT_TO_LAST = 1_000
internal const val PRIVATE_POINTS_LIMIT_TO_LAST = 400
internal const val MAX_ENEMY_PINGS_CACHE = 128
private const val MIN_STATE_WRITE_INTERVAL_MS = 750L
private const val MIN_ENEMY_PING_INTERVAL_MS = 1_200L
private const val MAX_ENEMY_PING_TTL_MS = 600_000L
private const val LEGACY_JOIN_GRACE_MS = 14L * 24L * 60L * 60L * 1_000L
internal const val DEFAULT_SIDE_ID = "SIDE-1"
internal const val STATE_CELL_PRECISION = 6
internal const val STATE_CELL_CACHE_MAX_ENTRIES = 5_000
internal const val STATE_CELL_CACHE_PRUNE_TO_ENTRIES = 4_000
internal const val STATE_CELL_PREFLIGHT_TIMEOUT_MS = 1_250L
internal const val BACKEND_HEALTH_PROBE_INTERVAL_MS = 10_000L
internal const val BACKEND_HEALTH_PROBE_FAILURE_THRESHOLD = 6

class FirebaseTeamRepository internal constructor(
    private val backendClient: RealtimeBackendClient,
) : TeamRepository {
    constructor(db: DatabaseReference) : this(FirebaseRealtimeBackendClient(db))

    private val lastStateCellByUid: MutableMap<String, StateCellEntry> = ConcurrentHashMap()
    private val stateCellCachePruneInProgress = AtomicBoolean(false)
    @Volatile
    private var stateCellsReadDeniedInSession: Boolean = false
    private val lastStateWriteAtByUid: MutableMap<String, Long> = ConcurrentHashMap()
    private val lastEnemyPingAtByUid: MutableMap<String, Long> = ConcurrentHashMap()

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> = createTeamWrite(
        backendClient = backendClient,
        ownerUid = ownerUid,
        ownerCallsign = ownerCallsign,
        nowMs = nowMs,
        maxAttempts = maxAttempts,
    )

    override suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long,
    ): TeamActionResult<Unit> = joinTeamWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        callsign = callsign,
        nowMs = nowMs,
        legacyJoinGraceMs = LEGACY_JOIN_GRACE_MS,
        logTag = TAG,
    )

    override fun observeBackendHealth(): Flow<Boolean> =
        observeBackendHealthFlow(
            backendClient = backendClient,
            logTag = TAG,
        )

    override fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode,
        selfPoint: LocationPoint?,
    ): Flow<TeamSnapshot> = observeTeamFlow(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        viewMode = viewMode,
        selfPoint = selfPoint,
        lastStateCellByUid = lastStateCellByUid,
        stateCellCachePruneInProgress = stateCellCachePruneInProgress,
        initialStateCellsReadDeniedInSession = stateCellsReadDeniedInSession,
        setStateCellsReadDeniedInSession = { stateCellsReadDeniedInSession = it },
        logTag = TAG,
    )

    override suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit> =
        upsertStateWrite(
            backendClient = backendClient,
            teamCode = teamCode,
            uid = uid,
            payload = payload,
            lastStateWriteAtByUid = lastStateWriteAtByUid,
            minStateWriteIntervalMs = MIN_STATE_WRITE_INTERVAL_MS,
            lastStateCellByUid = lastStateCellByUid,
            stateCellCachePruneInProgress = stateCellCachePruneInProgress,
            stateCellPrecision = STATE_CELL_PRECISION,
        )

    override suspend fun upsertStateCell(
        teamCode: String,
        uid: String,
        cellId: String,
        payload: TeamStatePayload,
    ): TeamActionResult<Unit> = upsertStateCellWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        cellId = cellId,
        payload = payload,
    )

    override fun observeTeamCells(
        teamCode: String,
        cellIds: Set<String>,
    ): Flow<List<PlayerState>> = observeTeamCellsFlow(
        backendClient = backendClient,
        teamCode = teamCode,
        cellIds = cellIds,
        snapshotDebounceMs = SNAPSHOT_DEBOUNCE_MS,
    )

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> = addPointWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        payload = payload,
        forTeam = forTeam,
    )

    override suspend fun updatePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        payload: TeamPointUpdatePayload,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = updatePointWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        pointId = pointId,
        payload = payload,
        isTeam = isTeam,
    )

    override suspend fun deletePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = deletePointWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        pointId = pointId,
        isTeam = isTeam,
    )

    override suspend fun setActiveCommand(teamCode: String, uid: String, type: String): TeamActionResult<Unit> =
        setActiveCommandWrite(
            backendClient = backendClient,
            teamCode = teamCode,
            uid = uid,
            type = type,
        )

    override suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long,
    ): TeamActionResult<Unit> = addEnemyPingWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        lat = lat,
        lon = lon,
        type = type,
        ttlMs = ttlMs,
        lastEnemyPingAtByUid = lastEnemyPingAtByUid,
        minEnemyPingIntervalMs = MIN_ENEMY_PING_INTERVAL_MS,
        maxEnemyPingTtlMs = MAX_ENEMY_PING_TTL_MS,
    )

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> =
        observeMemberPrefsFlow(
            backendClient = backendClient,
            teamCode = teamCode,
            uid = uid,
        )

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = upsertMemberPrefsWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        uid = uid,
        prefs = prefs,
    )

    override fun observeTeamRoleProfiles(teamCode: String): Flow<List<TeamMemberRoleProfile>> =
        observeTeamRoleProfilesFlow(
            backendClient = backendClient,
            teamCode = teamCode,
        )

    override suspend fun assignTeamMemberRole(
        teamCode: String,
        actorUid: String,
        targetUid: String,
        patch: TeamRolePatch,
    ): TeamActionResult<TeamMemberRoleProfile> = assignTeamMemberRoleWrite(
        backendClient = backendClient,
        teamCode = teamCode,
        actorUid = actorUid,
        targetUid = targetUid,
        patch = patch,
    )

    private companion object {
        internal const val TAG = "FirebaseTeamRepo"
    }
}

