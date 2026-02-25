package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.ui.theme.TeamCompassTheme

@Preview(showBackground = true)
@Composable
private fun TargetListItemPreview() {
    TeamCompassTheme {
        Surface {
            TargetListItem(
                t = CompassTarget(
                    uid = "1",
                    nick = "Тест1",
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
}

@Preview(showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    TeamCompassTheme {
        Surface {
            LoadingOverlay(message = "Подключение...")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TeamCompassTheme {
        Surface {
            EmptyState(
                title = "Список пуст",
                message = "Добавьте участников в команду"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorBannerPreview() {
    TeamCompassTheme {
        Surface {
            ErrorBanner(
                message = "Не удалось подключиться к серверу",
                actionLabel = "Повторить",
                onAction = { }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningBannerPreview() {
    TeamCompassTheme {
        Surface {
            WarningBanner(message = "Низкая точность геолокации")
        }
    }
}
