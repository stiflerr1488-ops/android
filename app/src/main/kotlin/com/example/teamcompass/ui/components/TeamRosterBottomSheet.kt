@file:android.annotation.SuppressLint("LocalContextResourcesRead")

package com.example.teamcompass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.ui.theme.StatusColors
import kotlin.math.roundToInt

data class TeamRosterItemUi(
    val uid: String,
    val callsign: String,
    val distanceMeters: Double?,
    val isCommander: Boolean,
    val isDead: Boolean,
    val sosUntilMs: Long = 0L,
    val sosActive: Boolean = false,
    val commandRole: TeamCommandRole = TeamCommandRole.FIGHTER,
    val combatRole: CombatRole = CombatRole.NONE,
    val vehicleRole: VehicleRole = VehicleRole.NONE,
    val orgPath: TeamOrgPath = TeamOrgPath(),
)

@Composable
private fun TeamRosterRow(
    item: TeamRosterItemUi,
    onClick: (() -> Unit)? = null,
) {
    val resources = LocalContext.current.resources
    val markerColor = when {
        item.isCommander -> MaterialTheme.colorScheme.tertiary
        item.isDead -> StatusColors.dead
        else -> StatusColors.game
    }
    val roleLabel = buildString {
        append(item.commandRole.toUiLabel(resources))
        if (item.combatRole != CombatRole.NONE) {
            append(" / ")
            append(item.combatRole.toUiLabel(resources))
        }
        if (item.vehicleRole != VehicleRole.NONE) {
            append(" / ")
            append(item.vehicleRole.toUiLabel(resources))
        }
    }
    val distanceLabel = item.distanceMeters
        ?.roundToInt()
        ?.let { stringResource(R.string.distance_m_format, it) }
        ?: stringResource(R.string.placeholder_dash)
    val orgLabel = item.orgPath.toShortUiLabel(resources)
    val rowModifier = if (onClick == null) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.listItemPadding, vertical = Dimens.listItemSpacing)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.md))
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.listItemPadding, vertical = Dimens.listItemSpacing)
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.targetDotSize)
                .clip(CircleShape)
                .background(markerColor),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.callsign,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$roleLabel • $distanceLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = orgLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.sosActive) {
            Text(
                stringResource(R.string.label_sos),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = StatusColors.error,
            )
        } else if (item.isDead) {
            Text(
                stringResource(R.string.status_dead_short),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = StatusColors.dead,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRosterBottomSheet(
    members: List<TeamRosterItemUi>,
    showAll: Boolean,
    onShowAllChange: (Boolean) -> Unit,
    onEditMember: ((TeamRosterItemUi) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val visibleMembers = if (showAll) members else members.take(10)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.label_team),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dialog_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.team_roster_summary_format, visibleMembers.size, members.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onEditMember != null) {
                Text(
                    text = stringResource(R.string.team_roster_edit_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (members.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.team_roster_empty_title),
                    message = stringResource(R.string.team_roster_empty_message),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = Dimens.sheetListMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    items(
                        items = visibleMembers,
                        key = { it.uid },
                    ) { item ->
                        TeamRosterRow(
                            item = item,
                            onClick = onEditMember?.let { callback -> { callback(item) } },
                        )
                    }
                }
            }

            if (members.size > 10) {
                if (showAll) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onShowAllChange(false) },
                    ) {
                        Text(stringResource(R.string.team_roster_show_first_10))
                    }
                } else {
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onShowAllChange(true) },
                    ) {
                        Text(stringResource(R.string.team_roster_show_all_format, members.size))
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
        }
    }
}
