package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerState

internal const val MIN_SCALE_PX_PER_METER = 0.01f
internal const val MAX_SCALE_PX_PER_METER = 120f
internal const val DEFAULT_IMPORTED_COLOR_ARGB = 0xFF00BCD4L
internal const val DEFAULT_DRAFT_COLOR_ARGB = 0xFFFFB74DL

internal data class DraftKmlPoint(
    val id: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val description: String = "",
    val iconRaw: String = TacticalIconId.OBJECTIVE.raw,
    val colorArgb: Long = DEFAULT_DRAFT_COLOR_ARGB,
) {
    fun toKmlPoint(): KmlPoint {
        return KmlPoint(
            id = id,
            name = name,
            description = description,
            lat = lat,
            lon = lon,
            iconRaw = iconRaw,
            colorArgb = colorArgb,
        )
    }
}

internal data class FullscreenMapSaveToSourcePlan(
    val draftKmlPoints: List<KmlPoint>,
    val deletedPoints: List<KmlPoint>,
    val pendingSavedDraftIds: Set<String>,
    val pendingDeletedPointIds: Set<String>,
    val expectedPointsAfterSave: Int,
)

internal fun buildFullscreenMapSaveToSourcePlan(
    activeMapPoints: List<KmlPoint>,
    currentMapPointCount: Int,
    draftPoints: List<DraftKmlPoint>,
    deletedPointIds: Set<String>,
): FullscreenMapSaveToSourcePlan? {
    if (draftPoints.isEmpty() && deletedPointIds.isEmpty()) return null
    val deletedPoints = activeMapPoints.filter { deletedPointIds.contains(it.id) }
    return FullscreenMapSaveToSourcePlan(
        draftKmlPoints = draftPoints.map { it.toKmlPoint() },
        deletedPoints = deletedPoints,
        pendingSavedDraftIds = draftPoints.map { it.id }.toSet(),
        pendingDeletedPointIds = deletedPoints.map { it.id }.toSet(),
        expectedPointsAfterSave = (currentMapPointCount - deletedPoints.size + draftPoints.size).coerceAtLeast(0),
    )
}

internal fun buildSafeKmzDocumentName(mapName: String): String {
    val safeName = mapName.ifBlank { "map" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
    return "$safeName.kmz"
}

internal data class FullscreenMapPointDialogInit(
    val pendingPoint: GeoPoint,
    val pointName: String,
    val pointDescription: String = "",
)

internal fun buildFullscreenMapPointDialogInit(
    screen: Offset,
    canvasSize: IntSize,
    viewportState: MapViewportState,
    origin: GeoPoint,
    selectedIconRaw: String,
    mapMarkerDefaultName: String,
): FullscreenMapPointDialogInit? {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return null
    val world = screenToWorld(screen, viewportState, canvasSize)
    val latLon = localMetersToLatLon(
        eastMeters = world.first,
        northMeters = world.second,
        originLat = origin.lat,
        originLon = origin.lon,
    )
    return FullscreenMapPointDialogInit(
        pendingPoint = GeoPoint(latLon.first, latLon.second),
        pointName = tacticalIconOrNull(selectedIconRaw)?.label ?: mapMarkerDefaultName,
        pointDescription = "",
    )
}

internal data class FullscreenMapMarkerEditUpdate(
    val draftPoints: List<DraftKmlPoint>,
    val deletedPointIds: Set<String>,
)

internal fun removeMarkerFromEditorState(
    marker: MarkerOverlayUi,
    draftPoints: List<DraftKmlPoint>,
    deletedPointIds: Set<String>,
): FullscreenMapMarkerEditUpdate {
    return if (marker.isDraft) {
        FullscreenMapMarkerEditUpdate(
            draftPoints = draftPoints.filterNot { it.id == marker.point.id },
            deletedPointIds = deletedPointIds,
        )
    } else {
        FullscreenMapMarkerEditUpdate(
            draftPoints = draftPoints,
            deletedPointIds = deletedPointIds + marker.point.id,
        )
    }
}

internal fun hasLiveMarkerDuplicate(
    teamPoints: List<MapPoint>,
    privatePoints: List<MapPoint>,
    lat: Double,
    lon: Double,
    name: String,
    iconRaw: String,
): Boolean {
    val candidates = teamPoints + privatePoints
    return candidates.any { existing ->
        MarkerCorePolicies.isDuplicate(
            latA = lat,
            lonA = lon,
            labelA = name,
            iconRawA = iconRaw,
            latB = existing.lat,
            lonB = existing.lon,
            labelB = existing.label,
            iconRawB = existing.icon,
            toleranceM = DEFAULT_MARKER_DUPLICATE_TOLERANCE_M,
        )
    }
}

internal fun computeFullscreenMapOrigin(
    activeMap: TacticalMap,
    me: LocationPoint?,
    players: List<PlayerState>,
): GeoPoint {
    val fitPoints = buildFitPoints(activeMap)
    return when {
        fitPoints.isNotEmpty() -> {
            val minLat = fitPoints.minOf { it.lat }
            val maxLat = fitPoints.maxOf { it.lat }
            val minLon = fitPoints.minOf { it.lon }
            val maxLon = fitPoints.maxOf { it.lon }
            GeoPoint((minLat + maxLat) / 2.0, (minLon + maxLon) / 2.0)
        }
        me != null -> GeoPoint(me.lat, me.lon)
        players.isNotEmpty() -> GeoPoint(players.first().point.lat, players.first().point.lon)
        else -> GeoPoint(0.0, 0.0)
    }
}
