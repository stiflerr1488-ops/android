package com.example.teamcompass.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.ui.components.TeamRosterItemUi
import java.util.Locale

internal data class CompassScreenRosterComputed(
    val allyVisualsByUid: Map<String, AllyVisualDescriptor>,
    val teamRoster: List<TeamRosterItemUi>,
)

internal data class CompassScreenOverlayComputed(
    val bluetoothDevicesCount: Int,
    val enemyOverlays: List<RadarOverlay>,
    val pointMarkers: List<PointMarkerUi>,
)

@Composable
internal fun rememberCompassScreenRosterComputed(
    state: UiState,
    nowMs: Long,
): CompassScreenRosterComputed {
    val roleProfilesByUid = remember(state.roleProfiles) {
        state.roleProfiles.associateBy { it.uid }
    }
    val allyVisualsByUid = remember(state.players, state.roleProfiles, state.uid) {
        buildAllyVisualsByUid(
            players = state.players,
            roleProfiles = state.roleProfiles,
            selfUid = state.uid,
        )
    }
    val teamRosterBase = remember(state.players, state.me, state.uid, state.roleProfiles) {
        state.players
            .asSequence()
            .filter { it.uid != state.uid }
            .map { player ->
                val roleProfile = roleProfilesByUid[player.uid]
                val commandRole = roleProfile?.commandRole ?: when (player.role) {
                    Role.COMMANDER -> TeamCommandRole.TEAM_COMMANDER
                    Role.DEPUTY -> TeamCommandRole.TEAM_DEPUTY
                    else -> TeamCommandRole.FIGHTER
                }
                TeamRosterItemUi(
                    uid = player.uid,
                    callsign = player.nick,
                    distanceMeters = state.me?.let { me ->
                        com.example.teamcompass.core.GeoMath.distanceMeters(me, player.point)
                    },
                    isCommander = commandRole <= TeamCommandRole.TEAM_COMMANDER,
                    isDead = player.mode == com.example.teamcompass.core.PlayerMode.DEAD,
                    sosUntilMs = player.sosUntilMs,
                    commandRole = commandRole,
                    combatRole = roleProfile?.combatRole ?: CombatRole.NONE,
                    vehicleRole = roleProfile?.vehicleRole ?: VehicleRole.NONE,
                    orgPath = roleProfile?.orgPath ?: TeamOrgPath(),
                )
            }
            .sortedWith(
                compareBy<TeamRosterItemUi> { it.commandRole.rank }
                    .thenBy { it.distanceMeters ?: Double.MAX_VALUE }
                    .thenBy { it.callsign.lowercase(Locale.getDefault()) }
            )
            .toList()
    }
    val teamRoster = remember(teamRosterBase, nowMs) {
        teamRosterBase.map { item ->
            item.copy(sosActive = item.sosUntilMs > nowMs)
        }
    }
    return CompassScreenRosterComputed(
        allyVisualsByUid = allyVisualsByUid,
        teamRoster = teamRoster,
    )
}

@Composable
internal fun rememberCompassScreenOverlayComputed(
    state: UiState,
    localEnemyPings: List<LocalEnemyPingUi>,
    rangeMeters: Float,
    radarSize: IntSize,
    defaultPointLabel: String,
    nowMs: Long,
): CompassScreenOverlayComputed {
    val bluetoothDevicesCount = remember(state.bluetoothScanResult, nowMs) {
        state.bluetoothScanResult?.getActiveDevices(nowMs)?.size ?: 0
    }
    val enemyOverlays = remember(
        state.enemyPings,
        localEnemyPings,
        state.me,
        state.myHeadingDeg,
        nowMs,
    ) {
        buildCompassEnemyOverlays(
            remoteEnemyPings = state.enemyPings,
            localEnemyPings = localEnemyPings,
            me = state.me,
            headingDeg = state.myHeadingDeg,
            now = nowMs,
        )
    }
    val pointMarkers = remember(
        state.teamPoints,
        state.privatePoints,
        state.me,
        state.myHeadingDeg,
        rangeMeters,
        radarSize,
    ) {
        buildCompassRadarPointMarkers(
            teamPoints = state.teamPoints,
            privatePoints = state.privatePoints,
            me = state.me,
            headingDeg = state.myHeadingDeg,
            rangeMeters = rangeMeters,
            radarSize = radarSize,
            defaultPointLabel = defaultPointLabel,
        )
    }
    return CompassScreenOverlayComputed(
        bluetoothDevicesCount = bluetoothDevicesCount,
        enemyOverlays = enemyOverlays,
        pointMarkers = pointMarkers,
    )
}
