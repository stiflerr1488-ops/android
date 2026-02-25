package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.ControlSize
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.ui.theme.StatusColors

@Composable
fun BottomControlBar(
    playerMode: PlayerMode,
    isTracking: Boolean,
    sosActive: Boolean,
    onTogglePlayerMode: () -> Unit,
    onToggleTracking: () -> Unit,
    onToggleSos: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val actionWidth = if (compact) 92.dp else Dimens.tacticalMenuButtonWidth
    val actionHeight = Dimens.tacticalMenuButtonSize

    Card(
        modifier = modifier
            .padding(
                end = if (compact) Spacing.xs else Spacing.md,
                bottom = if (compact) Spacing.xs else Spacing.md
            ),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) Spacing.xs else Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalButton(
                onClick = onTogglePlayerMode,
                modifier = Modifier
                    .width(actionWidth)
                    .height(actionHeight),
                shape = RoundedCornerShape(Radius.button),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (playerMode == PlayerMode.DEAD) {
                        StatusColors.error.copy(alpha = 0.3f)
                    } else {
                        Color.Transparent
                    }
                )
            ) {
                Text(
                    text = if (playerMode == PlayerMode.DEAD) {
                        stringResource(R.string.label_dead_upper)
                    } else {
                        stringResource(R.string.player_mode_game)
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (playerMode == PlayerMode.DEAD) {
                        StatusColors.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Button(
                onClick = onToggleTracking,
                modifier = Modifier.size(if (compact) Dimens.tacticalMenuButtonSize else ControlSize.railButton),
                shape = RoundedCornerShape(Radius.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) StatusColors.warning else com.example.teamcompass.ui.theme.MulticamActive // <--- Tactical Green from palette
                )
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isTracking) stringResource(R.string.label_stop) else stringResource(R.string.label_start),
                    modifier = Modifier.size(Dimens.iconSizeMd)
                )
                if (!compact) {
                    Spacer(Modifier.width(Spacing.xxs))
                    Text(
                        text = if (isTracking) stringResource(R.string.label_stop_upper) else stringResource(R.string.label_start_upper),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FilledTonalButton(
                onClick = onToggleSos,
                modifier = Modifier
                    .width(actionWidth)
                    .height(actionHeight),
                shape = RoundedCornerShape(Radius.button),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (sosActive) {
                        StatusColors.error.copy(alpha = 0.5f)
                    } else {
                        com.example.teamcompass.ui.theme.MulticamSOS.copy(alpha = 0.15f) // <--- Subtle red hint for SOS
                    }
                )
            ) {
                Text(
                    text = stringResource(R.string.label_sos_upper),
                    fontWeight = FontWeight.Bold,
                    color = if (sosActive) StatusColors.error else com.example.teamcompass.ui.theme.MulticamSOS,
                    maxLines = 1
                )
                if (!compact) {
                    Spacer(Modifier.width(Spacing.xxs))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.label_sos_upper),
                        modifier = Modifier.size(Dimens.iconSizeSm),
                        tint = if (sosActive) StatusColors.error else com.example.teamcompass.ui.theme.MulticamSOS
                    )
                }
            }
        }
    }
}
