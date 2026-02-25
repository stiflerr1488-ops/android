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

internal fun statePayloadToMap(
    payload: TeamStatePayload,
    cellId: String?,
): Map<String, Any?> {
    val out = linkedMapOf<String, Any?>(
        "callsign" to payload.callsign,
        "lat" to payload.lat,
        "lon" to payload.lon,
        "acc" to payload.acc,
        "speed" to payload.speed,
        "heading" to payload.heading,
        "ts" to payload.ts,
        "mode" to payload.mode,
        "anchored" to payload.anchored,
        "sosUntilMs" to payload.sosUntilMs,
    )
    if (!cellId.isNullOrBlank()) {
        out["cellId"] = cellId
    }
    return out
}

internal fun roleProfileToMap(profile: TeamMemberRoleProfile): Map<String, Any> {
    val normalized = profile.orgPath.normalized()
    val orgMap = mutableMapOf<String, Any>(
        "sideId" to normalized.sideId,
    )
    normalized.companyId?.let { orgMap["companyId"] = it }
    normalized.platoonId?.let { orgMap["platoonId"] = it }
    normalized.teamId?.let { orgMap["teamId"] = it }
    normalized.vehicleId?.let { orgMap["vehicleId"] = it }

    val out = mutableMapOf<String, Any>(
        "commandRole" to profile.commandRole.name,
        "combatRole" to profile.combatRole.name,
        "vehicleRole" to profile.vehicleRole.name,
        "orgPath" to orgMap,
        "sideId" to normalized.sideId,
        "updatedAtMs" to profile.updatedAtMs,
        "schemaV" to 1,
    )
    normalized.companyId?.let { out["companyId"] = it }
    normalized.platoonId?.let { out["platoonId"] = it }
    normalized.teamId?.let { out["teamId"] = it }
    normalized.vehicleId?.let { out["vehicleId"] = it }
    profile.callsign?.let { if (it.isNotBlank()) out["callsign"] = it }
    profile.updatedBy?.let { if (it.isNotBlank()) out["updatedBy"] = it }
    return out
}

internal fun validatedTeamCodeOrNull(raw: String): String? = TeamCodeValidator.normalize(raw)

internal fun generateCode(): String = Random.nextInt(0, 1_000_000).toString().padStart(TEAM_CODE_LENGTH, '0')

internal fun invalidTeamCodeFailure(): TeamActionFailure =
    TeamActionFailure(TeamActionError.INVALID_INPUT, "Invalid team code")

