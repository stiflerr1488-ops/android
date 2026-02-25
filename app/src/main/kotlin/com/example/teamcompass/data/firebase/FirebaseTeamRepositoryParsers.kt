package com.example.teamcompass.data.firebase

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.TeamPoint
import com.example.teamcompass.domain.VehicleRole
import com.google.firebase.database.DataSnapshot

internal fun parsePlayer(snapshot: DataSnapshot, selfUid: String): PlayerState? {
    val id = snapshot.key ?: return null
    if (id == selfUid) return null
    val callsign = snapshot.child("callsign").stringOrNull() ?: "?"
    val lat = snapshot.child("lat").doubleOrNull() ?: return null
    val lon = snapshot.child("lon").doubleOrNull() ?: return null
    val acc = snapshot.child("acc").doubleOrNull() ?: 999.0
    val speed = snapshot.child("speed").doubleOrNull() ?: 0.0
    val heading = snapshot.child("heading").doubleOrNull()
    val ts = snapshot.child("ts").longOrZero()
    val mode = if (snapshot.child("mode").stringOrNull().equals("DEAD", ignoreCase = true)) {
        PlayerMode.DEAD
    } else {
        PlayerMode.GAME
    }
    val anchored = snapshot.child("anchored").boolOrFalse()
    val sosUntilMs = snapshot.child("sosUntilMs").longOrZero()
    return PlayerState(
        uid = id,
        nick = callsign,
        point = LocationPoint(lat, lon, acc, speed, heading, ts),
        mode = mode,
        role = Role.FIGHTER,
        anchored = anchored,
        sosUntilMs = sosUntilMs,
    )
}

internal fun parseRoleProfile(snapshot: DataSnapshot): TeamMemberRoleProfile? {
    val uid = snapshot.key ?: return null
    if (!snapshot.exists()) return null
    val orgNode = snapshot.child("orgPath")
    val orgPath = TeamOrgPath(
        sideId = orgNode.child("sideId").stringOrNull()
            ?: snapshot.child("sideId").stringOrNull()
            ?: DEFAULT_SIDE_ID,
        companyId = orgNode.child("companyId").stringOrNull()
            ?: snapshot.child("companyId").stringOrNull(),
        platoonId = orgNode.child("platoonId").stringOrNull()
            ?: snapshot.child("platoonId").stringOrNull(),
        teamId = orgNode.child("teamId").stringOrNull()
            ?: snapshot.child("teamId").stringOrNull(),
        vehicleId = orgNode.child("vehicleId").stringOrNull()
            ?: snapshot.child("vehicleId").stringOrNull(),
    ).normalized()
    return TeamMemberRoleProfile(
        uid = uid,
        callsign = snapshot.child("callsign").stringOrNull(),
        commandRole = parseTeamCommandRole(snapshot.child("commandRole").stringOrNull()),
        combatRole = parseCombatRole(snapshot.child("combatRole").stringOrNull()),
        vehicleRole = parseVehicleRole(snapshot.child("vehicleRole").stringOrNull()),
        orgPath = orgPath,
        updatedAtMs = snapshot.child("updatedAtMs").longOrZero(),
        updatedBy = snapshot.child("updatedBy").stringOrNull(),
    )
}

internal fun parsePoint(snapshot: DataSnapshot, isTeam: Boolean): TeamPoint? {
    val id = snapshot.key ?: return null
    val lat = snapshot.child("lat").doubleOrNull() ?: return null
    val lon = snapshot.child("lon").doubleOrNull() ?: return null
    val label = snapshot.child("label").stringOrNull().orEmpty()
    val icon = snapshot.child("icon").stringOrNull().orEmpty()
    normalizeMarkerKindRaw(snapshot.child("kind").stringOrNull(), defaultKind = MARKER_KIND_POINT)
    normalizeMarkerScopeRaw(
        snapshot.child("scope").stringOrNull(),
        defaultScope = if (isTeam) MARKER_SCOPE_TEAM else MARKER_SCOPE_PRIVATE,
    )
    normalizeMarkerStateRaw(snapshot.child("state").stringOrNull(), defaultState = MARKER_STATE_ACTIVE)
    val createdAtMs = snapshot.child("createdAtMs").longOrZero()
    val createdBy = snapshot.child("createdBy").stringOrNull()
    return TeamPoint(
        id = id,
        lat = lat,
        lon = lon,
        label = label,
        icon = icon,
        createdAtMs = createdAtMs,
        createdBy = createdBy,
        isTeam = isTeam,
    )
}

