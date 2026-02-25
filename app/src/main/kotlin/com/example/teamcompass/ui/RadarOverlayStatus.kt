package com.example.teamcompass.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.core.LocationPoint
import kotlin.math.roundToInt

@Composable
@Suppress("UNUSED_PARAMETER")
fun RadarLiveHud(
    modifier: Modifier = Modifier,
    me: LocationPoint?,
    headingDeg: Double?,
    rangeMeters: Int = 0,
    hasLocationPermission: Boolean = true,
    isLocationServiceEnabled: Boolean = true,
    geoActionLabel: String? = null,
    onGeoAction: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                stringResource(
                    R.string.hud_heading_format,
                    headingDeg?.let { normalized ->
                        stringResource(
                            R.string.heading_degrees_format,
                            (((normalized % 360.0) + 360.0) % 360.0).roundToInt(),
                        )
                    } ?: stringResource(R.string.placeholder_dash),
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (me == null) {
                    stringResource(R.string.hud_position_no_fix)
                } else {
                    stringResource(R.string.hud_position_format, me.lat.format5(), me.lon.format5())
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun DeviceStatusStrip(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val batteryLevel by produceState(initialValue = -1) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        value = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val batteryText = if (batteryLevel >= 0) "$batteryLevel%" else stringResource(R.string.placeholder_dash)
            Text(
                stringResource(R.string.battery_status_format, batteryText),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun Double.format5(): String = String.format(java.util.Locale.US, "%.5f", this)
