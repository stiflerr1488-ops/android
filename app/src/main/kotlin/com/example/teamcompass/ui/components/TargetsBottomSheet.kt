package com.example.teamcompass.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.ui.theme.StatusColors
import kotlin.math.roundToInt

@Composable
fun TargetListItem(
    t: CompassTarget,
    priorityScore: Int? = null,
) {
    val base = when {
        t.mode == PlayerMode.DEAD -> StatusColors.dead
        t.anchored -> StatusColors.anchored
        else -> StatusColors.game
    }

    val stAlpha = when (t.staleness) {
        Staleness.FRESH -> 1.0f
        Staleness.SUSPECT -> 0.75f
        Staleness.STALE -> 0.45f
        Staleness.HIDDEN -> 0.25f
    }
    val color = base.copy(alpha = stAlpha)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.listItemPadding, vertical = Dimens.listItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.targetDotSize)
                .clip(CircleShape)
                .background(color)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                t.nick,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val scoreSuffix = priorityScore?.let { stringResource(R.string.targets_priority_suffix_format, it) }.orEmpty()
            Text(
                stringResource(
                    R.string.targets_item_meta_format,
                    t.distanceMeters.roundToInt(),
                    t.lastSeenSec,
                    scoreSuffix,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (t.sosActive) {
            Text(
                stringResource(R.string.label_sos),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetsBottomSheet(
    targets: List<CompassTarget>,
    priorityByUid: Map<String, Int>,
    query: String,
    onQueryChange: (String) -> Unit,
    sortMode: TargetsSortMode,
    onSortModeChange: (TargetsSortMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.targets_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.dialog_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.targets_search_callsign_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Button(
                    onClick = { onSortModeChange(TargetsSortMode.PRIORITY) },
                    modifier = Modifier.weight(1f),
                    enabled = sortMode != TargetsSortMode.PRIORITY
                ) {
                    Text(stringResource(R.string.targets_sort_priority))
                }
                Button(
                    onClick = { onSortModeChange(TargetsSortMode.DISTANCE) },
                    modifier = Modifier.weight(1f),
                    enabled = sortMode != TargetsSortMode.DISTANCE
                ) {
                    Text(stringResource(R.string.targets_sort_distance))
                }
                Button(
                    onClick = { onSortModeChange(TargetsSortMode.FRESHNESS) },
                    modifier = Modifier.weight(1f),
                    enabled = sortMode != TargetsSortMode.FRESHNESS
                ) {
                    Text(stringResource(R.string.targets_sort_freshness))
                }
            }

            if (targets.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.targets_empty_title),
                    message = if (query.isBlank()) {
                        stringResource(R.string.targets_empty_message)
                    } else {
                        stringResource(R.string.targets_empty_search_message)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = Dimens.sheetListMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    items(targets, key = { it.uid }) { t ->
                        TargetListItem(
                            t = t,
                            priorityScore = priorityByUid[t.uid]
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

enum class TargetsSortMode {
    PRIORITY,
    DISTANCE,
    FRESHNESS,
}
