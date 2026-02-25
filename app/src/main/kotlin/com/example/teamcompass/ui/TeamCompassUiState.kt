package com.example.teamcompass.ui

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PrioritizedTarget
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamViewMode

data class MapPoint(
    val id: String,
    val lat: Double,
    val lon: Double,
    val label: String,
    val icon: String,
    val createdAtMs: Long,
    val createdBy: String? = null,
    val isTeam: Boolean,
)

data class EnemyPing(
    val id: String,
    val lat: Double,
    val lon: Double,
    val createdAtMs: Long,
    val createdBy: String? = null,
    val expiresAtMs: Long = 0L,
    val type: QuickCommandType = QuickCommandType.ENEMY,
    val isBluetooth: Boolean = false,
)

enum class QuickCommandType { ENEMY, ATTACK, DEFENSE, DANGER }

data class QuickCommand(
    val id: String,
    val type: QuickCommandType,
    val createdAtMs: Long,
    val createdBy: String? = null,
)

data class TelemetryState(
    val rtdbReadErrors: Int = 0,
    val rtdbWriteErrors: Int = 0,
    val trackingRestarts: Int = 0,
    val lastLocationAtMs: Long = 0L,
    val lastTrackingRestartReason: String? = null,
    val backendAvailable: Boolean = true,
    val backendUnavailableSinceMs: Long = 0L,
    val lastSnapshotAtMs: Long = 0L,
    val isBackendStale: Boolean = false,
    val p2pInboundMessages: Int = 0,
    val p2pInboundErrors: Int = 0,
)

sealed interface UiEvent {
    data class Error(val message: String) : UiEvent
}

data class AuthState(
    val isReady: Boolean = false,
    val uid: String? = null,
)

data class TrackingUiState(
    val isTracking: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isLocationServiceEnabled: Boolean = true,
    val me: LocationPoint? = null,
    val myHeadingDeg: Double? = null,
    val defaultMode: TrackingMode = TrackingMode.GAME,
    val hasStartedOnce: Boolean = false,
    val isAnchored: Boolean = false,
    val telemetry: TelemetryState = TelemetryState(),
)

data class TeamUiState(
    val callsign: String = "",
    val teamCode: String? = null,
    val players: List<PlayerState> = emptyList(),
    val roleProfiles: List<TeamMemberRoleProfile> = emptyList(),
    val playerMode: PlayerMode = PlayerMode.GAME,
    val mySosUntilMs: Long = 0L,
    val activeCommand: QuickCommand? = null,
    val viewMode: TeamViewMode = TeamViewMode.COMBAT,
    val isBusy: Boolean = false,
)

data class MapUiState(
    val teamPoints: List<MapPoint> = emptyList(),
    val privatePoints: List<MapPoint> = emptyList(),
    val enemyPings: List<EnemyPing> = emptyList(),
    val markers: List<UnifiedMarker> = emptyList(),
    val enemyMarkEnabled: Boolean = false,
    val enemyMarkType: QuickCommandType = QuickCommandType.ENEMY,
    val activeMap: TacticalMap? = null,
    val mapEnabled: Boolean = false,
    val mapOpacity: Float = 0.65f,
)

data class FilterUiState(
    val targetFilterState: TargetFilterState = TargetFilterState(),
    val prioritizedTargets: List<PrioritizedTarget> = emptyList(),
    val displayTargets: List<com.example.teamcompass.core.CompassTarget> = emptyList(),
)

data class SettingsUiState(
    val gameIntervalSec: Int = 3,
    val gameDistanceM: Int = 10,
    val silentIntervalSec: Int = 10,
    val silentDistanceM: Int = 30,
    val autoBrightnessEnabled: Boolean = true,
    val screenBrightness: Float = 0.8f,
    val themeMode: com.example.teamcompass.ui.theme.ThemeMode = com.example.teamcompass.ui.theme.ThemeMode.SYSTEM,
    val controlLayoutEditEnabled: Boolean = false,
    val controlPositions: Map<CompassControlId, ControlPosition> = defaultCompassControlPositions(),
    val showCompassHelpOnce: Boolean = true,
    val showOnboardingOnce: Boolean = true,
)

