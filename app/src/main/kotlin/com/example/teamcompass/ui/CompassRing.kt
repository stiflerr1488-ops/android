package com.example.teamcompass.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.Staleness
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun CompassRing(
    modifier: Modifier = Modifier,
    targets: List<CompassTarget>,
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier.fillMaxSize().padding(8.dp)) {
        val w = size.width
        val h = size.height
        val r = min(w, h) * 0.42f
        val center = Offset(w / 2f, h / 2f)

        // background rings
        drawCircle(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            radius = r,
            center = center,
            style = Stroke(width = 6f)
        )
        drawCircle(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            radius = r * 0.66f,
            center = center,
            style = Stroke(width = 4f)
        )
        drawCircle(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            radius = r * 0.33f,
            center = center,
            style = Stroke(width = 3f)
        )

        // N mark
        val northText = textMeasurer.measure("N", style = MaterialTheme.typography.labelLarge)
        drawText(
            northText,
            topLeft = Offset(center.x - northText.size.width / 2f, center.y - r - northText.size.height - 6f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Center dot (me)
        drawCircle(
            color = MaterialTheme.colorScheme.primary,
            radius = 10f,
            center = center
        )

        // Targets: draw arrows around ring
        targets.forEach { t ->
            if (t.staleness == Staleness.HIDDEN) return@forEach

            val alpha = when (t.staleness) {
                Staleness.FRESH -> 0.95f
                Staleness.SUSPECT -> 0.7f
                Staleness.STALE -> 0.5f
                Staleness.HIDDEN -> 0.0f
            }

            val tone = when (t.staleness) {
                Staleness.FRESH -> MaterialTheme.colorScheme.primary
                Staleness.SUSPECT -> MaterialTheme.colorScheme.secondary
                Staleness.STALE -> MaterialTheme.colorScheme.tertiary
                Staleness.HIDDEN -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            // Relative angle: -180..180 where 0 is forward (north on screen)
            val angRad = ((t.relativeBearingDeg - 90.0) * PI / 180.0).toFloat()
            val tip = Offset(
                x = center.x + cos(angRad) * r,
                y = center.y + sin(angRad) * r
            )

            val base = Offset(
                x = center.x + cos(angRad) * (r - 26f),
                y = center.y + sin(angRad) * (r - 26f)
            )

            val left = Offset(
                x = base.x + cos(angRad + (PI / 2).toFloat()) * 10f,
                y = base.y + sin(angRad + (PI / 2).toFloat()) * 10f
            )
            val right = Offset(
                x = base.x + cos(angRad - (PI / 2).toFloat()) * 10f,
                y = base.y + sin(angRad - (PI / 2).toFloat()) * 10f
            )

            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }
            drawPath(path, color = tone.copy(alpha = alpha))

            // label slightly inside the ring
            val label = "${t.nick} • ${t.distanceMeters.toInt()}м"
            val measured = textMeasurer.measure(label, style = MaterialTheme.typography.labelMedium)
            val labelPos = Offset(
                x = center.x + cos(angRad) * (r - 58f) - measured.size.width / 2f,
                y = center.y + sin(angRad) * (r - 58f) - measured.size.height / 2f
            )
            drawText(
                measured,
                topLeft = labelPos,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        }
    }
}
