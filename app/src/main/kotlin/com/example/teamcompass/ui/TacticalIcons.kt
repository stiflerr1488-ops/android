package com.example.teamcompass.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tactical marker icon ids saved in Firebase/KMZ as [raw].
 * Keep values stable for data compatibility.
 */
enum class TacticalIconId(
    val raw: String,
    val label: String,
    val vector: ImageVector,
) {
    FLAG("FLAG", "Флаг", Icons.Filled.Flag),
    OBJECTIVE("OBJECTIVE", "Цель", Icons.Filled.CenterFocusStrong),
    WAYPOINT("WAYPOINT", "Маршрут", Icons.Filled.Navigation),
    DEFEND("DEFEND", "Оборона", Icons.Filled.Shield),
    DANGER("DANGER", "Опасность", Icons.Filled.Warning),
    MEDIC("MEDIC", "Медик", Icons.Filled.HealthAndSafety),
    OBSERVE("OBSERVE", "Наблюдение", Icons.Filled.Visibility),
    BASE("BASE", "База", Icons.Filled.Home),
    TOOL("TOOL", "Тех", Icons.Filled.Build),
    RADIO("RADIO", "Связь", Icons.Filled.Radio),
}

fun tacticalIconOrNull(raw: String?): TacticalIconId? {
    val normalized = raw?.trim()?.uppercase() ?: return null
    return when (normalized) {
        // Legacy ids from older builds.
        "ATTACK" -> TacticalIconId.WAYPOINT
        "MARK", "STAR" -> TacticalIconId.OBJECTIVE
        else -> TacticalIconId.entries.firstOrNull { it.raw == normalized }
    }
}
