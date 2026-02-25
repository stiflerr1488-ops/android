package com.example.teamcompass.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.teamcompass.R
import com.example.teamcompass.core.PlayerMode

@Composable
fun FloatingTrackingButton(
    isTracking: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isTracking) Color(0xFFFF9800) else Color(0xFF22C55E),
        ),
    ) {
        Icon(
            imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isTracking) stringResource(R.string.label_stop) else stringResource(R.string.label_start),
        )
        if (!compact) {
            Text(if (isTracking) " ${stringResource(R.string.label_stop)}" else " ${stringResource(R.string.label_start)}")
        }
    }
}

@Composable
fun FloatingModeButton(
    playerMode: PlayerMode,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (playerMode == PlayerMode.DEAD) Color(0xFF8B0000) else Color(0xFF1565C0),
        ),
    ) {
        Text(
            if (playerMode == PlayerMode.DEAD) {
                stringResource(R.string.label_dead_upper)
            } else {
                stringResource(R.string.label_alive_upper)
            }
        )
    }
}

@Composable
fun FloatingScanButton(
    isScanning: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isScanning) Color(0xFF00897B) else Color(0xFF37474F),
        ),
    ) {
        Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = stringResource(R.string.label_scan))
        val label = if (compact) {
            if (isScanning) " ${stringResource(R.string.label_bt_scanning)}" else " ${stringResource(R.string.label_bt_idle)}"
        } else {
            if (isScanning) " ${stringResource(R.string.label_scan_scanning)}" else " ${stringResource(R.string.label_scan)}"
        }
        Text(label)
    }
}

@Composable
fun FloatingSosButton(
    active: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (active) Color(0xFFD32F2F) else Color(0xFF5D4037),
        ),
    ) {
        Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.label_sos))
        Text(" ${stringResource(R.string.label_sos)}")
    }
}

@Composable
fun MapModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Text(label, maxLines = 1)
    }
}
