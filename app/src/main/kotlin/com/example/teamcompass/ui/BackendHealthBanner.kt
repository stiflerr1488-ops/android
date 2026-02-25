package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing

internal enum class BackendHealthBannerState {
    DOWN,
    STALE,
}

@Composable
internal fun BackendHealthBanner(
    state: BackendHealthBannerState,
    modifier: Modifier = Modifier,
) {
    val visuals = when (state) {
        BackendHealthBannerState.DOWN -> BannerVisuals(
            label = stringResource(R.string.backend_banner_down),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            testTag = "backend_banner_down",
        )

        BackendHealthBannerState.STALE -> BannerVisuals(
            label = stringResource(R.string.backend_banner_stale),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            testTag = "backend_banner_stale",
        )
    }

    Card(
        modifier = modifier.testTag(visuals.testTag),
        colors = CardDefaults.cardColors(containerColor = visuals.containerColor),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = visuals.contentColor,
            )
            Text(
                text = visuals.label,
                color = visuals.contentColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class BannerVisuals(
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val testTag: String,
)
