package com.example.teamcompass.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.core.StalenessPolicy
import kotlin.math.max
import kotlin.math.roundToInt

internal data class GeoPoint(val lat: Double, val lon: Double)

internal data class MarkerOverlayUi(
    val point: KmlPoint,
    val icon: TacticalIconId,
    val color: Color,
    val pos: Offset,
    val isDraft: Boolean,
)

@Composable
internal fun FullscreenPointMarker(
    marker: MarkerOverlayUi,
    modifier: Modifier = Modifier,
) {
    val markerSize = 28.dp
    val halfPx = with(LocalDensity.current) { (markerSize / 2).toPx() }
    Column(
        modifier = modifier.offset {
            IntOffset(
                x = (marker.pos.x - halfPx).roundToInt(),
                y = (marker.pos.y - halfPx).roundToInt(),
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(markerSize)
                .clip(CircleShape)
                .background(marker.color.copy(alpha = if (marker.isDraft) 0.98f else 0.86f))
                .border(
                    width = if (marker.isDraft) 2.dp else 1.dp,
                    color = if (marker.isDraft) Color.White else Color.Black.copy(alpha = 0.35f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = marker.icon.vector,
                contentDescription = marker.point.name.ifBlank { marker.icon.label },
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
        if (marker.point.name.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = marker.point.name.take(18),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
internal fun FullscreenMapCanvasLayer(
    activeMap: TacticalMap,
    state: UiState,
    nowMs: Long,
    overlayBitmap: Bitmap?,
    canvasSize: IntSize,
    onCanvasSizeChange: (IntSize) -> Unit,
    viewportState: MapViewportState,
    onViewportStateChange: (MapViewportState) -> Unit,
    onViewportInitialized: () -> Unit,
    origin: GeoPoint,
    markerOverlays: List<MarkerOverlayUi>,
    editMode: Boolean,
    deleteMode: Boolean,
    onEditTap: (Offset) -> Unit,
    editHintDeleteMode: String,
    editHintEditMode: String,
    selectedColorArgb: Long,
    onColorSelected: (Long) -> Unit,
    selectedIconRaw: String,
    onIconSelected: (String) -> Unit,
    allyVisualsByUid: Map<String, AllyVisualDescriptor>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged(onCanvasSizeChange)
                .pointerInput(activeMap.id, canvasSize, viewportState) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@detectTransformGestures

                        onViewportInitialized()
                        val prevScale = viewportState.scalePxPerMeter
                        val nextScale = (prevScale * zoom).coerceIn(MIN_SCALE_PX_PER_METER, MAX_SCALE_PX_PER_METER)
                        val baseCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                        val currentCenter = baseCenter + viewportState.offsetPx
                        val appliedZoom = if (prevScale <= 0f) 1f else (nextScale / prevScale)
                        val nextCenter = (currentCenter - centroid) * appliedZoom + centroid + pan

                        onViewportStateChange(
                            viewportState.copy(
                                scalePxPerMeter = nextScale,
                                offsetPx = nextCenter - baseCenter,
                            )
                        )
                    }
                }
                .pointerInput(editMode, deleteMode, viewportState, canvasSize, origin, markerOverlays) {
                    detectTapGestures(
                        onTap = onEditTap,
                        onLongPress = onEditTap,
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(
                x = width / 2f + viewportState.offsetPx.x,
                y = height / 2f + viewportState.offsetPx.y,
            )

            fun geoToScreen(lat: Double, lon: Double): Offset {
                val (east, north) = toLocalMetersEastNorth(lat, lon, origin.lat, origin.lon)
                return Offset(
                    x = center.x + east.toFloat() * viewportState.scalePxPerMeter,
                    y = center.y - north.toFloat() * viewportState.scalePxPerMeter,
                )
            }

            drawRect(color = colorScheme.surfaceVariant.copy(alpha = 0.4f))

            val gridStepMeters = 100f
            val stepPx = (gridStepMeters * viewportState.scalePxPerMeter).coerceIn(24f, 240f)
            val startX = ((-center.x % stepPx) + stepPx) % stepPx
            val startY = ((-center.y % stepPx) + stepPx) % stepPx
            var gx = startX
            while (gx <= width) {
                drawLine(
                    color = colorScheme.outline.copy(alpha = 0.18f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1f,
                )
                gx += stepPx
            }
            var gy = startY
            while (gy <= height) {
                drawLine(
                    color = colorScheme.outline.copy(alpha = 0.18f),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1f,
                )
                gy += stepPx
            }

            val overlay = activeMap.groundOverlay
            val bmp = overlayBitmap
            if (overlay != null && bmp != null) {
                val overlayCenter = latLonBoxCenter(
                    north = overlay.north,
                    south = overlay.south,
                    east = overlay.east,
                    west = overlay.west,
                )
                val overlaySizeMeters = latLonBoxSizeMeters(
                    north = overlay.north,
                    south = overlay.south,
                    east = overlay.east,
                    west = overlay.west,
                )
                val overlayCenterScreen = geoToScreen(overlayCenter.first, overlayCenter.second)
                val targetWidthPx = overlaySizeMeters.first.toFloat() * viewportState.scalePxPerMeter
                val targetHeightPx = overlaySizeMeters.second.toFloat() * viewportState.scalePxPerMeter
                val rotation = -overlay.rotationDeg.toFloat()

                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    val paint = android.graphics.Paint().apply {
                        isFilterBitmap = true
                        alpha = 255
                    }
                    val matrix = Matrix().apply {
                        postTranslate(-bmp.width / 2f, -bmp.height / 2f)
                        postScale(
                            max(1f, targetWidthPx) / bmp.width.toFloat(),
                            max(1f, targetHeightPx) / bmp.height.toFloat(),
                        )
                        postRotate(rotation)
                        postTranslate(overlayCenterScreen.x, overlayCenterScreen.y)
                    }
                    native.drawBitmap(bmp, matrix, paint)
                }
            }

            val pointLabelPaint = android.graphics.Paint().apply {
                color = colorScheme.onSurface.toArgb()
                textSize = with(density) { 12.dp.toPx() }
                isAntiAlias = true
            }

            val teamColor = Color(0xFFFF9800)
            val allyColor = Color(0xFF2196F3)

            state.players.forEach { player ->
                val ageSec = ((nowMs - player.point.timestampMs).coerceAtLeast(0L)) / 1000L
                val staleness = StalenessPolicy.classify(ageSec)
                if (staleness == Staleness.HIDDEN) return@forEach

                val alpha = when (staleness) {
                    Staleness.FRESH -> 1.0f
                    Staleness.SUSPECT -> 0.85f
                    Staleness.STALE -> 0.6f
                    Staleness.HIDDEN -> 0f
                }
                val pos = geoToScreen(player.point.lat, player.point.lon)
                val visual = allyVisualsByUid[player.uid]
                val baseColor = when (visual?.relation ?: AllyRelation.ALLY) {
                    AllyRelation.TEAM -> teamColor
                    AllyRelation.ALLY -> allyColor
                }
                val fillColor = when (player.mode) {
                    PlayerMode.DEAD -> Color(0xFF9E9E9E).copy(alpha = alpha * 0.85f)
                    PlayerMode.GAME -> baseColor.copy(alpha = alpha)
                }
                drawCircle(
                    color = fillColor,
                    radius = 9f,
                    center = pos,
                )
                drawCircle(
                    color = baseColor.copy(alpha = alpha * 0.45f),
                    radius = 12f,
                    center = pos,
                    style = Stroke(width = 2f),
                )
                if (player.anchored && player.mode != PlayerMode.DEAD) {
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        radius = 15f,
                        center = pos,
                        style = Stroke(width = 1.6f),
                    )
                }
                drawAllyRoleIcon(
                    kind = visual?.roleIcon ?: AllyRoleIcon.FIGHTER,
                    center = pos,
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    size = 6.4f,
                )
                drawIntoCanvas { canvas ->
                    pointLabelPaint.alpha = (255f * alpha).toInt().coerceIn(64, 255)
                    canvas.nativeCanvas.drawText(player.nick.take(18), pos.x + 16f, pos.y + 2f, pointLabelPaint)
                }
            }

            val me = state.me
            if (me != null) {
                val pos = geoToScreen(me.lat, me.lon)
                drawCircle(color = Color(0xFF1976D2), radius = 10f, center = pos)
                drawCircle(color = Color.White, radius = 14f, center = pos, style = Stroke(width = 2f))
            }
        }

        markerOverlays.forEach { marker ->
            FullscreenPointMarker(
                marker = marker,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }

        if (editMode) {
            FullscreenMapEditHud(
                deleteMode = deleteMode,
                editHintDeleteMode = editHintDeleteMode,
                editHintEditMode = editHintEditMode,
                selectedColorArgb = selectedColorArgb,
                onColorSelected = onColorSelected,
                selectedIconRaw = selectedIconRaw,
                onIconSelected = onIconSelected,
            )
        }
    }
}

internal fun buildFitPoints(map: TacticalMap): List<GeoPoint> {
    val out = mutableListOf<GeoPoint>()
    map.groundOverlay?.let { overlay ->
        out += GeoPoint(overlay.north, overlay.west)
        out += GeoPoint(overlay.north, overlay.east)
        out += GeoPoint(overlay.south, overlay.west)
        out += GeoPoint(overlay.south, overlay.east)
    }
    map.points.forEach { point ->
        out += GeoPoint(point.lat, point.lon)
    }
    return out
}

internal fun buildFullscreenMapMarkerOverlays(
    allPoints: List<KmlPoint>,
    draftById: Map<String, DraftKmlPoint>,
    viewportState: MapViewportState,
    canvasSize: IntSize,
    origin: GeoPoint,
): List<MarkerOverlayUi> {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return emptyList()

    return allPoints.mapNotNull { point ->
        val draft = draftById[point.id]
        val icon = tacticalIconOrNull(draft?.iconRaw ?: point.iconRaw) ?: TacticalIconId.OBJECTIVE
        val colorArgb = draft?.colorArgb ?: point.colorArgb ?: if (draft != null) {
            DEFAULT_DRAFT_COLOR_ARGB
        } else {
            DEFAULT_IMPORTED_COLOR_ARGB
        }
        val (eastMeters, northMeters) = toLocalMetersEastNorth(
            lat = point.lat,
            lon = point.lon,
            originLat = origin.lat,
            originLon = origin.lon,
        )
        val screenPos = worldToScreen(
            eastMeters = eastMeters,
            northMeters = northMeters,
            viewport = viewportState,
            canvasSize = canvasSize,
        )
        val margin = 120f
        if (screenPos.x < -margin || screenPos.x > canvasSize.width + margin) return@mapNotNull null
        if (screenPos.y < -margin || screenPos.y > canvasSize.height + margin) return@mapNotNull null

        MarkerOverlayUi(
            point = point,
            icon = icon,
            color = argbToColor(colorArgb),
            pos = screenPos,
            isDraft = draft != null,
        )
    }
}

internal fun findMarkerOverlayAt(
    markerOverlays: List<MarkerOverlayUi>,
    screen: Offset,
    thresholdPx: Float,
): MarkerOverlayUi? {
    if (markerOverlays.isEmpty()) return null
    val best = markerOverlays.minByOrNull { marker ->
        val dx = marker.pos.x - screen.x
        val dy = marker.pos.y - screen.y
        dx * dx + dy * dy
    } ?: return null
    val dx = best.pos.x - screen.x
    val dy = best.pos.y - screen.y
    return if (dx * dx + dy * dy <= thresholdPx * thresholdPx) best else null
}

internal fun argbToColor(argb: Long): Color {
    val intArgb = (argb and 0xFFFFFFFFL).toInt()
    return Color(
        red = android.graphics.Color.red(intArgb),
        green = android.graphics.Color.green(intArgb),
        blue = android.graphics.Color.blue(intArgb),
        alpha = android.graphics.Color.alpha(intArgb),
    )
}

internal fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt().coerceIn(0, 255),
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255),
    )
}
