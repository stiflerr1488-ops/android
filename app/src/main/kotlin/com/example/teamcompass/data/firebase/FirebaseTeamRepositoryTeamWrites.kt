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

internal suspend fun createTeamWrite(
    backendClient: RealtimeBackendClient,
    ownerUid: String,
    ownerCallsign: String,
    nowMs: Long,
    maxAttempts: Int,
): TeamActionResult<String> {
    val attempts = maxAttempts.coerceIn(1, 20)
    repeat(attempts) {
        val code = generateCode()
        val teamPath = "teams/$code"
        val joinSalt = TeamCodeSecurity.generateSaltHex()
        val joinHash = TeamCodeSecurity.hashJoinCode(code, joinSalt)
        val payload = mapOf(
            "meta" to mapOf(
                "createdAtMs" to nowMs,
                "createdBy" to ownerUid,
                "isLocked" to false,
                "expiresAtMs" to (nowMs + 12 * 60 * 60_000L),
                "joinSalt" to joinSalt,
                "joinHash" to joinHash,
            ),
            "members" to mapOf(
                ownerUid to mapOf(
                    "callsign" to ownerCallsign,
                    "joinedAtMs" to nowMs,
                )
            ),
            "memberRoles" to mapOf(
                ownerUid to roleProfileToMap(
                    TeamMemberRoleProfile(
                        uid = ownerUid,
                        callsign = ownerCallsign,
                        commandRole = TeamCommandRole.SIDE_COMMANDER,
                        combatRole = CombatRole.NONE,
                        vehicleRole = VehicleRole.NONE,
                        orgPath = TeamOrgPath(sideId = DEFAULT_SIDE_ID),
                        updatedAtMs = nowMs,
                        updatedBy = ownerUid,
                    )
                )
            )
        )

        val writeResult = runCatching {
            backendClient.setValue(path = teamPath, value = payload)
        }
        if (writeResult.isSuccess) {
            return TeamActionResult.Success(code)
        }

        val failure = writeResult.exceptionOrNull().toFailure()
        when (failure.error) {
            TeamActionError.PERMISSION_DENIED -> {
                val collision = runCatching {
                    backendClient.get(path = "$teamPath/meta").exists()
                }.getOrDefault(false)
                if (collision) {
                    return@repeat
                }
                return TeamActionResult.Failure(failure)
            }
            TeamActionError.NETWORK -> return TeamActionResult.Failure(failure)
            else -> return TeamActionResult.Failure(failure)
        }
    }
    return TeamActionResult.Failure(
        TeamActionFailure(
            error = TeamActionError.COLLISION,
            message = "Failed to create unique team code",
        )
    )
}

internal suspend fun joinTeamWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
    callsign: String,
    nowMs: Long,
    legacyJoinGraceMs: Long,
    logTag: String,
): TeamActionResult<Unit> {
    val code = validatedTeamCodeOrNull(teamCode)
    if (code == null) {
        return TeamActionResult.Failure(
            TeamActionFailure(TeamActionError.INVALID_INPUT, "Invalid team code")
        )
    }
    val teamPath = "teams/$code"
    val metaResult = runCatching { backendClient.get(path = "$teamPath/meta") }
    val metaSnap = metaResult.getOrElse { return TeamActionResult.Failure(it.toFailure()) }
    if (!metaSnap.exists()) {
        return TeamActionResult.Failure(TeamActionFailure(TeamActionError.NOT_FOUND, "Team not found"))
    }

    val memberPath = "$teamPath/members/$uid"
    val memberResult = runCatching { backendClient.get(path = memberPath) }
    val memberSnap = memberResult.getOrElse { return TeamActionResult.Failure(it.toFailure()) }
    val isExistingMember = memberSnap.exists()

    val joinSalt = metaSnap.child("joinSalt").stringOrNull()
    val joinHash = metaSnap.child("joinHash").stringOrNull()
    val hasJoinSecrets = !joinSalt.isNullOrBlank() && !joinHash.isNullOrBlank()
    val createdAtMs = metaSnap.child("createdAtMs").longOrZero()
    val allowLegacyJoinWithoutSecrets = isExistingMember || (
        createdAtMs > 0L && nowMs <= createdAtMs + legacyJoinGraceMs
    )
    val joinCodeVerified = when {
        hasJoinSecrets -> {
            val salt = joinSalt ?: return TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.NOT_FOUND, "Team not found"),
            )
            val hash = joinHash ?: return TeamActionResult.Failure(
                TeamActionFailure(TeamActionError.NOT_FOUND, "Team not found"),
            )
            TeamCodeSecurity.verifyJoinCode(code, salt, hash)
        }
        allowLegacyJoinWithoutSecrets -> true
        else -> false
    }
    if (!joinCodeVerified) {
        return TeamActionResult.Failure(TeamActionFailure(TeamActionError.NOT_FOUND, "Team not found"))
    }
    if (!hasJoinSecrets) {
        migrateLegacyJoinSecrets(
            backendClient = backendClient,
            teamPath = teamPath,
            code = code,
            logTag = logTag,
        )
    }

    if (!isExistingMember) {
        val isLocked = metaSnap.child("isLocked").boolOrFalse()
        if (isLocked) {
            return TeamActionResult.Failure(TeamActionFailure(TeamActionError.LOCKED, "Match is locked"))
        }
        val expiresAtMs = metaSnap.child("expiresAtMs").longOrZero()
        if (expiresAtMs > 0L && nowMs > expiresAtMs) {
            return TeamActionResult.Failure(TeamActionFailure(TeamActionError.EXPIRED, "Match expired"))
        }
    }

    val joinedAtMs = memberSnap.child("joinedAtMs").longOrZero().takeIf { it > 0L } ?: nowMs
    val member = mapOf(
        "callsign" to callsign,
        "joinedAtMs" to joinedAtMs,
    )
    val existingRole = if (isExistingMember) {
        val roleSnapshot = runCatching {
            backendClient.get(path = "$teamPath/memberRoles/$uid")
        }.getOrElse { return TeamActionResult.Failure(it.toFailure()) }
        roleSnapshot.let(::parseRoleProfile)
    } else {
        null
    }
    val roleProfile = if (existingRole != null) {
        existingRole.copy(
            callsign = callsign,
            updatedAtMs = nowMs,
            updatedBy = uid,
        )
    } else {
        TeamMemberRoleProfile(
            uid = uid,
            callsign = callsign,
            commandRole = TeamCommandRole.FIGHTER,
            combatRole = CombatRole.NONE,
            vehicleRole = VehicleRole.NONE,
            orgPath = TeamOrgPath(sideId = DEFAULT_SIDE_ID),
            updatedAtMs = nowMs,
            updatedBy = uid,
        )
    }
    val updates = linkedMapOf<String, Any>(
        "members/$uid" to member,
        "memberRoles/$uid" to roleProfileToMap(roleProfile),
    )
    return runCatching {
        backendClient.updateChildren(path = teamPath, updates = updates)
    }.fold(
        onSuccess = { TeamActionResult.Success(Unit) },
        onFailure = { TeamActionResult.Failure(it.toFailure()) },
    )
}

