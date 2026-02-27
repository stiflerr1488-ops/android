package com.airsoft.social.feature.productivity.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

data class DashboardUiState(
    val selectedFocus: String = "Сегодня",
    val focusModes: List<String> = listOf("Сегодня", "Команда", "Подготовка"),
    val upcomingGameRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North", "Сб 19:00 | Полигон Северный | подтверждено 11/14", "игра"),
        ShellWireframeRow("Тренировка [EW]", "Ср 20:00 | лесной участок | 8 подтверждений", "команда"),
    ),
    val newMessageRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Команда [EW]", "Подтвердите участие на субботу до 18:00", "12"),
        ShellWireframeRow("Ghost", "Ищу команду на выходные и место в машине", "1"),
    ),
    val teamActivityRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Состав", "2 игрока обновили роли, 1 новая заявка", "команда"),
        ShellWireframeRow("Логистика", "Открыты 5 мест в попутчиках на Night Raid", "путь"),
    ),
    val recommendationRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Полигон Северный", "Площадка для сценарок и ночных игр", "4.8"),
        ShellWireframeRow("North Tech", "Тюнинг AEG перед субботней игрой", "услуга"),
    ),
)

sealed interface DashboardAction {
    data object CycleFocus : DashboardAction
    data object OpenCalendarClicked : DashboardAction
    data object OpenChatsClicked : DashboardAction
    data object OpenTeamsClicked : DashboardAction
    data object OpenAnnouncementsClicked : DashboardAction
}

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.CycleFocus -> {
                val current = _uiState.value
                val idx = current.focusModes.indexOf(current.selectedFocus)
                val next = current.focusModes[(idx + 1).mod(current.focusModes.size)]
                _uiState.value = current.copy(selectedFocus = next)
            }

            DashboardAction.OpenCalendarClicked,
            DashboardAction.OpenChatsClicked,
            DashboardAction.OpenTeamsClicked,
            DashboardAction.OpenAnnouncementsClicked,
            -> Unit
        }
    }
}

@Composable
fun DashboardShellRoute(
    onOpenCalendar: () -> Unit = {},
    onOpenChats: () -> Unit = {},
    onOpenTeams: () -> Unit = {},
    onOpenAnnouncements: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    DashboardShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                DashboardAction.OpenCalendarClicked -> onOpenCalendar()
                DashboardAction.OpenChatsClicked -> onOpenChats()
                DashboardAction.OpenTeamsClicked -> onOpenTeams()
                DashboardAction.OpenAnnouncementsClicked -> onOpenAnnouncements()
                else -> dashboardViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun DashboardShellScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
) {
    WireframePage(
        title = "Мой день",
        subtitle = "Каркас домашнего экрана: предстоящие игры, новые сообщения, активность команды и рекомендации.",
        primaryActionLabel = "Открыть ближайшую игру (заглушка)",
    ) {
        WireframeSection(
            title = "Фокус дня",
            subtitle = "Режим домашнего экрана: сегодня, команда или подготовка.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.selectedFocus,
                    "Игр" to uiState.upcomingGameRows.size.toString(),
                    "Сообщений" to uiState.newMessageRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.focusModes.map { if (it == uiState.selectedFocus) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Предстоящие игры",
            subtitle = "Быстрый срез ближайших игр и статусов подтверждения.",
        ) {
            uiState.upcomingGameRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Новые сообщения",
            subtitle = "Последние важные сообщения по чатам и команде.",
        ) {
            uiState.newMessageRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Активность команды",
            subtitle = "Состав, роли, заявки и логистика команды.",
        ) {
            uiState.teamActivityRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Рекомендации",
            subtitle = "Подсказки по полигонам, сервисам и подготовке к играм.",
        ) {
            uiState.recommendationRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Переходы",
            subtitle = "Быстрые переходы в ключевые разделы.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(DashboardAction.CycleFocus) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить фокус дня") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OpenCalendarClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть календарь игр") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OpenChatsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть чаты") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OpenTeamsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть команды") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OpenAnnouncementsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть объявления") }
            }
        }
    }
}

data class AnnouncementsUiState(
    val selectedChannel: String = "Все",
    val channels: List<String> = listOf("Все", "Администрация", "Безопасность", "Релизы"),
    val pinnedRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Правила безопасности на полигонах", "Обязательные обновления перед весенним сезоном", "важно"),
        ShellWireframeRow("Технические работы", "Плановое обслуживание в ночь с чт на пт", "service"),
    ),
    val feedRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Обновление shell v0", "Добавлены новые разделы: полигоны, магазины, услуги", "релиз"),
        ShellWireframeRow("Памятка по чекам барахолки", "Как безопасно оформлять сделки и жалобы", "гайд"),
        ShellWireframeRow("Изменения по уведомлениям", "Новые категории входящих и фильтры", "notice"),
    ),
    val unreadCount: Int = 3,
    val pinnedOnly: Boolean = false,
)

sealed interface AnnouncementsAction {
    data object CycleChannel : AnnouncementsAction
    data object TogglePinnedOnly : AnnouncementsAction
    data object OpenAnnouncementDetailClicked : AnnouncementsAction
}

class AnnouncementsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    fun onAction(action: AnnouncementsAction) {
        when (action) {
            AnnouncementsAction.CycleChannel -> {
                val current = _uiState.value
                val idx = current.channels.indexOf(current.selectedChannel)
                val next = current.channels[(idx + 1).mod(current.channels.size)]
                _uiState.value = current.copy(selectedChannel = next)
            }

            AnnouncementsAction.TogglePinnedOnly -> {
                _uiState.value = _uiState.value.copy(pinnedOnly = !_uiState.value.pinnedOnly)
            }

            AnnouncementsAction.OpenAnnouncementDetailClicked -> Unit
        }
    }
}

@Composable
fun AnnouncementsShellRoute(
    onOpenAnnouncementDetail: () -> Unit = {},
    announcementsViewModel: AnnouncementsViewModel = viewModel(),
) {
    val uiState by announcementsViewModel.uiState.collectAsState()
    AnnouncementsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                AnnouncementsAction.OpenAnnouncementDetailClicked -> onOpenAnnouncementDetail()
                else -> announcementsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun AnnouncementsShellScreen(
    uiState: AnnouncementsUiState,
    onAction: (AnnouncementsAction) -> Unit,
) {
    WireframePage(
        title = "Объявления",
        subtitle = "Каркас внутренних объявлений: важные уведомления, релизные заметки и сообщения администрации.",
        primaryActionLabel = "Прочитать закреплённое (заглушка)",
    ) {
        WireframeSection(
            title = "Каналы",
            subtitle = "Фильтр объявлений по источнику и типу.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Канал" to uiState.selectedChannel,
                    "Непрочитано" to uiState.unreadCount.toString(),
                    "Закреплённые" to uiState.pinnedRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.channels.map { if (it == uiState.selectedChannel) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Закреплённые",
            subtitle = "Важные объявления и правила, которые нужно увидеть первыми.",
        ) {
            uiState.pinnedRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Лента объявлений",
            subtitle = "Релизы, заметки, напоминания и сообщения от администрации.",
        ) {
            uiState.feedRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка состояния и переходов в карточку объявления.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(AnnouncementsAction.CycleChannel) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить канал") }
                OutlinedButton(
                    onClick = { onAction(AnnouncementsAction.TogglePinnedOnly) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Только закреплённые") }
                OutlinedButton(
                    onClick = { onAction(AnnouncementsAction.OpenAnnouncementDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть объявление") }
            }
        }
    }
}

data class AnnouncementDetailUiState(
    val announcementId: String = "",
    val selectedSection: String = "Сводка",
    val sections: List<String> = listOf("Сводка", "Что изменилось", "Что сделать"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val changesRows: List<ShellWireframeRow> = emptyList(),
    val actionRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface AnnouncementDetailAction {
    data object CycleSection : AnnouncementDetailAction
}

class AnnouncementDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementDetailUiState())
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    fun load(announcementId: String) {
        if (_uiState.value.announcementId == announcementId) return
        _uiState.value = AnnouncementDetailUiState(
            announcementId = announcementId,
            summaryRows = listOf(
                ShellWireframeRow("Источник", "Администрация Airsoft Social", "official"),
                ShellWireframeRow("Приоритет", "Высокий", "важно"),
                ShellWireframeRow("Период", "Активно до следующего обновления правил", "time"),
            ),
            changesRows = listOf(
                ShellWireframeRow("Безопасность", "Уточнены правила допуска и пиротехники", "rules"),
                ShellWireframeRow("Уведомления", "Добавлены категории входящих и фильтры", "release"),
                ShellWireframeRow("Навигация", "Новые разделы в shell и контекстный поиск", "ui"),
            ),
            actionRows = listOf(
                ShellWireframeRow("Прочитать памятку", "Открыть расширенный документ правил", "docs"),
                ShellWireframeRow("Проверить настройки", "Push и категории уведомлений", "settings"),
                ShellWireframeRow("Сообщить команде", "Поделиться в командный чат", "share"),
            ),
        )
    }

    fun onAction(action: AnnouncementDetailAction) {
        when (action) {
            AnnouncementDetailAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }
        }
    }
}

@Composable
fun AnnouncementDetailSkeletonRoute(
    announcementId: String,
    announcementDetailViewModel: AnnouncementDetailViewModel = viewModel(),
) {
    LaunchedEffect(announcementId) {
        announcementDetailViewModel.load(announcementId)
    }
    val uiState by announcementDetailViewModel.uiState.collectAsState()
    AnnouncementDetailSkeletonScreen(
        uiState = uiState,
        onAction = announcementDetailViewModel::onAction,
    )
}

@Composable
private fun AnnouncementDetailSkeletonScreen(
    uiState: AnnouncementDetailUiState,
    onAction: (AnnouncementDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка объявления",
        subtitle = "Детали внутреннего объявления/уведомления. ID: ${uiState.announcementId}",
        primaryActionLabel = "Отметить прочитанным (заглушка)",
    ) {
        WireframeSection(
            title = "Разделы",
            subtitle = "Сводка, изменения и действия пользователя.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Изменений" to uiState.changesRows.size.toString(),
                    "Действий" to uiState.actionRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Сводка",
            subtitle = "Источник, приоритет и срок актуальности.",
        ) {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Что изменилось",
            subtitle = "Краткий список изменений/улучшений/ограничений.",
        ) {
            uiState.changesRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Что сделать",
            subtitle = "Рекомендованные действия для пользователя/команды.",
        ) {
            uiState.actionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring карточки объявления.",
        ) {
            Button(
                onClick = { onAction(AnnouncementDetailAction.CycleSection) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел карточки") }
        }
    }
}
