package com.example.teamcompass.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.teamcompass.ui.theme.TeamCompassTheme

@Preview(showBackground = true)
@Composable
private fun RailButtonPreview() {
    TeamCompassTheme {
        RailButton(
            icon = Icons.Default.PlayArrow,
            label = "Старт",
            onClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RailButtonWithIndicatorPreview() {
    TeamCompassTheme {
        RailButton(
            icon = Icons.Default.Warning,
            label = "СОС",
            onClick = { },
            showIndicator = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlRailPreview() {
    TeamCompassTheme {
        ControlRail(
            hasLocationPermission = true,
            onRequestPermission = { },
            onOpenMenu = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlRailNoGpsPreview() {
    TeamCompassTheme {
        ControlRail(
            hasLocationPermission = false,
            onRequestPermission = { },
            onOpenMenu = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuRailPreview() {
    TeamCompassTheme {
        MenuRail(
            onShowList = { },
            onOpenMenu = { }
        )
    }
}
