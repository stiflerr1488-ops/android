package com.example.teamcompass.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * "Нормальные" тактические иконки для точек/пингов.
 *
 * В Firebase в поле icon хранится [raw] (строка), чтобы было стабильно и совместимо.
 */
enum class TacticalIconId(
    val raw: String,
    val label: String,
    val vector: ImageVector,
) {
    FLAG("FLAG", "Флаг", Icons.Filled.Flag),
    OBJECTIVE("OBJECTIVE", "Цель", Icons.Filled.CenterFocusStrong),
    ATTACK("ATTACK", "Атака", Icons.Filled.Navigation),
    DEFEND("DEFEND", "Оборона", Icons.Filled.Shield),
    DANGER("DANGER", "Опасность", Icons.Filled.Warning),
    MEDIC("MEDIC", "Медик", Icons.Filled.HealthAndSafety),
    OBSERVE("OBSERVE", "Наблюдение", Icons.Filled.Visibility),
    BASE("BASE", "База", Icons.Filled.Home),
    TOOL("TOOL", "Тех", Icons.Filled.Build),
    MARK("MARK", "Метка", Icons.Filled.LocationOn),
    RADIO("RADIO", "Связь", Icons.Filled.Radio),
    STAR("STAR", "Звезда", Icons.Filled.Star),
}

fun tacticalIconOrNull(raw: String?): TacticalIconId? {
    val r = raw ?: return null
    return TacticalIconId.entries.firstOrNull { it.raw.equals(r, ignoreCase = true) }
}
