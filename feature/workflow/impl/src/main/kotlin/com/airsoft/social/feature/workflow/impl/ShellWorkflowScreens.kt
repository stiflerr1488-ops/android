package com.airsoft.social.feature.workflow.impl

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

data class SavedFiltersUiState(
    val selectedDomain: String = "Все",
    val domains: List<String> = listOf("Все", "Чаты", "Команды", "События", "Маркет", "Полигоны"),
    val autoApplyEnabled: Boolean = false,
    val presetRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Команды: набор на выходные", "Открытые объявления, CQB/лес, Москва +100км", "команды"),
        ShellWireframeRow("Игры: ночные сценарные", "Пт-Сб, ночные, сценарные, 20+ участников", "игры"),
        ShellWireframeRow("Маркет: приводы и оптика", "Москва/МО, AEG + optics, только активные", "маркет"),
    ),
    val recentApplyRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Поиск команд", "Применён фильтр \"Команды: набор на выходные\" 2 часа назад", "команды"),
        ShellWireframeRow("Барахолка", "Применён фильтр \"Маркет: приводы и оптика\" вчера", "маркет"),
    ),
)

sealed interface SavedFiltersAction {
    data object CycleDomain : SavedFiltersAction
    data object ToggleAutoApply : SavedFiltersAction
    data object OpenFilterDetailClicked : SavedFiltersAction
}

class SavedFiltersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SavedFiltersUiState())
    val uiState: StateFlow<SavedFiltersUiState> = _uiState.asStateFlow()

    fun onAction(action: SavedFiltersAction) {
        when (action) {
            SavedFiltersAction.CycleDomain -> {
                val current = _uiState.value
                val idx = current.domains.indexOf(current.selectedDomain)
                val next = current.domains[(idx + 1).mod(current.domains.size)]
                _uiState.value = current.copy(selectedDomain = next)
            }

            SavedFiltersAction.ToggleAutoApply -> {
                _uiState.value = _uiState.value.copy(autoApplyEnabled = !_uiState.value.autoApplyEnabled)
            }

            SavedFiltersAction.OpenFilterDetailClicked -> Unit
        }
    }
}

