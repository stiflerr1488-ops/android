package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun MarkerPaletteOverlay(
    onSelectEnemy: () -> Unit,
    onSelectDanger: () -> Unit,
    onSelectAttack: () -> Unit,
    onSelectDefense: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mark_palette"),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center,
        ) {
            FilledTonalButton(
                onClick = onSelectEnemy,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 10.dp)
                    .testTag("mark_type_enemy_button"),
            ) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Text(" ${stringResource(R.string.label_enemy)}")
            }
            FilledTonalButton(
                onClick = onSelectDanger,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 10.dp)
                    .testTag("mark_type_danger_button"),
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Text(" ${stringResource(R.string.label_danger)}")
            }
            FilledTonalButton(
                onClick = onSelectAttack,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-10).dp)
                    .testTag("mark_type_attack_button"),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(" ${stringResource(R.string.label_attack)}")
            }
            FilledTonalButton(
                onClick = onSelectDefense,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-10).dp)
                    .testTag("mark_type_defense_button"),
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text(" ${stringResource(R.string.label_defense)}")
            }
        }
        FilledTonalButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .testTag("mark_type_cancel_button"),
        ) {
            Text(stringResource(R.string.label_cancel))
        }
    }
}

@Composable
internal fun BoxScope.LocationServicesDisabledCta(
    visible: Boolean,
    onEnableLocation: () -> Unit,
) {
    if (!visible) return
    Card(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = Spacing.md, vertical = Spacing.lg)
            .widthIn(max = 460.dp)
            .testTag("location_disabled_cta"),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.location_enable_prompt_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(R.string.location_enable_prompt_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Button(
                onClick = onEnableLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("enable_location_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                )
                Text(" ${stringResource(R.string.action_enable_location)}")
            }
        }
    }
}

@Composable
internal fun BoxScope.ActiveQuickCommandBanner(
    command: QuickCommand?,
    nowMs: Long,
) {
    val cmd = command ?: return
    if (nowMs - cmd.createdAtMs > 60_000L) return

    Card(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = Spacing.xs),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.pill),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlay),
        ),
    ) {
        val txt = when (cmd.type) {
            QuickCommandType.ENEMY -> stringResource(R.string.label_enemy)
            QuickCommandType.ATTACK -> stringResource(R.string.label_attack)
            QuickCommandType.DEFENSE -> stringResource(R.string.label_defense)
            QuickCommandType.DANGER -> stringResource(R.string.label_danger)
        }
        Text(
            txt,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