internal suspend fun assignTeamMemberRoleWrite(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    actorUid: String,
    targetUid: String,
    patch: TeamRolePatch,
): TeamActionResult<TeamMemberRoleProfile> {
    val code = validatedTeamCodeOrNull(teamCode)
        ?: return TeamActionResult.Failure(invalidTeamCodeFailure())
    if (actorUid.isBlank() || targetUid.isBlank()) {
        return TeamActionResult.Failure(
            TeamActionFailure(TeamActionError.INVALID_INPUT, "Actor and target ids are required")
        )
    }
    if (patch.commandRole == null && patch.combatRole == null && patch.vehicleRole == null && patch.orgPath == null) {
        return TeamActionResult.Failure(
            TeamActionFailure(TeamActionError.INVALID_INPUT, "Role patch cannot be empty")
        )
    }

    val teamPath = "teams/$code"
    val now = System.currentTimeMillis()
    val metaSnapshot = runCatching { backendClient.get(path = "$teamPath/meta") }.getOrElse {
        return TeamActionResult.Failure(it.toFailure())
    }
    if (!metaSnapshot.exists()) {
        return TeamActionResult.Failure(TeamActionFailure(TeamActionError.NOT_FOUND, "Team not found"))
    }
    val createdBy = metaSnapshot.child("createdBy").stringOrNull()

    val actorProfile = loadMemberRoleProfileForAssign(
        backendClient = backendClient,
        teamPath = teamPath,
        uid = actorUid,
        createdBy = createdBy,
        now = now,
    ) ?: return TeamActionResult.Failure(
        TeamActionFailure(TeamActionError.PERMISSION_DENIED, "Actor has no role access in this team")
    )

    val targetCurrent = loadMemberRoleProfileForAssign(
        backendClient = backendClient,
        teamPath = teamPath,
        uid = targetUid,
        createdBy = createdBy,
        now = now,
    ) ?: return TeamActionResult.Failure(
        TeamActionFailure(TeamActionError.NOT_FOUND, "Target member not found")
    )

    val nextProfile = targetCurrent.applyPatch(
        patch = patch,
        updatedAtMs = now,
        updatedBy = actorUid,
    )

    if (!canAssignRole(actor = actorProfile, nextProfile = nextProfile, patch = patch)) {
        return TeamActionResult.Failure(
            TeamActionFailure(
                TeamActionError.PERMISSION_DENIED,
                "Insufficient permissions to assign this role",
            )
        )
    }

    return runCatching {
        backendClient.setValue(
            path = "$teamPath/memberRoles/$targetUid",
            value = roleProfileToMap(nextProfile),
        )
        TeamActionResult.Success(nextProfile)
    }.getOrElse { TeamActionResult.Failure(it.toFailure()) }
}

internal suspend fun loadMemberRoleProfileForAssign(
    backendClient: RealtimeBackendClient,
    teamPath: String,
    uid: String,
    createdBy: String?,
    now: Long,
): TeamMemberRoleProfile? {
    val roleSnap = runCatching {
        backendClient.get(path = "$teamPath/memberRoles/$uid")
    }.getOrNull()
    parseRoleProfile(roleSnap ?: return null)?.let { return it }

    val memberSnap = runCatching {
        backendClient.get(path = "$teamPath/members/$uid")
    }.getOrNull() ?: return null
    if (!memberSnap.exists()) return null

    val callsign = memberSnap.child("callsign").stringOrNull()
    val defaultCommandRole = if (uid == createdBy) TeamCommandRole.SIDE_COMMANDER else TeamCommandRole.FIGHTER
    return TeamMemberRoleProfile(
        uid = uid,
        callsign = callsign,
        commandRole = defaultCommandRole,
        combatRole = CombatRole.NONE,
        vehicleRole = VehicleRole.NONE,
        orgPath = TeamOrgPath(sideId = DEFAULT_SIDE_ID),
        updatedAtMs = memberSnap.child("joinedAtMs").longOrZero().takeIf { it > 0L } ?: now,
        updatedBy = uid,
    )
}

