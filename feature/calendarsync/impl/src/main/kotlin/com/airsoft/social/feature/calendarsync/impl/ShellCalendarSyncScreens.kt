package com.airsoft.social.feature.calendarsync.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShellWireframeRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class CalendarSyncUiState(
    val selectedTarget: String = "Системный календарь",
    val targets: List<String> = listOf("Системный календарь", "Google Calendar", "ICS экспорт"),
    val syncEnabled: Boolean = false,
    val exportRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North", "Экспортировано как событие на 1 мар 03:00", "ok"),
        ShellWireframeRow("Тренировка [EW]", "Ожидает подтверждения времени выезда", "draft"),
    ),
    val reminderRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Напоминание за 24 часа", "Игры и турниры", "вкл"),
        ShellWireframeRow("Напоминание за 2 часа", "События с подтверждённым участием", "вкл"),
    ),
)

sealed interface CalendarSyncAction {
    data object CycleTarget : CalendarSyncAction
    data object ToggleSync : CalendarSyncAction
    data object OpenRemindersClicked : CalendarSyncAction
    data object OpenExportHistoryClicked : CalendarSyncAction
}

class CalendarSyncViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarSyncUiState())
    val uiState: StateFlow<CalendarSyncUiState> = _uiState.asStateFlow()

    fun onAction(action: CalendarSyncAction) {
        when (action) {
            CalendarSyncAction.CycleTarget -> {
                val current = _uiState.value
                val idx = current.targets.indexOf(current.selectedTarget)
                val next = current.targets[(idx + 1).mod(current.targets.size)]
                _uiState.value = current.copy(selectedTarget = next)
            }

            CalendarSyncAction.ToggleSync -> {
                _uiState.value = _uiState.value.copy(syncEnabled = !_uiState.value.syncEnabled)
            }

            CalendarSyncAction.OpenRemindersClicked,
            CalendarSyncAction.OpenExportHistoryClicked,
            -> Unit
        }
    }
}

