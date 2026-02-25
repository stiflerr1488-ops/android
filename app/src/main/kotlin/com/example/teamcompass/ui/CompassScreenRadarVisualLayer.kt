package com.example.teamcompass.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.teamcompass.core.CompassTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun CompassScreenRadarVisualLayer(
    state: UiState,
    targets: List<CompassTarget>,
    allyVisualsByUid: Map<String, AllyVisualDescriptor>,
    enemyOverlays: List<RadarOverlay>,
    pointMarkers: List<PointMarkerUi>,
    rangeMeters: Float,
    nowMs: Long,
    showMarkerPalette: Boolean,
    armedEnemyMarkType: QuickCommandType?,
) {
    val loadedMapRender by produceState<TacticalMapRender?>(initialValue = null, key1 = state.activeMap?.id) {
        value = null
        val activeMap = state.activeMap ?: return@produceState
        val overlay = activeMap.groundOverlay ?: return@produceState
        val image = File(activeMap.dirPath, overlay.imageHref)
        if (!image.exists()) return@produceState
        val bmp = withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(image.absolutePath)
        }
        if (bmp != null) {
            value = TacticalMapRender(
                overlay = overlay,
                bitmap = bmp,
                opacity = state.mapOpacity,
                points = activeMap.points,
                lines = activeMap.lines,
                polygons = activeMap.polygons,
            )
        }
    }
    val tactical = if (state.mapEnabled) loadedMapRender?.copy(opacity = state.mapOpacity) else null

    Box(Modifier.fillMaxSize()) {
        CompassRing(
            modifier = Modifier.fillMaxSize(),
            targets = targets,
            allyVisualsByUid = allyVisualsByUid,
            overlays = enemyOverlays,
            rangeMeters = rangeMeters,
            me = state.me,
            myHeadingDeg = state.myHeadingDeg,
            tacticalMap = tactical,
            mySosActive = state.mySosUntilMs > nowMs,
            nowMs = nowMs,
        )

        pointMarkers.forEach { marker ->
            key("p_" + marker.isTeam + "_" + marker.id) {
                RadarPointMarker(marker = marker)
            }
        }
        if (showMarkerPalette || armedEnemyMarkType != null || state.enemyMarkEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        }
    }
}
