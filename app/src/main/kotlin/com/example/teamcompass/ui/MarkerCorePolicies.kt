package com.example.teamcompass.ui

import kotlin.math.sqrt

const val DEFAULT_MARKER_DUPLICATE_TOLERANCE_M = 2.5

object MarkerCorePolicies {

    fun canEdit(marker: UnifiedMarker, viewerUid: String?): Boolean {
        return when (marker.scope) {
            MarkerScope.TEAM, MarkerScope.PRIVATE, MarkerScope.TEAM_EVENT -> {
                marker.ownerUid == null || (viewerUid != null && marker.ownerUid == viewerUid)
            }
        }
    }

    fun canDelete(marker: UnifiedMarker, viewerUid: String?): Boolean = canEdit(marker, viewerUid)

    fun isVisible(marker: UnifiedMarker, viewerUid: String?): Boolean {
        return when (marker.scope) {
            MarkerScope.PRIVATE -> marker.ownerUid == null || (viewerUid != null && marker.ownerUid == viewerUid)
            MarkerScope.TEAM, MarkerScope.TEAM_EVENT -> true
        }
    }

    fun resolveState(marker: UnifiedMarker, nowMs: Long): MarkerState {
        if (marker.state == MarkerState.DISABLED) return MarkerState.DISABLED
        if (marker.expiresAtMs > 0L && nowMs >= marker.expiresAtMs) return MarkerState.EXPIRED
        if (marker.state == MarkerState.EXPIRED) return MarkerState.EXPIRED
        return MarkerState.ACTIVE
    }

    fun isDuplicate(
        a: UnifiedMarker,
        b: UnifiedMarker,
        toleranceM: Double = DEFAULT_MARKER_DUPLICATE_TOLERANCE_M,
    ): Boolean {
        return isDuplicate(
            latA = a.lat,
            lonA = a.lon,
            labelA = a.label,
            iconRawA = a.iconRaw,
            latB = b.lat,
            lonB = b.lon,
            labelB = b.label,
            iconRawB = b.iconRaw,
            toleranceM = toleranceM,
        )
    }

    fun isDuplicate(
        latA: Double,
        lonA: Double,
        labelA: String?,
        iconRawA: String?,
        latB: Double,
        lonB: Double,
        labelB: String?,
        iconRawB: String?,
        toleranceM: Double = DEFAULT_MARKER_DUPLICATE_TOLERANCE_M,
    ): Boolean {
        val (eastMeters, northMeters) = toLocalMetersEastNorth(
            lat = latA,
            lon = lonA,
            originLat = latB,
            originLon = lonB,
        )
        val distanceMeters = sqrt(eastMeters * eastMeters + northMeters * northMeters)
        if (distanceMeters > toleranceM) return false

        val aIcon = normalizeIcon(iconRawA)
        val bIcon = normalizeIcon(iconRawB)
        if (!aIcon.equals(bIcon, ignoreCase = true)) return false

        val aName = labelA.orEmpty().trim()
        val bName = labelB.orEmpty().trim()
        return aName.isBlank() || bName.isBlank() || aName.equals(bName, ignoreCase = true)
    }

    private fun normalizeIcon(raw: String?): String {
        return tacticalIconOrNull(raw)?.raw ?: TacticalIconId.OBJECTIVE.raw
    }
}
