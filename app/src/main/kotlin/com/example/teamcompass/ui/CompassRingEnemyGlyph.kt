package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun DrawScope.drawEnemyGlyph(kind: String, pos: Offset, color: Color) {
    when (kind) {
        "ENEMY" -> {
            drawCircle(
                color = color.copy(alpha = 0.95f),
                radius = 8f,
                center = pos,
                style = Stroke(width = 2.2f),
            )
            drawLine(
                color = color.copy(alpha = 0.95f),
                start = Offset(pos.x - 10f, pos.y),
                end = Offset(pos.x + 10f, pos.y),
                strokeWidth = 2f,
            )
            drawLine(
                color = color.copy(alpha = 0.95f),
                start = Offset(pos.x, pos.y - 10f),
                end = Offset(pos.x, pos.y + 10f),
                strokeWidth = 2f,
            )
        }

        "ATTACK" -> {
            val p = Path().apply {
                moveTo(pos.x, pos.y - 9f)
                lineTo(pos.x + 7f, pos.y)
                lineTo(pos.x + 2f, pos.y)
                lineTo(pos.x + 2f, pos.y + 9f)
                lineTo(pos.x - 2f, pos.y + 9f)
                lineTo(pos.x - 2f, pos.y)
                lineTo(pos.x - 7f, pos.y)
                close()
            }
            drawPath(p, color = color.copy(alpha = 0.96f))
        }

        "DEFENSE" -> {
            val p = Path().apply {
                moveTo(pos.x, pos.y - 9f)
                lineTo(pos.x + 8f, pos.y - 3f)
                lineTo(pos.x + 6f, pos.y + 7f)
                lineTo(pos.x, pos.y + 10f)
                lineTo(pos.x - 6f, pos.y + 7f)
                lineTo(pos.x - 8f, pos.y - 3f)
                close()
            }
            drawPath(p, color = color.copy(alpha = 0.95f))
            drawPath(p, color = Color.White.copy(alpha = 0.55f), style = Stroke(width = 1.6f))
        }

        else -> {
            val p = Path().apply {
                moveTo(pos.x, pos.y - 9f)
                lineTo(pos.x + 8f, pos.y + 7f)
                lineTo(pos.x - 8f, pos.y + 7f)
                close()
            }
            drawPath(p, color = color.copy(alpha = 0.96f))
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(pos.x, pos.y - 3f),
                end = Offset(pos.x, pos.y + 3f),
                strokeWidth = 1.8f,
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 1.5f,
                center = Offset(pos.x, pos.y + 5.5f),
            )
        }
    }
}
