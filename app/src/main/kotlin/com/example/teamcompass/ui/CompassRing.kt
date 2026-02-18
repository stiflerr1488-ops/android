package com.example.teamcompass.ui

import androidx.compose.animation.core.Animatable
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

private fun unwrapAngle(prev: Float, target: Float): Float {
    var t = target
    while (t - prev > 180f) t -= 360f
    while (t - prev < -180f) t += 360f
    return t
}

private fun alphaForAgeSec(ageSec: Long): Float {
    return when {
        ageSec <= 20 -> 1.0f
        ageSec <= 60 -> {
            val k = (ageSec - 20).toFloat() / 40f
            1.0f + (0.65f - 1.0f) * k
        }
        ageSec <= 120 -> {
            val k = (ageSec - 60).toFloat() / 60f
            0.65f + (0.15f - 0.65f) * k
        }
        else -> 0.0f
    }
}

private fun formatRangeLabel(meters: Int): String {
    return if (meters >= 1000) {
        val km = meters / 1000.0
        if (km == km.roundToInt().toDouble()) "${km.roundToInt()}км" else String.format("%.1fкм", km)
    } else {
        "${meters}м"
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun CompassRing(
    modifier: Modifier = Modifier,
    targets: List<CompassTarget>,
    overlays: List<RadarOverlay> = emptyList(),
    /** Current radar range (outer ring) in meters. 10..1000 */
    rangeMeters: Float = 1000f,
    /** My current location; required for rendering geo-referenced KMZ maps. */
    me: LocationPoint? = null,
    /** Continuous device heading in degrees (0..360 where 0=north). */
    myHeadingDeg: Double? = null,
    /** Optional tactical map (KMZ/KML) drawn under the radar. */
    tacticalMap: TacticalMapRender? = null,
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
    val gameGreen = Color(0xFF22C55E)
    val anchorBlue = Color(0xFF3B82F6)
    val deadGrey = Color(0xFF9CA3AF)
    val alertRed = Color(0xFFFF3B30)
    val pointTeam = Color(0xFF00D4FF)
    val pointPrivate = Color(0xFFFFD166)

    // Smooth animations per player target
    val angleAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val distAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val alphaAnim = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }

    fun getAngle(uid: String, init: Float) = angleAnim.getOrPut(uid) { Animatable(init) }
    fun getDist(uid: String, init: Float) = distAnim.getOrPut(uid) { Animatable(init) }
    fun getAlpha(uid: String, init: Float) = alphaAnim.getOrPut(uid) { Animatable(init) }

    targets.forEach { t ->
        val uid = t.uid
        val targetAngle = t.relativeBearingDeg.toFloat()
        val targetDist = (t.distanceMeters.toFloat() / rangeMeters).coerceIn(0f, 1f)
        val targetAlpha = alphaForAgeSec(t.lastSeenSec)

        val a = getAngle(uid, targetAngle)
        val d = getDist(uid, targetDist)
        val al = getAlpha(uid, targetAlpha)

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

    Canvas(modifier = modifier.fillMaxSize().padding(6.dp)) {
        val w = size.width
        val h = size.height
        // Make radar the main hero element: occupy almost the whole screen.
        val r = min(w, h) * 0.49f
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

            // Helpers: convert geo -> screen offset
            fun geoToScreen(lat: Double, lon: Double): Offset {
                val (east, north) = toLocalMetersEastNorth(lat, lon, myLoc.lat, myLoc.lon)
                val dist = kotlin.math.sqrt(east * east + north * north)
                if (dist <= 0.01) return center
                val bearing = Math.toDegrees(kotlin.math.atan2(east, north))
                val heading = (myHeadingDeg ?: 0.0)
                val relDeg = bearing - heading
                val rad = Math.toRadians(relDeg)
                val dx = kotlin.math.sin(rad) * dist * ppm
                val dy = -kotlin.math.cos(rad) * dist * ppm
                return Offset((center.x + dx).toFloat(), (center.y + dy).toFloat())
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
                    if (pt.name.isNotBlank()) {
                        val m = textMeasurer.measure(pt.name.take(18), style = labelStyle)
                        drawText(
                            m,
                            topLeft = Offset(pos.x + 10f, pos.y - m.size.height / 2f),
                            color = cs.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        // Subtle grid
        val grid = cs.onSurface.copy(alpha = 0.05f)
        drawLine(grid, Offset(center.x - r, center.y), Offset(center.x + r, center.y), strokeWidth = 2f)
        drawLine(grid, Offset(center.x, center.y - r), Offset(center.x, center.y + r), strokeWidth = 2f)

        // Range rings
        fun ring(frac: Float, stroke: Float, a: Float) {
            drawCircle(
                color = cs.onSurface.copy(alpha = a),
                radius = r * frac,
                center = center,
                style = Stroke(width = stroke)
            )
        }
        ring(1.0f, 6f, 0.10f)
        ring(0.75f, 4f, 0.07f)
        ring(0.50f, 3f, 0.06f)
        ring(0.25f, 2f, 0.05f)

        // Compass ticks (absolute bearings projected into view)
        val heading = (myHeadingDeg ?: 0.0).toFloat()
        fun relToScreenDeg(absDeg: Float): Float = absDeg - heading

        for (absDeg in 0 until 360 step 30) {
            val relDeg = relToScreenDeg(absDeg.toFloat())
            val rad = (relDeg * PI / 180.0).toFloat()
            val dx = sin(rad)
            val dy = -cos(rad)
            val isCardinal = absDeg % 90 == 0
            val tickLen = if (isCardinal) 16f else 10f

            val start = Offset(center.x + dx * r, center.y + dy * r)
            val end = Offset(center.x + dx * (r + tickLen), center.y + dy * (r + tickLen))
            drawLine(
                color = cs.onSurface.copy(alpha = if (isCardinal) 0.28f else 0.18f),
                start = start,
                end = end,
                strokeWidth = if (isCardinal) 4f else 2f
            )

            val label = when (absDeg) {
                0 -> "N"
                90 -> "E"
                180 -> "S"
                270 -> "W"
                else -> absDeg.toString()
            }
            val style = if (isCardinal) compassLabelStyle else labelStyle
            val m = textMeasurer.measure(label, style = style)
            val labelRadius = r + tickLen + (if (isCardinal) 16f else 12f)
            val pos = Offset(
                center.x + dx * labelRadius - m.size.width / 2f,
                center.y + dy * labelRadius - m.size.height / 2f
            )
            drawText(m, topLeft = pos, color = cs.onSurface.copy(alpha = if (isCardinal) 0.55f else 0.40f))
        }

        // Range labels based on current zoom
        val labels = listOf(
            (rangeMeters * 0.25f) to 0.25f,
            (rangeMeters * 0.50f) to 0.50f,
            (rangeMeters * 0.75f) to 0.75f,
            (rangeMeters * 1.00f) to 1.00f,
        )
        labels.forEach { (meters, frac) ->
            val txt = formatRangeLabel(meters.roundToInt().coerceAtLeast(1))
            val m = textMeasurer.measure(txt, style = labelStyle)
            drawText(
                m,
                topLeft = Offset(center.x + 8f, center.y - r * frac - m.size.height / 2f),
                color = cs.onSurface.copy(alpha = 0.35f)
            )
        }

        // Forward marker (top)
        run {
            val top = Offset(center.x, center.y - r)
            val base = Offset(center.x, center.y - r + 18f)
            val left = Offset(base.x - 10f, base.y)
            val right = Offset(base.x + 10f, base.y)
            val p = Path().apply {
                moveTo(top.x, top.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }
            drawPath(p, color = cs.primary.copy(alpha = 0.55f))
        }

        // Me (center)
        drawCircle(color = cs.primary, radius = 10f, center = center)
        drawCircle(color = cs.onPrimary.copy(alpha = 0.35f), radius = 4f, center = center)

        if (mySosActive) {
            val tt = ((nowMs % 900L) / 900f)
            val pulse = 0.55f + 0.45f * kotlin.math.sin(tt * 2f * PI.toFloat())
            val inner = 18f
            val outer = 44f + 10f * pulse
            val rays = 14
            for (i in 0 until rays) {
                val a = (i.toFloat() / rays.toFloat()) * 2f * PI.toFloat()
                val rx = kotlin.math.cos(a)
                val ry = kotlin.math.sin(a)
                drawLine(
                    color = alertRed.copy(alpha = 0.45f + 0.35f * pulse),
                    start = Offset(center.x + rx * inner, center.y + ry * inner),
                    end = Offset(center.x + rx * outer, center.y + ry * outer),
                    strokeWidth = 5f
                )
            }
        }

        fun polarToPos(relDeg: Float, distMeters: Float): Offset {
            val distN = (distMeters / rangeMeters).coerceIn(0f, 1f)
            val rad = (relDeg * PI / 180.0).toFloat()
            val dx = sin(rad)
            val dy = -cos(rad)
            return Offset(center.x + dx * (r * distN), center.y + dy * (r * distN))
        }

        // OVERLAYS (points + enemy)
        overlays.forEach { o ->
            val pos = polarToPos(o.relativeBearingDeg.toFloat(), o.distanceMeters.toFloat())
            when (o.kind) {
                RadarOverlayKind.ENEMY_PING -> {
                    // 1-min halo: pulsating stroke
                    val t = ((nowMs % 1000L) / 1000f)
                    val pulse = 0.55f + 0.45f * kotlin.math.sin(t * 2f * PI.toFloat())
                    val rr = 34f + 10f * pulse
                    drawCircle(
                        color = alertRed.copy(alpha = 0.45f + 0.25f * pulse),
                        radius = rr,
                        center = pos,
                        style = Stroke(width = 6f)
                    )
                    drawCircle(color = alertRed.copy(alpha = 0.20f), radius = rr * 0.55f, center = pos)
                }
                RadarOverlayKind.TEAM_POINT, RadarOverlayKind.PRIVATE_POINT -> {
                    val c = if (o.kind == RadarOverlayKind.TEAM_POINT) pointTeam else pointPrivate
                    // Diamond marker
                    val s = 14f
                    val p = Path().apply {
                        moveTo(pos.x, pos.y - s)
                        lineTo(pos.x + s, pos.y)
                        lineTo(pos.x, pos.y + s)
                        lineTo(pos.x - s, pos.y)
                        close()
                    }
                    drawPath(p, color = c.copy(alpha = 0.95f))
                    drawPath(p, color = Color.Black.copy(alpha = 0.18f), style = Stroke(width = 2f))

                    // Icon inside
                    if (o.icon.isNotBlank()) {
                        val m = textMeasurer.measure(o.icon, style = typo.labelMedium)
                        drawText(
                            m,
                            topLeft = Offset(pos.x - m.size.width / 2f, pos.y - m.size.height / 2f),
                            color = Color.Black.copy(alpha = 0.80f)
                        )
                    }
                    // Label next to marker
                    if (o.label.isNotBlank()) {
                        val m = textMeasurer.measure(o.label, style = labelStyle)
                        drawText(
                            m,
                            topLeft = Offset(pos.x + 16f, pos.y - m.size.height / 2f),
                            color = cs.onSurface.copy(alpha = 0.60f)
                        )
                    }
                }
            }
        }

        // PLAYERS
        targets.forEach { t ->
            val al = alphaAnim[t.uid]?.value ?: alphaForAgeSec(t.lastSeenSec)
            if (al <= 0.01f) return@forEach

            val base = when {
                t.mode == PlayerMode.DEAD -> deadGrey
                t.anchored -> anchorBlue
                else -> gameGreen
            }
            // DEAD should look "transparent gray".
            val alphaMul = if (t.mode == PlayerMode.DEAD) 0.45f else 1.0f
            val tone = base.copy(alpha = (al * alphaMul).coerceIn(0f, 1f))

            val ang = angleAnim[t.uid]?.value ?: t.relativeBearingDeg.toFloat()
            val distN = distAnim[t.uid]?.value ?: (t.distanceMeters.toFloat() / rangeMeters).coerceIn(0f, 1f)

            val rad = (ang * PI / 180.0).toFloat()
            val dx = sin(rad)
            val dy = -cos(rad)

            val pos = Offset(center.x + dx * (r * distN), center.y + dy * (r * distN))

            // SOS rays (1 minute)
            if (t.sosActive) {
                val tt = ((nowMs % 900L) / 900f)
                val pulse = 0.55f + 0.45f * kotlin.math.sin(tt * 2f * PI.toFloat())
                val inner = 18f
                val outer = 44f + 10f * pulse
                val rays = 12
                for (i in 0 until rays) {
                    val a = (i.toFloat() / rays.toFloat()) * 2f * PI.toFloat()
                    val rx = kotlin.math.cos(a)
                    val ry = kotlin.math.sin(a)
                    drawLine(
                        color = alertRed.copy(alpha = 0.55f + 0.30f * pulse),
                        start = Offset(pos.x + rx * inner, pos.y + ry * inner),
                        end = Offset(pos.x + rx * outer, pos.y + ry * outer),
                        strokeWidth = 5f
                    )
                }
            }

            // Point
            val pointSize = when {
                t.mode == PlayerMode.DEAD -> 7f
                t.lowAccuracy -> 10f
                else -> 8f
            }
            drawCircle(color = tone, radius = pointSize, center = pos)

            // If out of range, draw small direction marker at edge
            if (distN >= 0.999f && t.staleness != Staleness.HIDDEN) {
                val edge = Offset(center.x + dx * r, center.y + dy * r)
                val baseP = Offset(center.x + dx * (r - 10f), center.y + dy * (r - 10f))
                val ortho = Offset(-dy, dx)
                val left = Offset(baseP.x + ortho.x * 8f, baseP.y + ortho.y * 8f)
                val right = Offset(baseP.x - ortho.x * 8f, baseP.y - ortho.y * 8f)
                val p = Path().apply {
                    moveTo(edge.x, edge.y)
                    lineTo(left.x, left.y)
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(p, color = tone.copy(alpha = (al * 0.9f).coerceIn(0f, 1f)))
            }
        }
    }
}
