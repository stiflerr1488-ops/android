package com.example.teamcompass.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun DrawScope.drawCompassRadarOverlaysLayer(
    overlays: List<RadarOverlay>,
    center: Offset,
    radarRadiusPx: Float,
    rangeMeters: Float,
    nowMs: Long,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    mediumLabelStyle: TextStyle,
    onSurfaceColor: Color,
    alertRed: Color,
    pointTeam: Color,
    pointPrivate: Color,
) {
    overlays.forEach { o ->
        val pos = polarToPos(
            center = center,
            radarRadiusPx = radarRadiusPx,
            rangeMeters = rangeMeters,
            relDeg = o.relativeBearingDeg.toFloat(),
            distMeters = o.distanceMeters.toFloat(),
        )
        when (o.kind) {
            RadarOverlayKind.ENEMY_PING -> {
                val pingType = normalizeEnemyPingType(o.icon)
                val pingColor = when (pingType) {
                    "ENEMY" -> Color(0xFFFF5252)
                    "ATTACK" -> Color(0xFFFF8A65)
                    "DEFENSE" -> Color(0xFF4FC3F7)
                    else -> alertRed
                }
                val t = ((nowMs % 1000L) / 1000f)
                val pulse = 0.55f + 0.45f * kotlin.math.sin(t * 2f * PI.toFloat())
                val haloRadiusM = o.radiusMeters.takeIf { it > 0.0 }?.toFloat() ?: 15f
                val metersToPx = radarRadiusPx / rangeMeters.coerceAtLeast(1f)
                val baseRadiusPx = (haloRadiusM * metersToPx).coerceAtLeast(4f)
                val rr = baseRadiusPx * (0.92f + 0.16f * pulse)
                drawCircle(
                    color = pingColor.copy(alpha = 0.45f + 0.25f * pulse),
                    radius = rr,
                    center = pos,
                    style = Stroke(width = 2f),
                )
                drawCircle(color = pingColor.copy(alpha = 0.14f), radius = rr, center = pos)
                drawEnemyGlyph(pingType, pos, pingColor)
            }

            RadarOverlayKind.TEAM_POINT, RadarOverlayKind.PRIVATE_POINT -> {
                val c = if (o.kind == RadarOverlayKind.TEAM_POINT) pointTeam else pointPrivate
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

                if (o.icon.isNotBlank()) {
                    val m = textMeasurer.measure(o.icon, style = mediumLabelStyle)
                    drawText(
                        m,
                        topLeft = Offset(pos.x - m.size.width / 2f, pos.y - m.size.height / 2f),
                        color = Color.Black.copy(alpha = 0.80f),
                    )
                }
                if (o.label.isNotBlank()) {
                    val m = textMeasurer.measure(o.label, style = labelStyle)
                    drawText(
                        m,
                        topLeft = Offset(pos.x + 16f, pos.y - m.size.height / 2f),
                        color = onSurfaceColor.copy(alpha = 0.60f),
                    )
                }
            }

            RadarOverlayKind.BLUETOOTH_DEVICE -> {
                val bluetoothGray = Color(0xFF9E9E9E)
                val s = 10f
                drawCircle(
                    color = bluetoothGray.copy(alpha = 0.7f),
                    radius = s,
                    center = pos,
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = s,
                    center = pos,
                    style = Stroke(width = 1.5f),
                )

                val m = textMeasurer.measure("?", style = mediumLabelStyle.copy(fontWeight = FontWeight.Bold))
                drawText(
                    m,
                    topLeft = Offset(pos.x - m.size.width / 2f, pos.y - m.size.height / 2f),
                    color = Color.White.copy(alpha = 0.9f),
                )

                if (o.label.isNotBlank()) {
                    val distLabel = "${o.distanceMeters.roundToInt()}м"
                    val distM = textMeasurer.measure(distLabel, style = labelStyle)
                    drawText(
                        distM,
                        topLeft = Offset(pos.x + 14f, pos.y - distM.size.height / 2f),
                        color = bluetoothGray.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

internal fun DrawScope.drawCompassPlayersLayer(
    targets: List<CompassTarget>,
    allyVisualsByUid: Map<String, AllyVisualDescriptor>,
    angleAnim: MutableMap<String, Animatable<Float, AnimationVector1D>>,
    distAnim: MutableMap<String, Animatable<Float, AnimationVector1D>>,
    alphaAnim: MutableMap<String, Animatable<Float, AnimationVector1D>>,
    center: Offset,
    radarRadiusPx: Float,
    rangeMeters: Float,
    nowMs: Long,
    teamOrange: Color,
    allyBlue: Color,
    anchorBlue: Color,
    deadGrey: Color,
    alertRed: Color,
) {
    targets.forEach { t ->
        val al = alphaAnim[t.uid]?.value ?: alphaForAgeSec(t.lastSeenSec)
        if (al <= 0.01f) return@forEach

        val visual = allyVisualsByUid[t.uid]
        val relationColor = when (visual?.relation ?: AllyRelation.ALLY) {
            AllyRelation.TEAM -> teamOrange
            AllyRelation.ALLY -> allyBlue
        }
        val base = when {
            t.mode == PlayerMode.DEAD -> deadGrey
            else -> relationColor
        }
        val alphaMul = if (t.mode == PlayerMode.DEAD) 0.45f else 1.0f
        val tone = base.copy(alpha = (al * alphaMul).coerceIn(0f, 1f))

        val ang = angleAnim[t.uid]?.value ?: t.relativeBearingDeg.toFloat()
        val distN = distAnim[t.uid]?.value ?: (t.distanceMeters.toFloat() / rangeMeters).coerceIn(0f, 1f)

        val rad = (ang * PI / 180.0).toFloat()
        val dx = sin(rad)
        val dy = -kotlin.math.cos(rad)

        val pos = Offset(center.x + dx * (radarRadiusPx * distN), center.y + dy * (radarRadiusPx * distN))

        if (t.sosActive) {
            drawSosPulseRays(
                center = pos,
                alertColor = alertRed,
                nowMs = nowMs,
                rays = 12,
                innerRadiusPx = 18f,
                outerBasePx = 44f,
                outerPulsePx = 10f,
                alphaBase = 0.55f,
                alphaPulse = 0.30f,
                strokeWidth = 5f,
            )
        }

        val pointSize = when {
            t.mode == PlayerMode.DEAD -> 7f
            t.lowAccuracy -> 10f
            else -> 8f
        }
        drawCircle(color = tone, radius = pointSize, center = pos)
        if (t.anchored && t.mode != PlayerMode.DEAD) {
            drawCircle(
                color = anchorBlue.copy(alpha = (al * 0.9f).coerceIn(0f, 1f)),
                radius = pointSize + 4f,
                center = pos,
                style = Stroke(width = 2.2f),
            )
        }

        drawAllyRoleIcon(
            kind = visual?.roleIcon ?: AllyRoleIcon.FIGHTER,
            center = pos,
            color = Color.White.copy(alpha = (al * 0.95f).coerceIn(0f, 1f)),
            size = pointSize * 0.82f,
        )

        if (distN >= 0.999f && t.staleness != Staleness.HIDDEN) {
            val edge = Offset(center.x + dx * radarRadiusPx, center.y + dy * radarRadiusPx)
            val baseP = Offset(center.x + dx * (radarRadiusPx - 10f), center.y + dy * (radarRadiusPx - 10f))
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
