package com.example.teamcompass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.ControlSize
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun RadarPointMarker(marker: PointMarkerUi) {
    if (!marker.inRange) return

    val icon = tacticalIconOrNull(marker.iconRaw) ?: TacticalIconId.FLAG
    val bg = if (marker.isTeam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val fg = if (marker.isTeam) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary

    val halfPx = with(androidx.compose.ui.platform.LocalDensity.current) { (ControlSize.pointMarker / 2).toPx() }
    val x = marker.posPx.x
    val y = marker.posPx.y

    Column(
        modifier = Modifier
            .offset { IntOffset((x - halfPx).roundToInt(), (y - halfPx).roundToInt()) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(ControlSize.pointMarker)
                .clip(CircleShape)
                .background(bg.copy(alpha = AlphaTokens.marker)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon.vector, contentDescription = marker.label, tint = fg)
        }
        if (marker.label.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                marker.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.labelBackground))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
internal fun BinaryChoiceButtons(
    modifier: Modifier = Modifier,
    leftText: String,
    rightText: String,
    leftSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        val w = Modifier.weight(1f)
        if (leftSelected) {
            Button(onClick = onLeftClick, modifier = w) { Text(leftText) }
            OutlinedButton(onClick = onRightClick, modifier = w) { Text(rightText) }
        } else {
            OutlinedButton(onClick = onLeftClick, modifier = w) { Text(leftText) }
            Button(onClick = onRightClick, modifier = w) { Text(rightText) }
        }
    }
}

@Composable
internal fun TargetRow(t: CompassTarget) {
    val base = when {
        t.mode == com.example.teamcompass.core.PlayerMode.DEAD -> androidx.compose.ui.graphics.Color(0xFFFF4D4D).copy(alpha = 0.65f)
        t.anchored -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        else -> androidx.compose.ui.graphics.Color(0xFF22C55E)
    }

    val stAlpha = when (t.staleness) {
        Staleness.FRESH -> 1.0f
        Staleness.SUSPECT -> 0.85f
        Staleness.STALE -> 0.55f
        Staleness.HIDDEN -> 0.35f
    }
    val tone = base.copy(alpha = (stAlpha * base.alpha).coerceIn(0f, 1f))

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(t.nick.take(1).uppercase(), color = tone, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(t.nick, fontWeight = FontWeight.SemiBold)
            val dist = if (t.staleness == Staleness.HIDDEN) stringResource(R.string.placeholder_dash) else stringResource(R.string.distance_m_format, t.distanceMeters.roundToInt())
            val seen = stringResource(R.string.target_seen_sec_ago_format, t.lastSeenSec)
            val acc = if (t.lowAccuracy) stringResource(R.string.target_low_accuracy_suffix) else ""
            val mode = when {
                t.mode == com.example.teamcompass.core.PlayerMode.DEAD -> stringResource(R.string.target_dead_suffix)
                t.anchored -> stringResource(R.string.target_anchored_suffix)
                else -> ""
            }
            Text(
                "$dist РІР‚Сћ $seen$acc$mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (t.lowAccuracy) {
            Icon(
                Icons.Default.Warning,
                contentDescription = stringResource(R.string.content_desc_warning),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
internal fun CoordinatesLine(
    me: LocationPoint?,
    headingDeg: Double?,
) {
    val text = if (me == null) {
        stringResource(R.string.gps_line_no_fix)
    } else {
        val lat = formatCoord(me.lat)
        val lon = formatCoord(me.lon)
        val acc = me.accMeters.roundToInt()
        val base = stringResource(R.string.gps_line_prefix, lat, lon)
        val accuracySuffix = if (acc > 0) stringResource(R.string.gps_line_accuracy_suffix, acc) else ""
        val headingSuffix = headingDeg
            ?.let { (((it % 360.0) + 360.0) % 360.0).roundToInt() }
            ?.let { stringResource(R.string.gps_line_heading_suffix, it) }
            ?: ""
        base + accuracySuffix + headingSuffix
    }

    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

internal fun formatCoord(v: Double): String = String.format(Locale.US, "%.5f", v)