data class BluetoothUiState(
    val isScanning: Boolean = false,
    val scanResult: com.example.teamcompass.bluetooth.BluetoothScanResult? = null,
)

data class UiState(
    val auth: AuthState = AuthState(),
    val tracking: TrackingUiState = TrackingUiState(),
    val team: TeamUiState = TeamUiState(),
    val map: MapUiState = MapUiState(),
    val filter: FilterUiState = FilterUiState(),
    val settings: SettingsUiState = SettingsUiState(),
    val bluetooth: BluetoothUiState = BluetoothUiState(),
    val lastError: String? = null,
) {
    // Backward-compatible delegating properties while UI migration is in progress.
    val isAuthReady: Boolean get() = auth.isReady
    val uid: String? get() = auth.uid

    val isTracking: Boolean get() = tracking.isTracking
    val hasLocationPermission: Boolean get() = tracking.hasLocationPermission
    val isLocationServiceEnabled: Boolean get() = tracking.isLocationServiceEnabled
    val me: LocationPoint? get() = tracking.me
    val myHeadingDeg: Double? get() = tracking.myHeadingDeg
    val defaultMode: TrackingMode get() = tracking.defaultMode
    val hasStartedOnce: Boolean get() = tracking.hasStartedOnce
    val isAnchored: Boolean get() = tracking.isAnchored
    val telemetry: TelemetryState get() = tracking.telemetry

    val callsign: String get() = team.callsign
    val teamCode: String? get() = team.teamCode
    val players: List<PlayerState> get() = team.players
    val roleProfiles: List<TeamMemberRoleProfile> get() = team.roleProfiles
    val playerMode: PlayerMode get() = team.playerMode
    val mySosUntilMs: Long get() = team.mySosUntilMs
    val activeCommand: QuickCommand? get() = team.activeCommand
    val viewMode: TeamViewMode get() = team.viewMode
    val isBusy: Boolean get() = team.isBusy

    val teamPoints: List<MapPoint> get() = map.teamPoints
    val privatePoints: List<MapPoint> get() = map.privatePoints
    val enemyPings: List<EnemyPing> get() = map.enemyPings
    val markers: List<UnifiedMarker> get() = map.markers
    val enemyMarkEnabled: Boolean get() = map.enemyMarkEnabled
    val enemyMarkType: QuickCommandType get() = map.enemyMarkType
    val activeMap: TacticalMap? get() = map.activeMap
    val mapEnabled: Boolean get() = map.mapEnabled
    val mapOpacity: Float get() = map.mapOpacity

    val targetFilterState: TargetFilterState get() = filter.targetFilterState
    val prioritizedTargets: List<PrioritizedTarget> get() = filter.prioritizedTargets
    val displayTargets: List<com.example.teamcompass.core.CompassTarget> get() = filter.displayTargets

    val gameIntervalSec: Int get() = settings.gameIntervalSec
    val gameDistanceM: Int get() = settings.gameDistanceM
    val silentIntervalSec: Int get() = settings.silentIntervalSec
    val silentDistanceM: Int get() = settings.silentDistanceM
    val autoBrightnessEnabled: Boolean get() = settings.autoBrightnessEnabled
    val screenBrightness: Float get() = settings.screenBrightness
    val themeMode: com.example.teamcompass.ui.theme.ThemeMode get() = settings.themeMode
    val controlLayoutEditEnabled: Boolean get() = settings.controlLayoutEditEnabled
    val controlPositions: Map<CompassControlId, ControlPosition> get() = settings.controlPositions
    val showCompassHelpOnce: Boolean get() = settings.showCompassHelpOnce
    val showOnboardingOnce: Boolean get() = settings.showOnboardingOnce

    val isBluetoothScanning: Boolean get() = bluetooth.isScanning
    val bluetoothScanResult: com.example.teamcompass.bluetooth.BluetoothScanResult? get() = bluetooth.scanResult
}
