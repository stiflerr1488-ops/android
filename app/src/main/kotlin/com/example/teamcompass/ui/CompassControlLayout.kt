package com.example.teamcompass.ui

data class ControlPosition(
    val xNorm: Float,
    val yNorm: Float,
)

enum class CompassControlId(val prefKey: String) {
    GPS("gps"),
    MENU("menu"),
    LIST("list"),
    ENEMY("enemy"),
    EDIT("edit"),
    MODE("mode"),
    TRACK("track"),
    SCAN_BLUETOOTH("scanBluetooth"),
    SOS("sos"),
    ZOOM_IN("zoomIn"),
    ZOOM_OUT("zoomOut"),
}

enum class ControlLayoutPreset {
    RIGHT_HANDED,
    LEFT_HANDED,
}

fun defaultCompassControlPositions(): Map<CompassControlId, ControlPosition> =
    controlPositionsForPreset(ControlLayoutPreset.RIGHT_HANDED)

fun controlPositionsForPreset(preset: ControlLayoutPreset): Map<CompassControlId, ControlPosition> {
    return when (preset) {
        ControlLayoutPreset.RIGHT_HANDED -> mapOf(
            CompassControlId.GPS to ControlPosition(0.08f, 0.38f),
            CompassControlId.MENU to ControlPosition(0.08f, 0.54f),
            // Ergonomic right-hand column in landscape.
            CompassControlId.LIST to ControlPosition(0.92f, 0.20f),
            CompassControlId.ENEMY to ControlPosition(0.92f, 0.34f),
            CompassControlId.EDIT to ControlPosition(0.92f, 0.46f),
            CompassControlId.MODE to ControlPosition(0.92f, 0.58f),
            CompassControlId.TRACK to ControlPosition(0.92f, 0.70f),
            CompassControlId.SCAN_BLUETOOTH to ControlPosition(0.92f, 0.70f),
            CompassControlId.SOS to ControlPosition(0.92f, 0.82f),
            CompassControlId.ZOOM_IN to ControlPosition(0.08f, 0.84f),
            CompassControlId.ZOOM_OUT to ControlPosition(0.16f, 0.84f),
        )

        ControlLayoutPreset.LEFT_HANDED -> mapOf(
            CompassControlId.GPS to ControlPosition(0.92f, 0.38f),
            CompassControlId.MENU to ControlPosition(0.92f, 0.54f),
            // Ergonomic left-hand column in landscape.
            CompassControlId.LIST to ControlPosition(0.08f, 0.20f),
            CompassControlId.ENEMY to ControlPosition(0.08f, 0.34f),
            CompassControlId.EDIT to ControlPosition(0.08f, 0.46f),
            CompassControlId.MODE to ControlPosition(0.08f, 0.58f),
            CompassControlId.TRACK to ControlPosition(0.08f, 0.70f),
            CompassControlId.SCAN_BLUETOOTH to ControlPosition(0.08f, 0.70f),
            CompassControlId.SOS to ControlPosition(0.08f, 0.82f),
            CompassControlId.ZOOM_IN to ControlPosition(0.92f, 0.84f),
            CompassControlId.ZOOM_OUT to ControlPosition(0.84f, 0.84f),
        )
    }
}

fun ControlPosition.normalized(): ControlPosition {
    return ControlPosition(
        xNorm = xNorm.coerceIn(0f, 1f),
        yNorm = yNorm.coerceIn(0f, 1f),
    )
}

fun ControlPosition.encode(): String {
    val p = normalized()
    return "${p.xNorm},${p.yNorm}"
}

fun decodeControlPosition(raw: String?): ControlPosition? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.split(',')
    if (parts.size != 2) return null
    val x = parts[0].toFloatOrNull() ?: return null
    val y = parts[1].toFloatOrNull() ?: return null
    return ControlPosition(x, y).normalized()
}
