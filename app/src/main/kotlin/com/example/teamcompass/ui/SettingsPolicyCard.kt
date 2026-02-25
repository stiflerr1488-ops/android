package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun PolicyCard(
    title: String,
    subtitle: String,
    intervalSec: Int,
    distanceM: Int,
    intervalRange: IntRange,
    distanceRange: IntRange,
    onChange: (intervalSec: Int, distanceM: Int) -> Unit,
) {
    val intervalState = remember(intervalSec) { mutableIntStateOf(intervalSec) }
    val distanceState = remember(distanceM) { mutableIntStateOf(distanceM) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Text(
                stringResource(R.string.settings_policy_frequency_format, intervalState.intValue),
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = intervalState.intValue.toFloat(),
                onValueChange = {
                    intervalState.intValue = it.toInt().coerceIn(intervalRange.first, intervalRange.last)
                },
                onValueChangeFinished = {
                    onChange(intervalState.intValue, distanceState.intValue)
                },
                valueRange = intervalRange.first.toFloat()..intervalRange.last.toFloat(),
                steps = (intervalRange.last - intervalRange.first - 1).coerceAtLeast(0),
            )

            Text(
                stringResource(R.string.settings_policy_distance_format, distanceState.intValue),
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = distanceState.intValue.toFloat(),
                onValueChange = {
                    distanceState.intValue = it.toInt().coerceIn(distanceRange.first, distanceRange.last)
                },
                onValueChangeFinished = {
                    onChange(intervalState.intValue, distanceState.intValue)
                },
                valueRange = distanceRange.first.toFloat()..distanceRange.last.toFloat(),
                steps = (distanceRange.last - distanceRange.first - 1).coerceAtLeast(0),
            )
        }
    }
}