@Composable
fun SavedFiltersShellRoute(
    onOpenFilterDetail: () -> Unit = {},
    savedFiltersViewModel: SavedFiltersViewModel = viewModel(),
) {
    val uiState by savedFiltersViewModel.uiState.collectAsState()
    SavedFiltersShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SavedFiltersAction.OpenFilterDetailClicked -> onOpenFilterDetail()
                else -> savedFiltersViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun SavedFiltersShellScreen(
    uiState: SavedFiltersUiState,
    onAction: (SavedFiltersAction) -> Unit,
) {
    WireframePage(
        title = "Сохранённые фильтры",
        subtitle = "Каркас раздела сохранённых фильтров: пресеты поиска по разделам, быстрый запуск и авто-применение.",
        primaryActionLabel = "Создать новый фильтр (заглушка)",
    ) {
        WireframeSection(
            title = "Область и режим",
            subtitle = "Управление доменом фильтра и базовым поведением применения.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Область" to uiState.selectedDomain,
                    "Пресетов" to uiState.presetRows.size.toString(),
                    "Авто" to if (uiState.autoApplyEnabled) "Вкл" else "Выкл",
                ),
            )
            WireframeChipRow(
                labels = uiState.domains.map { if (it == uiState.selectedDomain) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Мои фильтры",
            subtitle = "Список пользовательских пресетов поиска для разных разделов.",
        ) {
            uiState.presetRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Недавно применялись",
            subtitle = "История запуска фильтров для быстрого повторного поиска.",
        ) {
            uiState.recentApplyRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка состояния и перехода в карточку фильтра.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(SavedFiltersAction.CycleDomain) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить область") }
                OutlinedButton(
                    onClick = { onAction(SavedFiltersAction.ToggleAutoApply) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить авто-применение") }
                OutlinedButton(
                    onClick = { onAction(SavedFiltersAction.OpenFilterDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть фильтр") }
            }
        }
    }
}

data class SavedFilterDetailUiState(
    val savedFilterId: String = "",
    val selectedSection: String = "Сводка",
    val sections: List<String> = listOf("Сводка", "Критерии", "Применение"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val criteriaRows: List<ShellWireframeRow> = emptyList(),
    val applyRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface SavedFilterDetailAction {
    data object CycleSection : SavedFilterDetailAction
}

class SavedFilterDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SavedFilterDetailUiState())
    val uiState: StateFlow<SavedFilterDetailUiState> = _uiState.asStateFlow()

    fun load(savedFilterId: String) {
        if (_uiState.value.savedFilterId == savedFilterId) return
        _uiState.value = SavedFilterDetailUiState(
            savedFilterId = savedFilterId,
            summaryRows = listOf(
                ShellWireframeRow("Название", "Команды: набор на выходные", "preset"),
                ShellWireframeRow("Раздел", "Команды", "scope"),
                ShellWireframeRow("Владелец", "Teiwaz_", "self"),
            ),
            criteriaRows = listOf(
                ShellWireframeRow("Статус набора", "Открытые", "status"),
                ShellWireframeRow("Формат", "CQB, лес", "format"),
                ShellWireframeRow("Гео", "Москва + 100 км", "geo"),
            ),
            applyRows = listOf(
                ShellWireframeRow("Где используется", "Поиск команд, лента набора", "usage"),
                ShellWireframeRow("Последний запуск", "Сегодня 21:12", "time"),
                ShellWireframeRow("Авто-применение", "Выключено", "auto"),
            ),
        )
    }

    fun onAction(action: SavedFilterDetailAction) {
        when (action) {
            SavedFilterDetailAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }
        }
    }
}

@Composable
fun SavedFilterDetailSkeletonRoute(
    savedFilterId: String,
    savedFilterDetailViewModel: SavedFilterDetailViewModel = viewModel(),
) {
    LaunchedEffect(savedFilterId) {
        savedFilterDetailViewModel.load(savedFilterId)
    }
    val uiState by savedFilterDetailViewModel.uiState.collectAsState()
    SavedFilterDetailSkeletonScreen(
        uiState = uiState,
        onAction = savedFilterDetailViewModel::onAction,
    )
}

@Composable
private fun SavedFilterDetailSkeletonScreen(
    uiState: SavedFilterDetailUiState,
    onAction: (SavedFilterDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка фильтра",
        subtitle = "Детали сохранённого фильтра. ID: ${uiState.savedFilterId}",
        primaryActionLabel = "Применить фильтр (заглушка)",
    ) {
        WireframeSection(
            title = "Разделы",
            subtitle = "Сводка, критерии и история применения фильтра.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Критериев" to uiState.criteriaRows.size.toString(),
                    "Использований" to uiState.applyRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
        }
        WireframeSection(title = "Сводка", subtitle = "Базовые свойства фильтра.") {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Критерии", subtitle = "Набор сохранённых критериев поиска.") {
            uiState.criteriaRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Применение", subtitle = "История и точки использования фильтра.") {
            uiState.applyRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring карточки фильтра.") {
            Button(
                onClick = { onAction(SavedFilterDetailAction.CycleSection) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел фильтра") }
        }
    }
}

data class DraftsUiState(
    val selectedType: String = "Все",
    val types: List<String> = listOf("Все", "Команды", "События", "Барахолка", "Попутчики"),
    val unsyncedOnly: Boolean = false,
    val draftRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Событие: Night Raid North", "Черновик сценария, изменён 15 мин назад", "event"),
        ShellWireframeRow("Объявление: M4A1 + 3 магазина", "Черновик барахолки, фото и цена заполнены", "market"),
        ShellWireframeRow("Попутчики: Маршрут на полигон Северный", "Черновик поездки, 2 свободных места", "ride"),
    ),
    val autosaveRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Автосохранение", "Интервал 30 сек, локально на устройстве", "30s"),
        ShellWireframeRow("Очередь синхронизации", "2 черновика ожидают сеть", "2"),
    ),
)

sealed interface DraftsAction {
    data object CycleType : DraftsAction
    data object ToggleUnsyncedOnly : DraftsAction
    data object OpenDraftDetailClicked : DraftsAction
}

class DraftsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DraftsUiState())
    val uiState: StateFlow<DraftsUiState> = _uiState.asStateFlow()

    fun onAction(action: DraftsAction) {
        when (action) {
            DraftsAction.CycleType -> {
                val current = _uiState.value
                val idx = current.types.indexOf(current.selectedType)
                val next = current.types[(idx + 1).mod(current.types.size)]
                _uiState.value = current.copy(selectedType = next)
            }

            DraftsAction.ToggleUnsyncedOnly -> {
                _uiState.value = _uiState.value.copy(unsyncedOnly = !_uiState.value.unsyncedOnly)
            }

            DraftsAction.OpenDraftDetailClicked -> Unit
        }
    }
}

@Composable
fun DraftsShellRoute(
    onOpenDraftDetail: () -> Unit = {},
    draftsViewModel: DraftsViewModel = viewModel(),
) {
    val uiState by draftsViewModel.uiState.collectAsState()
    DraftsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                DraftsAction.OpenDraftDetailClicked -> onOpenDraftDetail()
                else -> draftsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun DraftsShellScreen(
    uiState: DraftsUiState,
    onAction: (DraftsAction) -> Unit,
) {
    WireframePage(
        title = "Черновики",
        subtitle = "Каркас раздела черновиков: автосохранение, очередь синхронизации и ручная публикация.",
        primaryActionLabel = "Создать черновик (заглушка)",
    ) {
        WireframeSection(
            title = "Тип и режим",
            subtitle = "Фильтрация черновиков по сущности и статусу синхронизации.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Тип" to uiState.selectedType,
                    "Черновиков" to uiState.draftRows.size.toString(),
                    "Несинхр." to if (uiState.unsyncedOnly) "Да" else "Нет",
                ),
            )
            WireframeChipRow(
                labels = uiState.types.map { if (it == uiState.selectedType) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Черновики",
            subtitle = "Локальные и ожидающие публикации материалы пользователя.",
        ) {
            uiState.draftRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Автосохранение и синхронизация",
            subtitle = "Техническое состояние сохранения и очереди отправки.",
        ) {
            uiState.autosaveRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring и перехода в карточку черновика.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(DraftsAction.CycleType) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить тип") }
                OutlinedButton(
                    onClick = { onAction(DraftsAction.ToggleUnsyncedOnly) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Только несинхронизированные") }
                OutlinedButton(
                    onClick = { onAction(DraftsAction.OpenDraftDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть черновик") }
            }
        }
    }
}

data class DraftDetailUiState(
    val draftId: String = "",
    val selectedTab: String = "Содержимое",
    val tabs: List<String> = listOf("Содержимое", "Автосохранение", "Публикация"),
    val contentRows: List<ShellWireframeRow> = emptyList(),
    val autosaveRows: List<ShellWireframeRow> = emptyList(),
    val publishRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface DraftDetailAction {
    data object CycleTab : DraftDetailAction
}

class DraftDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DraftDetailUiState())
    val uiState: StateFlow<DraftDetailUiState> = _uiState.asStateFlow()

    fun load(draftId: String) {
        if (_uiState.value.draftId == draftId) return
        _uiState.value = DraftDetailUiState(
            draftId = draftId,
            contentRows = listOf(
                ShellWireframeRow("Тип", "Событие / игра", "event"),
                ShellWireframeRow("Статус заполнения", "Название, дата, полигон заполнены", "75%"),
                ShellWireframeRow("Последняя правка", "Сегодня 22:41", "edit"),
            ),
            autosaveRows = listOf(
                ShellWireframeRow("Локальная версия", "v12 на устройстве", "local"),
                ShellWireframeRow("Серверная версия", "не отправлялась", "remote"),
                ShellWireframeRow("Конфликты", "Не обнаружены", "ok"),
            ),
            publishRows = listOf(
                ShellWireframeRow("Проверки", "Заполнить лимит участников и контакты", "todo"),
                ShellWireframeRow("Видимость", "Черновик (только автор)", "private"),
                ShellWireframeRow("Следующее действие", "Опубликовать или сохранить черновик", "next"),
            ),
        )
    }

    fun onAction(action: DraftDetailAction) {
        when (action) {
            DraftDetailAction.CycleTab -> {
                val current = _uiState.value
                val idx = current.tabs.indexOf(current.selectedTab)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
fun DraftDetailSkeletonRoute(
    draftId: String,
    draftDetailViewModel: DraftDetailViewModel = viewModel(),
) {
    LaunchedEffect(draftId) {
        draftDetailViewModel.load(draftId)
    }
    val uiState by draftDetailViewModel.uiState.collectAsState()
    DraftDetailSkeletonScreen(
        uiState = uiState,
        onAction = draftDetailViewModel::onAction,
    )
}

@Composable
private fun DraftDetailSkeletonScreen(
    uiState: DraftDetailUiState,
    onAction: (DraftDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка черновика",
        subtitle = "Детали черновика, версии автосохранения и готовность к публикации. ID: ${uiState.draftId}",
        primaryActionLabel = "Открыть редактор (заглушка)",
    ) {
        WireframeSection(
            title = "Вкладки",
            subtitle = "Содержимое, автосохранение и публикация.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Вкладка" to uiState.selectedTab,
                    "Контент" to uiState.contentRows.size.toString(),
                    "Проверки" to uiState.publishRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(title = "Содержимое", subtitle = "Данные черновика и заполненность.") {
            uiState.contentRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Автосохранение", subtitle = "Версии и состояние синхронизации.") {
            uiState.autosaveRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Публикация", subtitle = "Проверки и готовность к публикации.") {
            uiState.publishRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring карточки черновика.") {
            Button(
                onClick = { onAction(DraftDetailAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить вкладку черновика") }
        }
    }
}
