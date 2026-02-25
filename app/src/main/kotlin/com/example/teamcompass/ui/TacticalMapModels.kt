package com.example.teamcompass.ui

/**
 * Offline tactical map parsed from KMZ/KML.
 *
 * MVP: GroundOverlay image + simple vector features.
 */
data class TacticalMap(
    val id: String,
    val name: String,
    /** Absolute directory path where map was extracted (app internal storage). */
    val dirPath: String,
    /** Relative path inside [dirPath] for the main editable KML document. */
    val mainKmlRelativePath: String,
    /** Source document URI used for direct overwrite save, when permission is available. */
    val sourceUriString: String? = null,
    val groundOverlay: GroundOverlay? = null,
    val points: List<KmlPoint> = emptyList(),
    val lines: List<KmlLine> = emptyList(),
    val polygons: List<KmlPolygon> = emptyList(),
)

data class GroundOverlay(
    val name: String,
    /** Relative path inside [TacticalMap.dirPath]. */
    val imageHref: String,
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    /** Degrees, counterclockwise (KML). */
    val rotationDeg: Double = 0.0,
)

data class KmlPoint(
    val id: String,
    val name: String,
    val description: String = "",
    val lat: Double,
    val lon: Double,
    /** Optional icon id from [TacticalIconId.raw]. */
    val iconRaw: String? = null,
    /** Optional ARGB color in 0xAARRGGBB format. */
    val colorArgb: Long? = null,
)

data class KmlLine(
    val id: String,
    val name: String,
    val coords: List<Pair<Double, Double>>, // lat, lon
)

data class KmlPolygon(
    val id: String,
    val name: String,
    val outer: List<Pair<Double, Double>>, // lat, lon
)
