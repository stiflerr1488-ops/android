package com.example.teamcompass.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawRadarRangeRings(
    center: Offset,
    radarRadiusPx: Float,
    rangeMeters: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    onSurfaceColor: Color,
) {
    val ringMeters = listOf(
        rangeMeters * 0.25f,
        rangeMeters * 0.50f,
        rangeMeters * 0.75f,
        rangeMeters,
    )
        .map(::roundRadarRingMeters)
        .distinct()
        .sorted()
    ringMeters.forEach { meters ->
        val frac = (meters / rangeMeters).coerceIn(0f, 1f)
        val isOuter = frac >= 0.999f
        val stroke = when {
            isOuter -> 6f
            frac >= 0.75f -> 4f
            frac >= 0.50f -> 3f
            else -> 2f
        }
        val alpha = if (isOuter) 0.08f else 0.04f + frac * 0.015f
        drawCircle(
            color = onSurfaceColor.copy(alpha = alpha),
            radius = radarRadiusPx * frac,
            center = center,
            style = Stroke(width = stroke),
        )

        val txt = formatRangeLabel(meters.coerceAtLeast(1))
        val m = textMeasurer.measure(txt, style = labelStyle)
        drawText(
            m,
            topLeft = Offset(center.x + 8f, center.y - radarRadiusPx * frac - m.size.height / 2f),
            color = onSurfaceColor.copy(alpha = 0.35f),
        )
    }
}

internal fun DrawScope.drawClockDial(
    center: Offset,
    radarRadiusPx: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    compassLabelStyle: TextStyle,
    onSurfaceColor: Color,
) {
    for (hour in 1..12) {
        val angleDeg = (hour % 12) * 30f
        val rad = (angleDeg * PI / 180.0).toFloat()
        val dx = sin(rad)
        val dy = -cos(rad)
        val labelRadius = radarRadiusPx * 0.84f
        val label = hour.toString()
        val style = if (hour == 12) {
            compassLabelStyle.copy(fontWeight = FontWeight.Bold)
        } else {
            labelStyle
        }
        val m = textMeasurer.measure(label, style = style)
        val pos = Offset(
            center.x + dx * labelRadius - m.size.width / 2f,
            center.y + dy * labelRadius - m.size.height / 2f,
        )
        drawText(
            m,
            topLeft = pos,
            color = onSurfaceColor.copy(alpha = if (hour == 12) 0.48f else 0.32f),
        )
    }
}

internal fun DrawScope.drawCompassTicks(
    center: Offset,
    radarRadiusPx: Float,
    headingDeg: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    compassLabelStyle: TextStyle,
    onSurfaceColor: Color,
) {
    for (absDeg in 0 until 360 step 30) {
        val relDeg = relToScreenDeg(absDeg.toFloat(), headingDeg)
        val rad = (relDeg * PI / 180.0).toFloat()
        val dx = sin(rad)
        val dy = -cos(rad)
        val isCardinal = absDeg % 90 == 0
        val tickLen = if (isCardinal) 16f else 10f

        val start = Offset(center.x + dx * radarRadiusPx, center.y + dy * radarRadiusPx)
        val end = Offset(
            center.x + dx * (radarRadiusPx + tickLen),
            center.y + dy * (radarRadiusPx + tickLen),
        )
        drawLine(
            color = onSurfaceColor.copy(alpha = if (isCardinal) 0.28f else 0.18f),
            start = start,
            end = end,
            strokeWidth = if (isCardinal) 4f else 2f,
        )

        val label = when (absDeg) {
            0 -> "Р РЋ"
            90 -> "Р вЂ™"
            180 -> "Р В®"
            270 -> "Р вЂ”"
            else -> absDeg.toString()
        }
        val style = if (isCardinal) {
            compassLabelStyle.copy(fontWeight = FontWeight.Bold)
        } else {
            labelStyle
        }
        val m = textMeasurer.measure(label, style = style)
        val labelRadius = radarRadiusPx + tickLen + (if (isCardinal) 16f else 12f)
        val pos = Offset(
            center.x + dx * labelRadius - m.size.width / 2f,
            center.y + dy * labelRadius - m.size.height / 2f,
        )

        val bgPadding = 4f
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.45f),
            topLeft = Offset(pos.x - bgPadding, pos.y - bgPadding),
            size = Size(m.size.width + bgPadding * 2, m.size.height + bgPadding * 2),
            cornerRadius = CornerRadius(6f, 6f),
        )

        drawText(
            m,
            topLeft = pos,
            color = when (absDeg) {
                0 -> Color(0xFF3B82F6).copy(alpha = 0.95f)
                else -> onSurfaceColor.copy(alpha = if (isCardinal) 0.65f else 0.50f)
            },
        )
    }
}

internal fun DrawScope.drawForwardMarker(
    center: Offset,
    radarRadiusPx: Float,
    color: Color,
) {
    val top = Offset(center.x, center.y - radarRadiusPx)
    val base = Offset(center.x, center.y - radarRadiusPx + 18f)
    val left = Offset(base.x - 10f, base.y)
    val right = Offset(base.x + 10f, base.y)
    val p = Path().apply {
        moveTo(top.x, top.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(p, color = color)
}

internal fun DrawScope.drawCenterDot(
    center: Offset,
    primaryColor: Color,
    onPrimaryColor: Color,
) {
    drawCircle(color = primaryColor, radius = 10f, center = center)
    drawCircle(color = onPrimaryColor.copy(alpha = 0.35f), radius = 4f, center = center)
}

internal fun DrawScope.drawSosPulseRays(
    center: Offset,
    alertColor: Color,
    nowMs: Long,
    rays: Int,
    innerRadiusPx: Float,
    outerBasePx: Float,
    outerPulsePx: Float,
    alphaBase: Float,
    alphaPulse: Float,
    strokeWidth: Float,
) {
    val tt = (nowMs % 900L) / 900f
    val pulse = 0.55f + 0.45f * kotlin.math.sin(tt * 2f * PI.toFloat())
    val outer = outerBasePx + outerPulsePx * pulse
    for (i in 0 until rays) {
        val a = (i.toFloat() / rays.toFloat()) * 2f * PI.toFloat()
        val rx = kotlin.math.cos(a)
        val ry = kotlin.math.sin(a)
        drawLine(
            color = alertColor.copy(alpha = alphaBase + alphaPulse * pulse),
            start = Offset(center.x + rx * innerRadiusPx, center.y + ry * innerRadiusPx),
            end = Offset(center.x + rx * outer, center.y + ry * outer),
            strokeWidth = strokeWidth,
        )
    }
}