@Composable
fun CalendarSyncShellRoute(
    onOpenReminders: () -> Unit = {},
    onOpenExportHistory: () -> Unit = {},
    calendarSyncViewModel: CalendarSyncViewModel = viewModel(),
) {
    val uiState by calendarSyncViewModel.uiState.collectAsState()
    WireframePage(
        title = "Синхронизация календаря",
        subtitle = "Каркас интеграции с календарём: экспорт игр/событий, напоминания и история синхронизации.",
        primaryActionLabel = "Экспортировать ближайшую игру (заглушка)",
    ) {
        WireframeSection(title = "Цель синхронизации", subtitle = "Выбор календаря/формата и статус интеграции.") {
            WireframeMetricRow(
                items = listOf(
                    "Цель" to uiState.selectedTarget,
                    "Синхр." to if (uiState.syncEnabled) "Вкл" else "Выкл",
                    "Экспортов" to uiState.exportRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.targets.map { if (it == uiState.selectedTarget) "[$it]" else it },
            )
        }
        WireframeSection(title = "Экспортируемые события", subtitle = "Предпросмотр событий, которые попадут в календарь.") {
            uiState.exportRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Напоминания", subtitle = "Быстрый обзор правил напоминаний по играм.") {
            uiState.reminderRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Переходы", subtitle = "Проверка secondary pages синхронизации календаря.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { calendarSyncViewModel.onAction(CalendarSyncAction.CycleTarget) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить цель синхронизации") }
                OutlinedButton(
                    onClick = { calendarSyncViewModel.onAction(CalendarSyncAction.ToggleSync) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Включить / выключить синхронизацию") }
                OutlinedButton(
                    onClick = onOpenReminders,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть напоминания") }
                OutlinedButton(
                    onClick = onOpenExportHistory,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть историю экспорта") }
            }
        }
    }
}

data class CalendarSyncRemindersUiState(
    val selectedPreset: String = "Игры",
    val presets: List<String> = listOf("Игры", "Турниры", "Тренировки"),
    val reminderRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("За 24 часа", "Push + запись в календарь", "on"),
        ShellWireframeRow("За 2 часа", "Push + вибро", "on"),
        ShellWireframeRow("За 30 минут", "Только push", "off"),
    ),
)

sealed interface CalendarSyncRemindersAction {
    data object CyclePreset : CalendarSyncRemindersAction
}

class CalendarSyncRemindersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarSyncRemindersUiState())
    val uiState: StateFlow<CalendarSyncRemindersUiState> = _uiState.asStateFlow()

    fun onAction(action: CalendarSyncRemindersAction) {
        when (action) {
            CalendarSyncRemindersAction.CyclePreset -> {
                val current = _uiState.value
                val idx = current.presets.indexOf(current.selectedPreset)
                val next = current.presets[(idx + 1).mod(current.presets.size)]
                _uiState.value = current.copy(selectedPreset = next)
            }
        }
    }
}

@Composable
fun CalendarSyncRemindersShellRoute(
    calendarSyncRemindersViewModel: CalendarSyncRemindersViewModel = viewModel(),
) {
    val uiState by calendarSyncRemindersViewModel.uiState.collectAsState()
    WireframePage(
        title = "Напоминания календаря",
        subtitle = "Настройки напоминаний для экспорта игр и событий в календарь.",
        primaryActionLabel = "Сохранить пресет (заглушка)",
    ) {
        WireframeSection(title = "Пресет", subtitle = "Быстрый выбор набора правил напоминаний.") {
            WireframeMetricRow(
                items = listOf(
                    "Пресет" to uiState.selectedPreset,
                    "Правил" to uiState.reminderRows.size.toString(),
                    "Канал" to "Push",
                ),
            )
            WireframeChipRow(
                labels = uiState.presets.map { if (it == uiState.selectedPreset) "[$it]" else it },
            )
        }
        WireframeSection(title = "Правила", subtitle = "Интервалы и каналы напоминаний.") {
            uiState.reminderRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring напоминаний.") {
            Button(
                onClick = { calendarSyncRemindersViewModel.onAction(CalendarSyncRemindersAction.CyclePreset) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сменить пресет напоминаний") }
        }
    }
}

data class CalendarSyncExportHistoryUiState(
    val selectedScope: String = "Все",
    val scopes: List<String> = listOf("Все", "Экспорт", "Обновления", "Ошибки"),
    val historyRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North", "Создана запись в календаре: 2026-03-01 03:00", "экспорт"),
        ShellWireframeRow("Тренировка [EW]", "Обновлено время события в календаре", "update"),
        ShellWireframeRow("CQB Sunday", "Ошибка прав доступа к календарю", "error"),
    ),
)

sealed interface CalendarSyncExportHistoryAction {
    data object CycleScope : CalendarSyncExportHistoryAction
}

class CalendarSyncExportHistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarSyncExportHistoryUiState())
    val uiState: StateFlow<CalendarSyncExportHistoryUiState> = _uiState.asStateFlow()

    fun onAction(action: CalendarSyncExportHistoryAction) {
        when (action) {
            CalendarSyncExportHistoryAction.CycleScope -> {
                val current = _uiState.value
                val idx = current.scopes.indexOf(current.selectedScope)
                val next = current.scopes[(idx + 1).mod(current.scopes.size)]
                _uiState.value = current.copy(selectedScope = next)
            }
        }
    }
}

@Composable
fun CalendarSyncExportHistoryShellRoute(
    calendarSyncExportHistoryViewModel: CalendarSyncExportHistoryViewModel = viewModel(),
) {
    val uiState by calendarSyncExportHistoryViewModel.uiState.collectAsState()
    WireframePage(
        title = "История экспорта календаря",
        subtitle = "Лог экспорта/обновления событий и ошибок синхронизации.",
        primaryActionLabel = "Повторить экспорт (заглушка)",
    ) {
        WireframeSection(title = "Фильтр истории", subtitle = "Выбор области журнала синхронизации.") {
            WireframeMetricRow(
                items = listOf(
                    "Фильтр" to uiState.selectedScope,
                    "Записей" to uiState.historyRows.size.toString(),
                    "Источник" to "calendar-sync",
                ),
            )
            WireframeChipRow(
                labels = uiState.scopes.map { if (it == uiState.selectedScope) "[$it]" else it },
            )
        }
        WireframeSection(title = "Журнал", subtitle = "Список операций синхронизации/ошибок.") {
            uiState.historyRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring истории экспорта.") {
            Button(
                onClick = { calendarSyncExportHistoryViewModel.onAction(CalendarSyncExportHistoryAction.CycleScope) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сменить фильтр истории") }
        }
    }
}
