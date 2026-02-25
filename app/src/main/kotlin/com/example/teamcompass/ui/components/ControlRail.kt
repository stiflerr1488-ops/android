package com.example.teamcompass.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.ControlSize
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.ui.theme.StatusColors

@Composable
fun RailButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showIndicator: Boolean = false,
    indicatorColor: androidx.compose.ui.graphics.Color = StatusColors.error
) {
    Box(contentAlignment = Alignment.TopEnd) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier
                .size(ControlSize.railButton)
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = label },
            shape = CircleShape // Unity shape: circle/pill
        ) {
            Icon(icon, contentDescription = null) // description moved to semantics
        }
        if (showIndicator) {
            Box(
                modifier = Modifier
                    .size(Dimens.railIndicatorSize)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

@Composable
fun ControlRail(
    hasLocationPermission: Boolean,
    onRequestPermission: (Boolean) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onRequestPermission(granted)
    }

    Card(
        modifier = modifier
            .padding(start = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlay)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasLocationPermission) {
                RailButton(
                    icon = Icons.Default.GpsFixed,
                    label = stringResource(R.string.label_geo),
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            RailButton(
                icon = Icons.Default.Menu,
                label = stringResource(R.string.label_menu),
                onClick = onOpenMenu
            )
        }
    }
}

@Composable
fun MenuRail(
    onShowList: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(end = Spacing.xs),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlay)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RailButton(
                icon = Icons.Default.Groups,
                label = stringResource(R.string.label_list),
                onClick = onShowList
            )

            RailButton(
                icon = Icons.Default.Menu,
                label = stringResource(R.string.label_menu),
                onClick = onOpenMenu
            )
        }
    }
}
