package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassMenuSheet(
    teamCode: String?,
    targetFilterState: TargetFilterState?,
    onTargetPresetChange: (TargetFilterPreset) -> Unit,
    onTargetNearRadiusChange: (Int) -> Unit,
    onTargetShowDeadChange: (Boolean) -> Unit,
    onTargetShowStaleChange: (Boolean) -> Unit,
    onTargetFocusModeChange: (Boolean) -> Unit,
    onStatus: () -> Unit,
    onQuickCommands: () -> Unit,
    onMaps: () -> Unit,
    onShowQrCode: () -> Unit,
    onShareLocation: () -> Unit,
    onShareTeam: () -> Unit,
    onCopyCode: () -> Unit,
    onOpenSettings: () -> Unit,
    onLeaveTeam: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun closeAnd(action: () -> Unit) {
        scope.launch {
            runCatching { sheetState.hide() }
            onDismiss()
            action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.menu_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.menu_close)
                    )
                }
            }

            teamCode?.let {
                Text(
                    text = stringResource(R.string.status_team_format, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MenuActionButton(
                    label = stringResource(R.string.menu_show_qr),
                    icon = Icons.Default.GpsFixed,
                    onClick = { closeAnd(onShowQrCode) }
                )
            }

            MenuGroup(title = stringResource(R.string.menu_group_team)) {
                MenuActionButton(
                    label = stringResource(R.string.menu_status),
                    icon = Icons.Default.Groups,
                    onClick = { closeAnd(onStatus) }
                )
                MenuActionButton(
                    label = stringResource(R.string.menu_share_location),
                    icon = Icons.Default.Share,
                    onClick = { closeAnd(onShareLocation) }
                )
                MenuActionButton(
                    label = stringResource(R.string.menu_share_code),
                    icon = Icons.Default.ContentCopy,
                    onClick = { closeAnd(onShareTeam) }
                )
                MenuActionButton(
                    label = stringResource(R.string.label_leave_team),
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = { closeAnd(onLeaveTeam) }
                )
            }

            HorizontalDivider()

            MenuGroup(title = stringResource(R.string.menu_group_navigation)) {
                MenuActionButton(
                    label = stringResource(R.string.label_settings),
                    icon = Icons.Default.Settings,
                    onClick = { closeAnd(onOpenSettings) }
                )
                MenuActionButton(
                    label = stringResource(R.string.menu_maps_kmz_kml),
                    icon = Icons.Default.GpsFixed,
                    onClick = { closeAnd(onMaps) }
                )
            }

            HorizontalDivider()

            MenuGroup(title = stringResource(R.string.menu_group_tools)) {
                MenuActionButton(
                    label = stringResource(R.string.menu_quick_marks),
                    icon = Icons.Default.SwapHoriz,
                    onClick = { closeAnd(onQuickCommands) }
                )
            }

            if (targetFilterState != null) {
                HorizontalDivider()
                MenuGroup(title = stringResource(R.string.menu_group_target_filters)) {
                    TargetFilterBar(
                        state = targetFilterState,
                        onPresetChange = onTargetPresetChange,
                        onNearRadiusChange = onTargetNearRadiusChange,
                        onShowDeadChange = onTargetShowDeadChange,
                        onShowStaleChange = onTargetShowStaleChange,
                        onFocusModeChange = onTargetFocusModeChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaTokens.cardSubtle)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun MenuActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.button)
    ) {
        Icon(icon, contentDescription = label)
        Spacer(Modifier.width(Spacing.xs))
        Text(label)
    }
}
