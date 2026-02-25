package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.ControlSize
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
fun TacticalRailButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    showLabel: Boolean = true,
    buttonSize: Dp = ControlSize.railButton
) {
    if (showLabel) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .width(132.dp)
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = label },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
                },
                contentColor = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
            shape = RoundedCornerShape(Radius.pill),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null)
                Text(" $label")
            }
        }
    } else {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier
                .size(buttonSize)
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = label },
            shape = CircleShape,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        }
    }
}

@Composable
fun TacticalRail(
    onShowList: () -> Unit,
    onToggleEnemy: () -> Unit,
    onToggleEdit: () -> Unit,
    modifier: Modifier = Modifier,
    isEnemyEnabled: Boolean = false,
    isEditEnabled: Boolean = false,
    compact: Boolean = false,
) {
    Card(
        modifier = modifier
            .padding(end = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlay)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TacticalRailButton(
                icon = Icons.Default.Groups,
                label = stringResource(R.string.label_list),
                onClick = onShowList,
                showLabel = !compact,
                buttonSize = if (compact) Dimens.tacticalMenuButtonSize else ControlSize.railButton
            )

            TacticalRailButton(
                icon = Icons.Default.PersonOff,
                label = stringResource(R.string.label_enemy),
                onClick = onToggleEnemy,
                isActive = isEnemyEnabled,
                showLabel = !compact,
                buttonSize = if (compact) Dimens.tacticalMenuButtonSize else ControlSize.railButton
            )

            TacticalRailButton(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.label_edit_short),
                onClick = onToggleEdit,
                isActive = isEditEnabled,
                showLabel = !compact,
                buttonSize = if (compact) Dimens.tacticalMenuButtonSize else ControlSize.railButton
            )
        }
    }
}
