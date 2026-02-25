package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import com.example.teamcompass.R
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
fun TargetFilterBar(
    state: TargetFilterState,
    onPresetChange: (TargetFilterPreset) -> Unit,
    onNearRadiusChange: (Int) -> Unit,
    onShowDeadChange: (Boolean) -> Unit,
    onShowStaleChange: (Boolean) -> Unit,
    onFocusModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PresetChip(
                    text = stringResource(R.string.filter_preset_all),
                    selected = state.preset == TargetFilterPreset.ALL,
                    modifier = Modifier.testTag("filter_preset_all"),
                ) { onPresetChange(TargetFilterPreset.ALL) }
                PresetChip(
                    text = stringResource(R.string.filter_preset_sos),
                    selected = state.preset == TargetFilterPreset.SOS,
                    modifier = Modifier.testTag("filter_preset_sos"),
                ) { onPresetChange(TargetFilterPreset.SOS) }
                PresetChip(
                    text = stringResource(R.string.filter_preset_near),
                    selected = state.preset == TargetFilterPreset.NEAR,
                    modifier = Modifier.testTag("filter_preset_near"),
                ) { onPresetChange(TargetFilterPreset.NEAR) }
                PresetChip(
                    text = stringResource(R.string.filter_preset_active),
                    selected = state.preset == TargetFilterPreset.ACTIVE,
                    modifier = Modifier.testTag("filter_preset_active"),
                ) { onPresetChange(TargetFilterPreset.ACTIVE) }
            }

            if (state.preset == TargetFilterPreset.NEAR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNearRadiusChange((state.nearRadiusM - 25).coerceAtLeast(50)) },
                        modifier = Modifier.testTag("filter_near_decrease"),
                    ) {
                        Text(stringResource(R.string.filter_near_step_down))
                    }
                    Text(
                        stringResource(R.string.filter_near_radius_format, state.nearRadiusM),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = { onNearRadiusChange((state.nearRadiusM + 25).coerceAtMost(500)) },
                        modifier = Modifier.testTag("filter_near_increase"),
                    ) {
                        Text(stringResource(R.string.filter_near_step_up))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.filter_show_dead), style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = state.showDead,
                        onCheckedChange = onShowDeadChange,
                        modifier = Modifier.testTag("filter_show_dead"),
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.filter_show_stale), style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = state.showStale,
                        onCheckedChange = onShowStaleChange,
                        modifier = Modifier.testTag("filter_show_stale"),
                    )
                }
                Button(
                    onClick = { onFocusModeChange(!state.focusMode) },
                    modifier = Modifier.testTag("filter_focus_button"),
                ) {
                    Text(
                        if (state.focusMode) {
                            stringResource(R.string.filter_focus_on)
                        } else {
                            stringResource(R.string.filter_focus_off)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
    )
}
