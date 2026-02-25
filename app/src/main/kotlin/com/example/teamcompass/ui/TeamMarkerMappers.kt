package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamEnemyPing
import com.example.teamcompass.domain.TeamPoint

internal fun TeamPoint.toUiMapPoint(): MapPoint {
    return MapPoint(
        id = id,
        lat = lat,
        lon = lon,
        label = label,
        icon = icon.ifBlank {
            if (isTeam) TacticalIconId.FLAG.raw else TacticalIconId.OBSERVE.raw
        },
        createdAtMs = createdAtMs,
        createdBy = createdBy,
        isTeam = isTeam,
    )
}

internal fun TeamEnemyPing.toUiEnemyPing(): EnemyPing {
    val isBluetooth = type.equals("BLUETOOTH", ignoreCase = true)
    return EnemyPing(
        id = id,
        lat = lat,
        lon = lon,
        createdAtMs = createdAtMs,
        createdBy = createdBy,
        expiresAtMs = expiresAtMs,
        type = if (isBluetooth) QuickCommandType.DANGER else quickMarkerTypeFromRaw(type),
        isBluetooth = isBluetooth,
    )
}

internal fun TeamActiveCommand.toUiQuickCommand(): QuickCommand {
    return QuickCommand(
        id = id,
        type = quickMarkerTypeFromRaw(type),
        createdAtMs = createdAtMs,
        createdBy = createdBy,
    )
}

internal fun quickMarkerTypeFromRaw(raw: String?): QuickCommandType {
    return when (raw?.trim()?.uppercase()) {
        QuickCommandType.ENEMY.name, "ENEMY_CONTACT", "CONTACT" -> QuickCommandType.ENEMY
        QuickCommandType.ATTACK.name -> QuickCommandType.ATTACK
        QuickCommandType.DEFENSE.name, "DEFEND", "RETREAT", "RALLY" -> QuickCommandType.DEFENSE
        else -> QuickCommandType.DANGER
    }
}

internal fun buildUnifiedMarkersForView(
    teamPoints: List<MapPoint>,
    privatePoints: List<MapPoint>,
    enemyPings: List<EnemyPing>,
    viewerUid: String?,
): List<UnifiedMarker> {
    val now = System.currentTimeMillis()
    val list = ArrayList<UnifiedMarker>(teamPoints.size + privatePoints.size + enemyPings.size)
    teamPoints.mapTo(list) { it.toUnifiedMarker(nowMs = now) }
    privatePoints.mapTo(list) { it.toUnifiedMarker(nowMs = now) }
    enemyPings.mapTo(list) { it.toUnifiedMarker(nowMs = now) }
    return list.filter { MarkerCorePolicies.isVisible(it, viewerUid) }
}

internal fun MapPoint.toUnifiedMarker(nowMs: Long = System.currentTimeMillis()): UnifiedMarker {
    val scope = if (isTeam) MarkerScope.TEAM else MarkerScope.PRIVATE
    return UnifiedMarker(
        id = id,
        kind = MarkerKind.POINT,
        scope = scope,
        state = MarkerState.ACTIVE,
        ownerUid = createdBy,
        lat = lat,
        lon = lon,
        label = label,
        iconRaw = icon,
        colorArgb = null,
        createdAtMs = createdAtMs,
        updatedAtMs = createdAtMs.takeIf { it > 0L } ?: nowMs,
        expiresAtMs = 0L,
        source = if (isTeam) MarkerSource.TEAM_POINT else MarkerSource.PRIVATE_POINT,
    )
}

internal fun EnemyPing.toUnifiedMarker(nowMs: Long = System.currentTimeMillis()): UnifiedMarker {
    val initial = UnifiedMarker(
        id = id,
        kind = MarkerKind.ENEMY_PING,
        scope = MarkerScope.TEAM_EVENT,
        state = MarkerState.ACTIVE,
        ownerUid = createdBy,
        lat = lat,
        lon = lon,
        label = "",
        iconRaw = if (isBluetooth) "BT" else type.name,
        colorArgb = null,
        createdAtMs = createdAtMs,
        updatedAtMs = createdAtMs.takeIf { it > 0L } ?: nowMs,
        expiresAtMs = expiresAtMs,
        source = MarkerSource.ENEMY_PING,
    )
    return initial.copy(state = MarkerCorePolicies.resolveState(initial, nowMs))
}
