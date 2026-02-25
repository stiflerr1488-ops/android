package com.example.teamcompass.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DraggableControlSlot(
    id: CompassControlId,
    position: ControlPosition,
    containerSize: IntSize,
    editEnabled: Boolean,
    onPositionCommit: (CompassControlId, ControlPosition) -> Unit,
    content: @Composable () -> Unit,
) {
    var current by remember(id, position) { mutableStateOf(position.normalized()) }
    var slotSize by remember(id) { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawingInsets = WindowInsets.safeDrawing
    val edgeMarginPx = with(density) { 12.dp.toPx() }
    val leftInsetPx = safeDrawingInsets.getLeft(density, layoutDirection).toFloat()
    val rightInsetPx = safeDrawingInsets.getRight(density, layoutDirection).toFloat()
    val topInsetPx = safeDrawingInsets.getTop(density).toFloat()
    val bottomInsetPx = safeDrawingInsets.getBottom(density).toFloat()

    val minX = leftInsetPx + edgeMarginPx
    val maxX = (containerSize.width - rightInsetPx - edgeMarginPx - slotSize.width).coerceAtLeast(minX)
    val minY = topInsetPx + edgeMarginPx
    val maxY = (containerSize.height - bottomInsetPx - edgeMarginPx - slotSize.height).coerceAtLeast(minY)
    val usableWidthPx = (maxX - minX).coerceAtLeast(0f)
    val usableHeightPx = (maxY - minY).coerceAtLeast(0f)

    fun normalizeToOffset(norm: Float, min: Float, usable: Float): Float {
        if (usable <= 0f) return min
        return min + norm.coerceIn(0f, 1f) * usable
    }

    fun offsetToNorm(offset: Float, min: Float, usable: Float): Float {
        if (usable <= 0f) return 0f
        return ((offset - min) / usable).coerceIn(0f, 1f)
    }

    val currentX = normalizeToOffset(current.xNorm, minX, usableWidthPx)
    val currentY = normalizeToOffset(current.yNorm, minY, usableHeightPx)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = currentX.roundToInt(),
                    y = currentY.roundToInt(),
                )
            }
            .onSizeChanged { measured ->
                slotSize = measured
            }
            .then(
                if (!editEnabled || containerSize.width <= 0 || containerSize.height <= 0) {
                    Modifier
                } else {
                    Modifier.pointerInput(id, containerSize, slotSize) {
                        detectDragGestures(
                            onDragEnd = { onPositionCommit(id, current) },
                            onDragCancel = { onPositionCommit(id, current) },
                        ) { change, dragAmount ->
                            change.consume()
                            val nextXPx = (normalizeToOffset(current.xNorm, minX, usableWidthPx) + dragAmount.x)
                                .coerceIn(minX, maxX)
                            val nextYPx = (normalizeToOffset(current.yNorm, minY, usableHeightPx) + dragAmount.y)
                                .coerceIn(minY, maxY)
                            current = ControlPosition(
                                xNorm = offsetToNorm(nextXPx, minX, usableWidthPx),
                                yNorm = offsetToNorm(nextYPx, minY, usableHeightPx),
                            )
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
