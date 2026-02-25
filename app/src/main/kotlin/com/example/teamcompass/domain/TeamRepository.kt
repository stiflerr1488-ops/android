package com.example.teamcompass.domain

import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.LocationPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

enum class TeamActionError {
    NETWORK,
    PERMISSION_DENIED,
    NOT_FOUND,
    LOCKED,
    EXPIRED,
    COLLISION,
    INVALID_INPUT,
    UNKNOWN,
}

data class TeamActionFailure(
    val error: TeamActionError,
    val message: String? = null,
    val cause: Throwable? = null,
)

sealed interface TeamActionResult<out T> {
    data class Success<T>(val value: T) : TeamActionResult<T>
    data class Failure(val details: TeamActionFailure) : TeamActionResult<Nothing>
}

data class TeamPoint(
    val id: String,
    val lat: Double,
    val lon: Double,
    val label: String,
    val icon: String,
    val createdAtMs: Long,
    val createdBy: String? = null,
    val isTeam: Boolean,
)

data class TeamEnemyPing(
    val id: String,
    val lat: Double,
    val lon: Double,
    val createdAtMs: Long,
    val createdBy: String? = null,
    val expiresAtMs: Long,
    val type: String = "DANGER",
)

data class TeamActiveCommand(
    val id: String,
    val type: String,
    val createdAtMs: Long,
    val createdBy: String? = null,
)

data class TeamMemberPrefs(
    val preset: String,
    val nearRadiusM: Int,
    val showDead: Boolean,
    val showStale: Boolean,
    val focusMode: Boolean,
    val updatedAtMs: Long,
)

data class TeamSnapshot(
    val players: List<PlayerState> = emptyList(),
    val teamPoints: List<TeamPoint> = emptyList(),
    val privatePoints: List<TeamPoint> = emptyList(),
    val enemyPings: List<TeamEnemyPing> = emptyList(),
    val roleProfiles: List<TeamMemberRoleProfile> = emptyList(),
    val activeCommand: TeamActiveCommand? = null,
)

data class TeamStatePayload(
    val callsign: String,
    val lat: Double,
    val lon: Double,
    val acc: Double,
    val speed: Double,
    val heading: Double?,
    val ts: Long,
    val mode: String,
    val anchored: Boolean,
    val sosUntilMs: Long,
)

enum class TeamViewMode {
    COMBAT,
    COMMAND,
}

data class TeamPointPayload(
    val lat: Double,
    val lon: Double,
    val label: String,
    val icon: String,
)

data class TeamPointUpdatePayload(
    val lat: Double,
    val lon: Double,
    val label: String,
    val icon: String,
)

interface TeamRepository {
    suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long = System.currentTimeMillis(),
        maxAttempts: Int = 5,
    ): TeamActionResult<String>

    suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long = System.currentTimeMillis(),
    ): TeamActionResult<Unit>

    fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode = TeamViewMode.COMBAT,
        selfPoint: LocationPoint? = null,
    ): Flow<TeamSnapshot>

    fun observeBackendHealth(): Flow<Boolean> = flowOf(true)

    suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit>

    suspend fun upsertStateCell(
        teamCode: String,
        uid: String,
        cellId: String,
        payload: TeamStatePayload,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    fun observeTeamCells(
        teamCode: String,
        cellIds: Set<String>,
    ): Flow<List<PlayerState>> = flowOf(emptyList())

    suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String>

    suspend fun updatePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        payload: TeamPointUpdatePayload,
        isTeam: Boolean,
    ): TeamActionResult<Unit>

    suspend fun deletePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        isTeam: Boolean,
    ): TeamActionResult<Unit>

    suspend fun setActiveCommand(teamCode: String, uid: String, type: String): TeamActionResult<Unit>

    suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long = 120_000L,
    ): TeamActionResult<Unit>

    fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?>

    suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit>

    fun observeTeamRoleProfiles(teamCode: String): Flow<List<TeamMemberRoleProfile>> = flowOf(emptyList())

    suspend fun assignTeamMemberRole(
        teamCode: String,
        actorUid: String,
        targetUid: String,
        patch: TeamRolePatch,
    ): TeamActionResult<TeamMemberRoleProfile> = TeamActionResult.Failure(
        TeamActionFailure(
            error = TeamActionError.INVALID_INPUT,
            message = "Role assignment is not supported by this repository implementation",
        )
    )
}
