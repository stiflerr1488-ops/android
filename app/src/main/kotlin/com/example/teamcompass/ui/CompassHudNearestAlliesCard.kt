package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.components.TeamRosterItemUi
import com.example.teamcompass.ui.theme.AlphaTokens
import kotlin.math.roundToInt

@Composable
internal fun CompassHudNearestAlliesCard(nearestRosterTop10: List<TeamRosterItemUi>) {
    Card(
        modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.hud_nearest_allies),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (nearestRosterTop10.isEmpty()) {
                Text(
                    text = stringResource(R.string.hud_list_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                nearestRosterTop10.forEachIndexed { index, member ->
                    val distanceLabel = member.distanceMeters?.roundToInt()
                        ?.let { stringResource(R.string.distance_m_format, it) }
                        ?: stringResource(R.string.placeholder_dash)
                    val statusLabel = when {
                        member.sosActive -> "SOS"
                        member.isDead -> stringResource(R.string.status_dead_short)
                        else -> null
                    }
                    val rightLabel = if (statusLabel == null) {
                        distanceLabel
                    } else {
                        "$distanceLabel \u2022 $statusLabel"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val rolePrefix = if (member.isCommander) "\u2605 " else ""
                        Text(
                            text = "${index + 1}. $rolePrefix${member.callsign}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = rightLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