internal fun parseEnemyPing(snapshot: DataSnapshot): TeamEnemyPing? {
    val id = snapshot.key ?: return null
    val lat = snapshot.child("lat").doubleOrNull() ?: return null
    val lon = snapshot.child("lon").doubleOrNull() ?: return null
    val createdAt = snapshot.child("createdAtMs").longOrZero()
    val createdBy = snapshot.child("createdBy").stringOrNull()
    normalizeMarkerKindRaw(snapshot.child("kind").stringOrNull(), defaultKind = MARKER_KIND_ENEMY_PING)
    normalizeMarkerScopeRaw(snapshot.child("scope").stringOrNull(), defaultScope = MARKER_SCOPE_TEAM_EVENT)
    val stateRaw = normalizeMarkerStateRaw(snapshot.child("state").stringOrNull(), defaultState = MARKER_STATE_ACTIVE)
    if (stateRaw == MARKER_STATE_DISABLED) return null
    val type = normalizeEnemyPingType(
        snapshot.child("typeV2").stringOrNull()
            ?: snapshot.child("type").stringOrNull(),
    )
    val rawExpiresAt = snapshot.child("expiresAtMs").longOrZero().takeIf { it > 0L } ?: (createdAt + 120_000L)
    val expiresAt = if (stateRaw == MARKER_STATE_EXPIRED) {
        createdAt
    } else {
        rawExpiresAt
    }
    return TeamEnemyPing(
        id = id,
        lat = lat,
        lon = lon,
        createdAtMs = createdAt,
        createdBy = createdBy,
        expiresAtMs = expiresAt,
        type = type,
    )
}

internal fun parseActiveCommand(snapshot: DataSnapshot): TeamActiveCommand? {
    if (!snapshot.exists()) return null
    val type = snapshot.child("type").stringOrNull() ?: return null
    val id = snapshot.child("id").stringOrNull() ?: snapshot.key ?: "active"
    val createdAt = snapshot.child("createdAtMs").longOrZero()
    val createdBy = snapshot.child("createdBy").stringOrNull()
    return TeamActiveCommand(
        id = id,
        type = type,
        createdAtMs = createdAt,
        createdBy = createdBy,
    )
}

internal fun parseMemberPrefs(snapshot: DataSnapshot): TeamMemberPrefs? {
    if (!snapshot.exists()) return null
    val preset = snapshot.child("preset").stringOrNull() ?: return null
    val nearRadiusM = snapshot.child("nearRadiusM").longOrZero().toInt().coerceIn(50, 500)
    val showDead = snapshot.child("showDead").getValue(Boolean::class.java) ?: true
    val showStale = snapshot.child("showStale").getValue(Boolean::class.java) ?: true
    val focusMode = snapshot.child("focusMode").boolOrFalse()
    val updatedAtMs = snapshot.child("updatedAtMs").longOrZero()
    return TeamMemberPrefs(
        preset = preset,
        nearRadiusM = nearRadiusM,
        showDead = showDead,
        showStale = showStale,
        focusMode = focusMode,
        updatedAtMs = updatedAtMs,
    )
}

private fun parseTeamCommandRole(raw: String?): TeamCommandRole {
    return when (raw?.trim()?.uppercase()) {
        TeamCommandRole.SIDE_COMMANDER.name,
        "COMMANDER",
        -> TeamCommandRole.SIDE_COMMANDER
        TeamCommandRole.COMPANY_COMMANDER.name -> TeamCommandRole.COMPANY_COMMANDER
        TeamCommandRole.PLATOON_COMMANDER.name -> TeamCommandRole.PLATOON_COMMANDER
        TeamCommandRole.TEAM_COMMANDER.name -> TeamCommandRole.TEAM_COMMANDER
        TeamCommandRole.TEAM_DEPUTY.name,
        "DEPUTY",
        -> TeamCommandRole.TEAM_DEPUTY
        else -> TeamCommandRole.FIGHTER
    }
}

private fun parseCombatRole(raw: String?): CombatRole {
    return when (raw?.trim()?.uppercase()) {
        CombatRole.ASSAULTER.name, "ASSAULT", "STORM", "STORMER" -> CombatRole.ASSAULTER
        CombatRole.SCOUT.name, "RECON", "RECONNAISSANCE" -> CombatRole.SCOUT
        CombatRole.SNIPER.name -> CombatRole.SNIPER
        CombatRole.MORTAR.name, "MORTARMAN" -> CombatRole.MORTAR
        else -> CombatRole.NONE
    }
}

private fun parseVehicleRole(raw: String?): VehicleRole {
    return when (raw?.trim()?.uppercase()) {
        VehicleRole.DRIVER.name -> VehicleRole.DRIVER
        VehicleRole.ASSISTANT_DRIVER.name, "ASSIST_DRIVER", "CO_DRIVER" -> VehicleRole.ASSISTANT_DRIVER
        VehicleRole.PASSENGER.name -> VehicleRole.PASSENGER
        else -> VehicleRole.NONE
    }
}

internal fun DataSnapshot.stringOrNull(): String? = getValue(String::class.java)

internal fun DataSnapshot.doubleOrNull(): Double? {
    val n = value as? Number ?: return null
    return n.toDouble()
}

internal fun DataSnapshot.longOrZero(): Long {
    val n = value as? Number ?: return 0L
    return n.toLong()
}

internal fun DataSnapshot.boolOrFalse(): Boolean = getValue(Boolean::class.java) ?: false
