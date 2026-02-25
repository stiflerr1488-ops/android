package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import kotlin.math.roundToInt

@Composable
internal fun CompassHudPositionCard(
    state: UiState,
    formatCoord: (Double) -> String,
) {
    Card(
        modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val mePoint = state.me
            val headingText = state.myHeadingDeg
                ?.let { (((it % 360.0) + 360.0) % 360.0).roundToInt().toString() }
                ?: stringResource(R.string.placeholder_dash)
            Text(
                stringResource(R.string.hud_heading_format, headingText),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (mePoint == null) {
                    stringResource(R.string.hud_position_no_fix)
                } else {
                    stringResource(
                        R.string.hud_position_format,
                        formatCoord(mePoint.lat),
                        formatCoord(mePoint.lon),
                    )
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
