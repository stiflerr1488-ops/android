package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.core.TrackingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UiState,
    onBack: () -> Unit,
    onDefaultMode: (TrackingMode) -> Unit,
    onGamePolicy: (intervalSec: Int, distanceM: Int) -> Unit,
    onSilentPolicy: (intervalSec: Int, distanceM: Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.padding(6.dp))
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Режим по умолчанию", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Можно быстро переключать в шапке компаса. Изменения сохраняются на телефоне.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val gameSelected = state.defaultMode == TrackingMode.GAME
                    BinaryChoiceButtons(
                        modifier = Modifier.fillMaxWidth(),
                        leftText = "Игра",
                        rightText = "Тихо",
                        leftSelected = gameSelected,
                        onLeftClick = { if (!gameSelected) onDefaultMode(TrackingMode.GAME) },
                        onRightClick = { if (gameSelected) onDefaultMode(TrackingMode.SILENT) },
                    )
                }
            }

            PolicyCard(
                title = "Профиль: Игра",
                subtitle = "Часто и чуть точнее (для активной фазы).",
                intervalSec = state.gameIntervalSec,
                distanceM = state.gameDistanceM,
                intervalRange = 1..15,
                distanceRange = 5..50,
                onChange = onGamePolicy
            )

            PolicyCard(
                title = "Профиль: Тихо",
                subtitle = "Реже и экономнее (для перемещений/ожидания).",
                intervalSec = state.silentIntervalSec,
                distanceM = state.silentDistanceM,
                intervalRange = 5..60,
                distanceRange = 10..150,
                onChange = onSilentPolicy
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Диагностика", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Read errors: ${state.telemetry.rtdbReadErrors} · Write errors: ${state.telemetry.rtdbWriteErrors}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tracking restarts: ${state.telemetry.trackingRestarts}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.telemetry.lastTrackingRestartReason?.let { reason ->
                        Text(
                            "Last restart reason: $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                "Подсказка: частота = как часто телефон отправляет точку. Дистанция = отправка при заметном смещении.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun BinaryChoiceButtons(
    modifier: Modifier = Modifier,
    leftText: String,
    rightText: String,
    leftSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val w = Modifier.weight(1f)
        if (leftSelected) {
            Button(onClick = onLeftClick, modifier = w) { Text(leftText) }
            OutlinedButton(onClick = onRightClick, modifier = w) { Text(rightText) }
        } else {
            OutlinedButton(onClick = onLeftClick, modifier = w) { Text(leftText) }
            Button(onClick = onRightClick, modifier = w) { Text(rightText) }
        }
    }
}

@Composable
private fun PolicyCard(
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Text("Частота: каждые ${intervalState.intValue} сек", fontWeight = FontWeight.SemiBold)
            Slider(
                value = intervalState.intValue.toFloat(),
                onValueChange = {
                    intervalState.intValue = it.toInt().coerceIn(intervalRange.first, intervalRange.last)
                },
                onValueChangeFinished = {
                    onChange(intervalState.intValue, distanceState.intValue)
                },
                valueRange = intervalRange.first.toFloat()..intervalRange.last.toFloat(),
                steps = (intervalRange.last - intervalRange.first - 1).coerceAtLeast(0)
            )

            Text("Дистанция: > ${distanceState.intValue} м", fontWeight = FontWeight.SemiBold)
            Slider(
                value = distanceState.intValue.toFloat(),
                onValueChange = {
                    distanceState.intValue = it.toInt().coerceIn(distanceRange.first, distanceRange.last)
                },
                onValueChangeFinished = {
                    onChange(intervalState.intValue, distanceState.intValue)
                },
                valueRange = distanceRange.first.toFloat()..distanceRange.last.toFloat(),
                steps = (distanceRange.last - distanceRange.first - 1).coerceAtLeast(0)
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Применяется сразу (если трекинг включён).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
