package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal fun DrawScope.drawAllyRoleIcon(
    kind: AllyRoleIcon,
    center: Offset,
    color: Color,
    size: Float,
) {
    val strokeW = max(1f, size * 0.26f)

    when (kind) {
        AllyRoleIcon.COMMANDER -> {
            val star = Path().apply {
                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) size else size * 0.45f
                    val angle = (-90.0 + i * 36.0) * PI / 180.0
                    val x = center.x + cos(angle).toFloat() * radius
                    val y = center.y + sin(angle).toFloat() * radius
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path = star, color = color)
        }

        AllyRoleIcon.DEPUTY -> {
            val diamond = Path().apply {
                moveTo(center.x, center.y - size)
                lineTo(center.x + size * 0.9f, center.y)
                lineTo(center.x, center.y + size)
                lineTo(center.x - size * 0.9f, center.y)
                close()
            }
            drawPath(path = diamond, color = color, style = Stroke(width = strokeW))
        }

        AllyRoleIcon.ASSAULT -> {
            val arrow = Path().apply {
                moveTo(center.x - size * 0.8f, center.y - size * 0.7f)
                lineTo(center.x + size * 0.85f, center.y)
                lineTo(center.x - size * 0.8f, center.y + size * 0.7f)
                close()
            }
            drawPath(path = arrow, color = color)
        }

        AllyRoleIcon.SCOUT -> {
            drawOval(
                color = color,
                topLeft = Offset(center.x - size, center.y - size * 0.55f),
                size = Size(width = size * 2f, height = size * 1.1f),
                style = Stroke(width = strokeW),
            )
            drawCircle(color = color, radius = size * 0.22f, center = center)
        }

        AllyRoleIcon.SNIPER -> {
            drawCircle(
                color = color,
                radius = size * 0.85f,
                center = center,
                style = Stroke(width = strokeW),
            )
            drawLine(
                color = color,
                start = Offset(center.x - size, center.y),
                end = Offset(center.x + size, center.y),
                strokeWidth = strokeW,
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y - size),
                end = Offset(center.x, center.y + size),
                strokeWidth = strokeW,
            )
            drawCircle(color = color, radius = size * 0.16f, center = center)
        }

        AllyRoleIcon.MORTAR -> {
            drawLine(
                color = color,
                start = Offset(center.x - size * 0.7f, center.y + size * 0.65f),
                end = Offset(center.x + size * 0.7f, center.y + size * 0.65f),
                strokeWidth = strokeW,
            )
            drawLine(
                color = color,
                start = Offset(center.x - size * 0.25f, center.y + size * 0.55f),
                end = Offset(center.x + size * 0.5f, center.y - size * 0.65f),
                strokeWidth = strokeW,
            )
            drawCircle(
                color = color,
                radius = size * 0.18f,
                center = Offset(center.x + size * 0.58f, center.y - size * 0.72f),
            )
        }

        AllyRoleIcon.VEHICLE -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - size * 0.8f, center.y - size * 0.35f),
                size = Size(width = size * 1.6f, height = size * 0.8f),
            )
            drawCircle(
                color = color,
                radius = size * 0.2f,
                center = Offset(center.x - size * 0.45f, center.y + size * 0.58f),
            )
            drawCircle(
                color = color,
                radius = size * 0.2f,
                center = Offset(center.x + size * 0.45f, center.y + size * 0.58f),
            )
        }

        AllyRoleIcon.FIGHTER -> {
            drawCircle(color = color, radius = size * 0.4f, center = center)
        }
    }
}

