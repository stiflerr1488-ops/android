package com.example.teamcompass.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.example.teamcompass.core.CompassTarget
import java.util.concurrent.atomic.AtomicLong

@Composable
internal fun CompassScreenRadarSurface(
    state: UiState,
    targets: List<CompassTarget>,
    allyVisualsByUid: Map<String, AllyVisualDescriptor>,
    enemyOverlays: List<RadarOverlay>,
    pointMarkers: List<PointMarkerUi>,
    nowMs: Long,
    editMode: Boolean,
    showMarkerPalette: Boolean,
    armedEnemyMarkType: QuickCommandType?,
    onArmedEnemyMarkTypeChange: (QuickCommandType?) -> Unit,
    rangeMeters: Float,
    onRangeMetersChange: (Float) -> Unit,
    zoomStops: List<Float>,
    zoomStickyStop: Float?,
    onZoomStickyStopChange: (Float?) -> Unit,
    radarContentDescription: String,
    radarSize: IntSize,
    onRadarSizeChange: (IntSize) -> Unit,
    ignoreRadarTapUntilMs: AtomicLong,
    context: Context,
    noGpsFixText: String,
    density: Float,
    defaultPointForTeam: Boolean,
    defaultPointIconRaw: String,
    localPingPreviewTtlMs: Long,
    localEnemyPings: List<LocalEnemyPingUi>,
    onLocalEnemyPingsChange: (List<LocalEnemyPingUi>) -> Unit,
    onPointDialogChange: (PointDialogState?) -> Unit,
    onPointActionChange: (PointActionState?) -> Unit,
    onEnemyPing: (Double, Double, QuickCommandType) -> Unit,
    onEnemyMarkEnabled: (Boolean) -> Unit,
) {
    val latestMarkers by rememberUpdatedState(pointMarkers)
    val latestRangeMeters by rememberUpdatedState(rangeMeters)
    val latestMe by rememberUpdatedState(state.me)
    val latestHeadingDeg by rememberUpdatedState(state.myHeadingDeg)
    val latestRadarSize by rememberUpdatedState(radarSize)
    val latestEnemyMarkEnabled by rememberUpdatedState(state.enemyMarkEnabled)
    val latestEnemyMarkType by rememberUpdatedState(state.enemyMarkType)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("radar_canvas")
            .semantics {
                contentDescription = radarContentDescription
            }
            .onSizeChanged { onRadarSizeChange(it) }
            .pointerInput(
                editMode,
                showMarkerPalette,
                armedEnemyMarkType,
                state.enemyMarkEnabled,
            ) {
                if (!editMode && !showMarkerPalette && armedEnemyMarkType == null && !state.enemyMarkEnabled) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            val nextRaw = (latestRangeMeters / zoom).coerceIn(zoomStops.first(), zoomStops.last())
                            val zoomResult = applyCompassStickyZoom(
                                raw = nextRaw,
                                zoomStops = zoomStops,
                                currentStickyStop = zoomStickyStop,
                            )
                            onZoomStickyStopChange(zoomResult.stickyStop)
                            onRangeMetersChange(zoomResult.rangeMeters)
                        }
                    }
                }
            }
            .pointerInput(
                editMode,
                showMarkerPalette,
                armedEnemyMarkType,
                state.enemyMarkEnabled,
            ) {
                detectTapGestures(
                    onTap = { offset ->
                        if (System.currentTimeMillis() < ignoreRadarTapUntilMs.get()) {
                            return@detectTapGestures
                        }
                        if (showMarkerPalette) return@detectTapGestures
                        val selectedType = armedEnemyMarkType ?: if (latestEnemyMarkEnabled) latestEnemyMarkType else null
                        if (selectedType != null && latestMe == null) {
                            Toast.makeText(context, noGpsFixText, Toast.LENGTH_SHORT).show()
                            return@detectTapGestures
                        }
                        val ll = radarScreenToLatLon(
                            offset = offset,
                            me = latestMe,
                            headingDeg = latestHeadingDeg,
                            radarSize = latestRadarSize,
                            rangeMeters = latestRangeMeters,
                        ) ?: return@detectTapGestures
                        if (editMode) {
                            onPointDialogChange(
                                PointDialogState(
                                    id = null,
                                    isTeam = defaultPointForTeam,
                                    createdBy = state.uid,
                                    lat = ll.first,
                                    lon = ll.second,
                                    initialLabel = "",
                                    initialIconRaw = defaultPointIconRaw,
                                )
                            )
                            return@detectTapGestures
                        }

                        if (selectedType != null) {
                            val createdAt = System.currentTimeMillis()
                            onLocalEnemyPingsChange(
                                localEnemyPings + LocalEnemyPingUi(
                                    id = "local_${createdAt}_${selectedType.name}",
                                    lat = ll.first,
                                    lon = ll.second,
                                    type = selectedType,
                                    createdAtMs = createdAt,
                                    expiresAtMs = createdAt + localPingPreviewTtlMs,
                                )
                            )
                            onEnemyPing(ll.first, ll.second, selectedType)
                            onEnemyMarkEnabled(false)
                            onArmedEnemyMarkTypeChange(null)
                        }
                    },
                    onLongPress = { offset ->
                        if (!editMode) return@detectTapGestures
                        val ll = radarScreenToLatLon(
                            offset = offset,
                            me = latestMe,
                            headingDeg = latestHeadingDeg,
                            radarSize = latestRadarSize,
                            rangeMeters = latestRangeMeters,
                        ) ?: return@detectTapGestures

                        val best = latestMarkers.minByOrNull { marker ->
                            val dx = marker.posPx.x - offset.x
                            val dy = marker.posPx.y - offset.y
                            dx * dx + dy * dy
                        }
                        val thresholdPx = 48f * density
                        if (best != null) {
                            val dx = best.posPx.x - offset.x
                            val dy = best.posPx.y - offset.y
                            val d2 = dx * dx + dy * dy
                            if (d2 <= thresholdPx * thresholdPx) {
                                onPointActionChange(PointActionState(best, ll.first, ll.second))
                            }
                        }
                    }
                )
            }
    ) {
        CompassScreenRadarVisualLayer(
            state = state,
            targets = targets,
            allyVisualsByUid = allyVisualsByUid,
            enemyOverlays = enemyOverlays,
            pointMarkers = latestMarkers,
            rangeMeters = rangeMeters,
            nowMs = nowMs,
            showMarkerPalette = showMarkerPalette,
            armedEnemyMarkType = armedEnemyMarkType,
        )
    }
}
