package com.example.teamcompass.ui

import androidx.compose.animation.core.Animatable
import com.example.teamcompass.ui.theme.Dimens
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import java.util.Locale

private const val RADAR_RADIUS_FRACTION = 0.47f

@OptIn(ExperimentalTextApi::class)
@Composable
fun CompassRing(
    modifier: Modifier = Modifier,
    targets: List<CompassTarget>,
    allyVisualsByUid: Map<String, AllyVisualDescriptor> = emptyMap(),
    overlays: List<RadarOverlay> = emptyList(),
    /** Current radar range (outer ring) in meters. 30..500 */
    rangeMeters: Float = 500f,
    /** My current location; required for rendering geo-referenced KMZ maps. */
    me: LocationPoint? = null,
    /** Continuous device heading in degrees (0..360 where 0=north). */
    myHeadingDeg: Double? = null,
    /** Optional tactical map (KMZ/KML) drawn under the radar. */
    tacticalMap: TacticalMapRender? = null,
    mapVisualMode: TacticalMapVisualMode = TacticalMapVisualMode.TACTICAL,
    /** If true, draw SOS rays around the center (for the local player). */
    mySosActive: Boolean = false,
    nowMs: Long = System.currentTimeMillis(),
) {
    val textMeasurer = rememberTextMeasurer()

    // Canvas lambda is not a @Composable scope, so cache theme values here.
    val cs = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography
    val labelStyle = typo.labelSmall
    val compassLabelStyle = typo.labelMedium

    // Requested gameplay colors
    val teamOrange = Color(0xFFFF9800)
    val allyBlue = Color(0xFF2196F3)
    val anchorBlue = Color(0xFF3B82F6)
    val deadGrey = Color(0xFF9CA3AF)
    val alertRed = Color(0xFFFF3B30)
    val pointTeam = Color(0xFF00D4FF)
    val pointPrivate = Color(0xFFFFD166)

    // Smooth animations per player target
    val angleAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val distAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val alphaAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }

    targets.forEach { t ->
        val uid = t.uid
        val targetAngle = t.relativeBearingDeg.toFloat()
        val targetDist = (t.distanceMeters.toFloat() / rangeMeters).coerceIn(0f, 1f)
        val targetAlpha = alphaForAgeSec(t.lastSeenSec)

        val a = animatableFor(angleAnim, uid, targetAngle)
        val d = animatableFor(distAnim, uid, targetDist)
        val al = animatableFor(alphaAnim, uid, targetAlpha)

        LaunchedEffect(uid, targetAngle) {
            val next = unwrapAngle(a.value, targetAngle)
            a.animateTo(next, spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f))
        }
        LaunchedEffect(uid, targetDist) {
            d.animateTo(targetDist, tween(durationMillis = 450))
        }
        LaunchedEffect(uid, targetAlpha) {
            al.animateTo(targetAlpha, tween(durationMillis = 500))
        }
    }

    Canvas(modifier = modifier.fillMaxSize().padding(Dimens.radarPadding)) {
        val w = size.width
        val h = size.height
        // Make radar the main hero element: occupy almost the whole screen.
        val r = min(w, h) * RADAR_RADIUS_FRACTION
        val center = Offset(w / 2f, h / 2f)

        // Tactical background
        drawCircle(color = cs.surfaceVariant, radius = r, center = center)

        // Optional KMZ/KML map layer (GroundOverlay + vectors)
        val map = tacticalMap
        val myLoc = me
        if (map != null && myLoc != null) {
            val ov = map.overlay
            val bmp = map.bitmap
            val opacity = map.opacity.coerceIn(0f, 1f)

            // Clip to radar circle
            val clipCircle = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(
                    left = center.x - r,
                    top = center.y - r,
                    right = center.x + r,
                    bottom = center.y + r
                ))
            }

            // Pixel per meter for current zoom
            val ppm = (r / rangeMeters).toDouble()

            val geoToScreen: (Double, Double) -> Offset = { lat, lon ->
                geoToRadarScreen(
                    lat = lat,
                    lon = lon,
                    myLat = myLoc.lat,
                    myLon = myLoc.lon,
                    headingDeg = myHeadingDeg ?: 0.0,
                    center = center,
                    pixelsPerMeter = ppm,
                )
            }

            // GroundOverlay image draw
            val (cLat, cLon) = latLonBoxCenter(ov.north, ov.south, ov.east, ov.west)
            val (wM, hM) = latLonBoxSizeMeters(ov.north, ov.south, ov.east, ov.west)
            val screenCenter = geoToScreen(cLat, cLon)
            val targetWpx = (wM * ppm).toFloat()
            val targetHpx = (hM * ppm).toFloat()

            // Compose canvas uses y-down; positive rotation is clockwise.
            // KML LatLonBox rotation is counterclockwise, so invert its sign.
            val rot = -((myHeadingDeg ?: 0.0) + ov.rotationDeg).toFloat()

            clipPath(clipCircle) {
                drawIntoCanvas { cnv ->
                    val nc = cnv.nativeCanvas
                    val paint = android.graphics.Paint().apply {
                        isFilterBitmap = true
                        alpha = (opacity * 255f).roundToInt().coerceIn(0, 255)
                    }
                    val m = android.graphics.Matrix()
                    m.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
                    m.postScale(targetWpx / bmp.width.toFloat(), targetHpx / bmp.height.toFloat())
                    m.postRotate(rot)
                    m.postTranslate(screenCenter.x, screenCenter.y)
                    nc.drawBitmap(bmp, m, paint)
                }

                // Vector features (very light styling; MVP)
                val stroke = cs.onSurface.copy(alpha = 0.20f)
                val fill = cs.onSurface.copy(alpha = 0.06f)

                map.polygons.forEach { pg ->
                    if (pg.outer.size < 3) return@forEach
                    val p = Path()
                    pg.outer.forEachIndexed { idx, (lat, lon) ->
                        val pt = geoToScreen(lat, lon)
                        if (idx == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
                    }
                    p.close()
                    drawPath(p, color = fill)
                    drawPath(p, color = stroke, style = Stroke(width = 3f))
                }

                map.lines.forEach { ln ->
                    if (ln.coords.size < 2) return@forEach
                    val p = Path()
                    ln.coords.forEachIndexed { idx, (lat, lon) ->
                        val pt = geoToScreen(lat, lon)
                        if (idx == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
                    }
                    drawPath(p, color = stroke, style = Stroke(width = 3f))
                }

                map.points.forEach { pt ->
                    val pos = geoToScreen(pt.lat, pt.lon)
                    // icon dot
                    drawCircle(color = cs.tertiary.copy(alpha = 0.85f), radius = 7f, center = pos)
                }

                when (mapVisualMode) {
                    TacticalMapVisualMode.TACTICAL -> Unit
                    TacticalMapVisualMode.NIGHT -> {
                        drawCircle(
                            color = Color(0xAA06111D),
                            radius = r,
                            center = center
                        )
                    }
                    TacticalMapVisualMode.THERMAL -> {
                        drawCircle(
                            color = Color(0x66281A00),
                            radius = r,
                            center = center
                        )
                        drawCircle(
                            color = Color(0x33FF7A00),
                            radius = r * 0.9f,
                            center = center
                        )
                    }
                }
            }
        }

        // Subtle grid
        val grid = cs.onSurface.copy(alpha = 0.05f)
        drawLine(grid, Offset(center.x - r, center.y), Offset(center.x + r, center.y), strokeWidth = 2f)
        drawLine(grid, Offset(center.x, center.y - r), Offset(center.x, center.y + r), strokeWidth = 2f)

        drawRadarRangeRings(
            center = center,
            radarRadiusPx = r,
            rangeMeters = rangeMeters,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
            onSurfaceColor = cs.onSurface,
        )

        // Fixed clock dial inside radar. Player forward is always 12 o'clock.
        drawClockDial(
            center = center,
            radarRadiusPx = r,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
            compassLabelStyle = compassLabelStyle,
            onSurfaceColor = cs.onSurface,
        )

        // Compass ticks (absolute bearings projected into view)
        val heading = (myHeadingDeg ?: 0.0).toFloat()
        drawCompassTicks(
            center = center,
            radarRadiusPx = r,
            headingDeg = heading,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
            compassLabelStyle = compassLabelStyle,
            onSurfaceColor = cs.onSurface,
        )
        // Forward marker (top)
        drawForwardMarker(
            center = center,
            radarRadiusPx = r,
            color = cs.primary.copy(alpha = 0.55f),
        )

        // Me (center)
        drawCenterDot(
            center = center,
            primaryColor = cs.primary,
            onPrimaryColor = cs.onPrimary,
        )

        if (mySosActive) {
            drawSosPulseRays(
                center = center,
                alertColor = alertRed,
                nowMs = nowMs,
                rays = 14,
                innerRadiusPx = 18f,
                outerBasePx = 44f,
                outerPulsePx = 10f,
                alphaBase = 0.45f,
                alphaPulse = 0.35f,
                strokeWidth = 5f,
            )
        }
        drawCompassRadarOverlaysLayer(
            overlays = overlays,
            center = center,
            radarRadiusPx = r,
            rangeMeters = rangeMeters,
            nowMs = nowMs,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
            mediumLabelStyle = typo.labelMedium,
            onSurfaceColor = cs.onSurface,
            alertRed = alertRed,
            pointTeam = pointTeam,
            pointPrivate = pointPrivate,
        )

        drawCompassPlayersLayer(
            targets = targets,
            allyVisualsByUid = allyVisualsByUid,
            angleAnim = angleAnim,
            distAnim = distAnim,
            alphaAnim = alphaAnim,
            center = center,
            radarRadiusPx = r,
            rangeMeters = rangeMeters,
            nowMs = nowMs,
            teamOrange = teamOrange,
            allyBlue = allyBlue,
            anchorBlue = anchorBlue,
            deadGrey = deadGrey,
            alertRed = alertRed,
        )
    }
}
