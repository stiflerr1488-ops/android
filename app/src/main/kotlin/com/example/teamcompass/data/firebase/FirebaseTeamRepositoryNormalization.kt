package com.example.teamcompass.data.firebase

import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure

internal fun normalizeEnemyPingType(raw: String?): String {
    return when (raw?.trim()?.uppercase()) {
        "ENEMY", "ENEMY_CONTACT", "CONTACT" -> "ENEMY"
        "ATTACK" -> "ATTACK"
        "DEFENSE", "DEFEND", "RETREAT", "RALLY" -> "DEFENSE"
        "BLUETOOTH", "BT" -> "BLUETOOTH"
        else -> "DANGER"
    }
}

internal fun normalizeMarkerKindRaw(raw: String?, defaultKind: String): String {
    return when (raw?.trim()?.uppercase()) {
        MARKER_KIND_POINT -> MARKER_KIND_POINT
        MARKER_KIND_ENEMY_PING -> MARKER_KIND_ENEMY_PING
        else -> defaultKind
    }
}

internal fun normalizeMarkerScopeRaw(raw: String?, defaultScope: String): String {
    return when (raw?.trim()?.uppercase()) {
        MARKER_SCOPE_TEAM -> MARKER_SCOPE_TEAM
        MARKER_SCOPE_PRIVATE -> MARKER_SCOPE_PRIVATE
        MARKER_SCOPE_TEAM_EVENT -> MARKER_SCOPE_TEAM_EVENT
        else -> defaultScope
    }
}

internal fun normalizeMarkerStateRaw(raw: String?, defaultState: String): String {
    return when (raw?.trim()?.uppercase()) {
        MARKER_STATE_ACTIVE -> MARKER_STATE_ACTIVE
        MARKER_STATE_EXPIRED -> MARKER_STATE_EXPIRED
        MARKER_STATE_DISABLED -> MARKER_STATE_DISABLED
        else -> defaultState
    }
}

internal fun Throwable?.toFailure(): TeamActionFailure {
    if (this == null) return TeamActionFailure(TeamActionError.UNKNOWN)
    val msg = message.orEmpty()
    val normalized = msg.lowercase()
    return when {
        normalized.contains("permission denied") -> TeamActionFailure(TeamActionError.PERMISSION_DENIED, msg, this)
        normalized.contains("network") || normalized.contains("unavailable") || normalized.contains("timeout") ->
            TeamActionFailure(TeamActionError.NETWORK, msg, this)

        else -> TeamActionFailure(TeamActionError.UNKNOWN, msg, this)
    }
}
