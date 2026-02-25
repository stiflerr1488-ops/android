package com.example.teamcompass.data.firebase

import com.example.teamcompass.BuildConfig
import com.example.teamcompass.core.GeoCell
import com.example.teamcompass.core.TeamCodeValidator
import com.example.teamcompass.core.TeamCodeSecurity
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRolePatch
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.domain.applyPatch
import com.example.teamcompass.domain.canAssignRole
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

internal suspend fun setActiveCommandWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    type: String,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val now = System.currentTimeMillis()
    val data = mapOf(
        "id" to UUID.randomUUID().toString(),
        "type" to type,
        "createdAtMs" to now,
        "createdBy" to uid,
    )
    return runCatching {
        backendClient.setValue(path = "teams/$code/commands/active", value = data)
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun upsertMemberPrefsWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    prefs: TeamMemberPrefs,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val payload = mapOf(
        "preset" to prefs.preset,
        "nearRadiusM" to prefs.nearRadiusM.coerceIn(50, 500),
        "showDead" to prefs.showDead,
        "showStale" to prefs.showStale,
        "focusMode" to prefs.focusMode,
        "updatedAtMs" to (prefs.updatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()),
    )
    return runCatching {
        backendClient.setValue(path = "teams/$code/memberPrefs/$uid", value = payload)
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun upsertStateCellWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    cellId: String,
    payload: TeamStatePayload,
): TeamActionResult<Unit> {
    if (!BuildConfig.STATE_CELLS_V1_ENABLED) {
        return TeamActionResult.Success(Unit)
    }
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val normalizedCellId = cellId.trim().lowercase()
    if (normalizedCellId.isBlank()) {
        return TeamActionResult.Failure(
            TeamActionFailure(TeamActionError.INVALID_INPUT, "Invalid state cell id")
        )
    }
    val data = statePayloadToMap(payload = payload, cellId = normalizedCellId)
    return runCatching {
        backendClient.setValue(
            path = "teams/$code/stateCells/$normalizedCellId/$uid",
            value = data,
        )
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun upsertStateWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    payload: TeamStatePayload,
    lastStateWriteAtByUid: MutableMap<String, Long>,
    minStateWriteIntervalMs: Long,
    lastStateCellByUid: MutableMap<String, StateCellEntry>,
    stateCellCachePruneInProgress: AtomicBoolean,
    stateCellPrecision: Int,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val now = System.currentTimeMillis()
    if (!acquireClientRatePermit(
            lastWriteAtByUid = lastStateWriteAtByUid,
            uid = uid,
            nowMs = now,
            minIntervalMs = minStateWriteIntervalMs,
        )
    ) {
        return TeamActionResult.Success(Unit)
    }
    if (!BuildConfig.STATE_CELLS_V1_ENABLED) {
        return runCatching {
            backendClient.setValue(
                path = "teams/$code/state/$uid",
                value = statePayloadToMap(payload = payload, cellId = null),
            )
            lastStateCellByUid.remove(uid)
        }.fold(
            onSuccess = { TeamActionResult.Success(Unit) },
            onFailure = { TeamActionResult.Failure(it.toFailure()) },
        )
    }
    val cellId = GeoCell.encode(payload.lat, payload.lon, stateCellPrecision)
    val data = statePayloadToMap(payload = payload, cellId = cellId)
    val previousCellId = lastStateCellByUid[uid]?.cellId
    val updates = linkedMapOf<String, Any?>(
        "state/$uid" to data,
        "stateCells/$cellId/$uid" to data,
    )
    if (!previousCellId.isNullOrBlank() && previousCellId != cellId) {
        updates["stateCells/$previousCellId/$uid"] = null
    }
    return runCatching {
        backendClient.updateChildren(path = "teams/$code", updates = updates)
        lastStateCellByUid[uid] = StateCellEntry(
            cellId = cellId,
            lastUpdatedMs = now,
        )
        pruneStateCellCacheIfNeeded(
            cache = lastStateCellByUid,
            pruneInProgress = stateCellCachePruneInProgress,
        )
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun addPointWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    payload: TeamPointPayload,
    forTeam: Boolean,
): TeamActionResult<String> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val pointPath = if (forTeam) {
        "teams/$code/points"
    } else {
        "teams/$code/privatePoints/$uid"
    }
    val pointRef = backendClient.push(path = pointPath)
    val pointId = pointRef.key ?: UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    val scope = if (forTeam) MARKER_SCOPE_TEAM else MARKER_SCOPE_PRIVATE
    val data = mapOf(
        "lat" to payload.lat,
        "lon" to payload.lon,
        "label" to payload.label,
        "icon" to payload.icon,
        "createdAtMs" to now,
        "updatedAtMs" to now,
        "createdBy" to uid,
        "state" to MARKER_STATE_ACTIVE,
        "scope" to scope,
        "kind" to MARKER_KIND_POINT,
    )
    return runCatching {
        backendClient.setValue(path = "$pointPath/$pointId", value = data)
    }.fold(
        onSuccess = { TeamActionResult.Success(pointId) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun updatePointWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    pointId: String,
    payload: TeamPointUpdatePayload,
    isTeam: Boolean,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val pointPath = if (isTeam) {
        "teams/$code/points/$pointId"
    } else {
        "teams/$code/privatePoints/$uid/$pointId"
    }
    val now = System.currentTimeMillis()
    val data = mapOf(
        "lat" to payload.lat,
        "lon" to payload.lon,
        "label" to payload.label,
        "icon" to payload.icon,
        "updatedAtMs" to now,
        "state" to MARKER_STATE_ACTIVE,
        "scope" to if (isTeam) MARKER_SCOPE_TEAM else MARKER_SCOPE_PRIVATE,
        "kind" to MARKER_KIND_POINT,
    )
    return runCatching { backendClient.updateChildren(path = pointPath, updates = data) }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun deletePointWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    pointId: String,
    isTeam: Boolean,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val pointPath = if (isTeam) {
        "teams/$code/points/$pointId"
    } else {
        "teams/$code/privatePoints/$uid/$pointId"
    }
    return runCatching { backendClient.removeValue(path = pointPath) }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun addEnemyPingWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    lat: Double,
    lon: Double,
    type: String,
    ttlMs: Long,
    lastEnemyPingAtByUid: MutableMap<String, Long>,
    minEnemyPingIntervalMs: Long,
    maxEnemyPingTtlMs: Long,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    val now = System.currentTimeMillis()
    if (!acquireClientRatePermit(
            lastWriteAtByUid = lastEnemyPingAtByUid,
            uid = uid,
            nowMs = now,
            minIntervalMs = minEnemyPingIntervalMs,
        )
    ) {
        return TeamActionResult.Failure(
            TeamActionFailure(
                error = TeamActionError.PERMISSION_DENIED,
                message = "throttled_enemy_ping",
            )
        )
    }
    val pingPath = "teams/$code/enemyPings"
    val pingRef = backendClient.push(path = pingPath)
    val pingId = pingRef.key ?: UUID.randomUUID().toString()
    val normalizedType = normalizeEnemyPingType(type)
    val sanitizedTtlMs = ttlMs.coerceIn(1_000L, maxEnemyPingTtlMs)
    val rulesCompatibleType = when (normalizedType) {
        "ENEMY", "BLUETOOTH" -> "DANGER"
        else -> normalizedType
    }
    val data = mapOf(
        "lat" to lat,
        "lon" to lon,
        "createdAtMs" to now,
        "updatedAtMs" to now,
        "createdBy" to uid,
        "type" to rulesCompatibleType,
        "typeV2" to normalizedType,
        "expiresAtMs" to (now + sanitizedTtlMs),
        "state" to MARKER_STATE_ACTIVE,
        "scope" to MARKER_SCOPE_TEAM_EVENT,
        "kind" to MARKER_KIND_ENEMY_PING,
    )
    runCatching {
        pingRef.onDisconnect().removeValue()
    }
    val updates = mapOf(
        "enemyPings/$pingId" to data,
        "rateLimits/enemyPing/$uid/lastAtMs" to now,
    )
    return runCatching {
        backendClient.updateChildren(path = "teams/$code", updates = updates)
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}
