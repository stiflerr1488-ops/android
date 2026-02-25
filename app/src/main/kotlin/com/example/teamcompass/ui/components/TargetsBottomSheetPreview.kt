package com.example.teamcompass.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.ui.theme.TeamCompassTheme

@Preview(showBackground = true)
@Composable
private fun TargetListItemFreshPreview() {
    TeamCompassTheme {
        TargetListItem(
            t = CompassTarget(
                uid = "1",
                nick = "Альфа",
                distanceMeters = 150.0,
                relativeBearingDeg = 45.0,
                staleness = Staleness.FRESH,
                lowAccuracy = false,
                lastSeenSec = 5,
                mode = PlayerMode.GAME,
                anchored = false,
                sosActive = false
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TargetListItemSOSPreview() {
    TeamCompassTheme {
        TargetListItem(
            t = CompassTarget(
                uid = "2",
                nick = "Браво",
                distanceMeters = 320.0,
                relativeBearingDeg = 120.0,
                staleness = Staleness.SUSPECT,
                lowAccuracy = false,
                lastSeenSec = 25,
                mode = PlayerMode.GAME,
                anchored = false,
                sosActive = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TargetListItemDeadPreview() {
    TeamCompassTheme {
        TargetListItem(
            t = CompassTarget(
                uid = "3",
                nick = "Чарли",
                distanceMeters = 500.0,
                relativeBearingDeg = 270.0,
                staleness = Staleness.STALE,
                lowAccuracy = true,
                lastSeenSec = 90,
                mode = PlayerMode.DEAD,
                anchored = false,
                sosActive = false
            )
        )
    }
}
