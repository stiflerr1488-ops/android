package com.example.teamcompass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun RadarPointMarker(
    marker: PointMarkerUi,
    modifier: Modifier = Modifier,
) {
    val icon = tacticalIconOrNull(marker.iconRaw) ?: TacticalIconId.OBJECTIVE
    val bg = if (marker.isTeam) Color(0xFF00B8D4) else Color(0xFFFFB300)
    val markerSize = 30.dp
    val halfPx = 15f

    Column(
        modifier = modifier.offset {
            IntOffset(
                x = (marker.posPx.x - halfPx).roundToInt(),
                y = (marker.posPx.y - halfPx).roundToInt(),
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(markerSize)
                .background(bg.copy(alpha = if (marker.inRange) 0.95f else 0.55f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon.vector,
                contentDescription = icon.label,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun WaypointRoutesOverlay(
    routes: List<WaypointRouteUi>,
    modifier: Modifier = Modifier,
) {
    if (routes.isEmpty()) return
    Canvas(modifier = modifier) {
        routes.forEach { route ->
            val color = if (route.isTeam) Color(0xFF00E5FF) else Color(0xFFFFD54F)
            val pts = route.points
            if (pts.size < 2) return@forEach
            for (i in 1 until pts.size) {
                drawLine(
                    color = color.copy(alpha = 0.85f),
                    start = pts[i - 1],
                    end = pts[i],
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
