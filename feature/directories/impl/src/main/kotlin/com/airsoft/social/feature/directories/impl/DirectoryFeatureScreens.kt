package com.airsoft.social.feature.directories.impl

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
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Wide-first shell pages: big sections first, data/detail polish later.

data class GameCalendarUiState(
    val periodMode: String = "Неделя",
    val onlyMyGames: Boolean = false,
    val selectedFormat: String = "Все",
    val formatChips: List<String> = listOf("Все", "Тренировки", "Сценарные", "CQB", "Турниры"),
    val upcomingRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North", "Сб 19:00 | 42 игрока | Полигон Северный", "В пути"),
        ShellWireframeRow("CQB Sunday Mix", "Вс 11:00 | 18 игроков | Арена Bunker", "Запись"),
        ShellWireframeRow("Тренировка отделения [EW]", "Ср 20:00 | 12 игроков | Лесной участок", "Команда"),
    ),
    val myScheduleRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Подтверждение участия", "Night Raid North — до пятницы 18:00", "нужно"),
        ShellWireframeRow("Снаряжение", "Проверить аккумы, медпак, рация", "чек-лист"),
        ShellWireframeRow("Логистика", "2 свободных места в машине", "полезно"),
    ),
    val organizerRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Слоты ролей", "Медики 2/3, саппорт 1/2, снайпер 1/1", "контроль"),
        ShellWireframeRow("Брифинг", "Черновик брифинга и схема входа на полигон", "редактор"),
    ),
)

sealed interface GameCalendarAction {
    data object CyclePeriodMode : GameCalendarAction
    data object ToggleOnlyMyGames : GameCalendarAction
    data class SelectFormat(val format: String) : GameCalendarAction
    data object OpenEventsPageClicked : GameCalendarAction
    data object OpenGameDetailClicked : GameCalendarAction
    data object OpenGameEditorClicked : GameCalendarAction
    data object OpenGameLogisticsClicked : GameCalendarAction
}

class GameCalendarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameCalendarUiState())
    val uiState: StateFlow<GameCalendarUiState> = _uiState.asStateFlow()

    fun onAction(action: GameCalendarAction) {
        when (action) {
            GameCalendarAction.CyclePeriodMode -> {
                val next = when (_uiState.value.periodMode) {
                    "Неделя" -> "Месяц"
                    "Месяц" -> "Список"
                    else -> "Неделя"
                }
                _uiState.value = _uiState.value.copy(periodMode = next)
            }

            GameCalendarAction.ToggleOnlyMyGames -> {
                _uiState.value = _uiState.value.copy(onlyMyGames = !_uiState.value.onlyMyGames)
            }

            is GameCalendarAction.SelectFormat -> {
                _uiState.value = _uiState.value.copy(selectedFormat = action.format)
            }

            GameCalendarAction.OpenEventsPageClicked -> Unit
            GameCalendarAction.OpenGameDetailClicked -> Unit
            GameCalendarAction.OpenGameEditorClicked -> Unit
            GameCalendarAction.OpenGameLogisticsClicked -> Unit
        }
    }
}

@Composable
fun GameCalendarShellRoute(
    onOpenEventsPage: () -> Unit = {},
    onOpenGameDetail: () -> Unit = {},
    onOpenGameEditor: () -> Unit = {},
    onOpenGameLogistics: () -> Unit = {},
    gameCalendarViewModel: GameCalendarViewModel = viewModel(),
) {
    val uiState by gameCalendarViewModel.uiState.collectAsState()
    GameCalendarShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                GameCalendarAction.OpenEventsPageClicked -> onOpenEventsPage()
                GameCalendarAction.OpenGameDetailClicked -> onOpenGameDetail()
                GameCalendarAction.OpenGameEditorClicked -> onOpenGameEditor()
                GameCalendarAction.OpenGameLogisticsClicked -> onOpenGameLogistics()
                else -> gameCalendarViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun GameCalendarShellScreen(
    uiState: GameCalendarUiState,
    onAction: (GameCalendarAction) -> Unit,
) {
    WireframePage(
        title = "Календарь игр",
        subtitle = "Большой каркас календаря: расписание, участие, логистика, слоты ролей и быстрый переход в события.",
        primaryActionLabel = "Создать игру (заглушка)",
    ) {
        WireframeSection(
            title = "Режим просмотра",
            subtitle = "Переключение календаря: неделя / месяц / список и локальные фильтры.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.periodMode,
                    "Мои игры" to if (uiState.onlyMyGames) "Да" else "Нет",
                    "Формат" to uiState.selectedFormat,
                ),
            )
            WireframeChipRow(
                labels = uiState.formatChips.map { if (it == uiState.selectedFormat) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Ближайшие игры",
            subtitle = "Ключевые матчи/игры, тайминги и статус участия.",
        ) {
            uiState.upcomingRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Мой план на игру",
            subtitle = "Чек-листы участия, логистика и подготовка по ролям.",
        ) {
            uiState.myScheduleRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Инструменты организатора",
            subtitle = "Слоты, брифинг, контроль состава и дедлайны подтверждений.",
        ) {
            uiState.organizerRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка wiring страницы и перехода в раздел событий.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(GameCalendarAction.CyclePeriodMode) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить режим календаря") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarAction.ToggleOnlyMyGames) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Только мои игры") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedFormat == "Все") "Сценарные" else "Все"
                        onAction(GameCalendarAction.SelectFormat(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить формат") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarAction.OpenEventsPageClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть раздел событий") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarAction.OpenGameDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть карточку игры") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarAction.OpenGameEditorClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть редактор игры") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarAction.OpenGameLogisticsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть логистику игры") }
            }
        }
    }
}

data class RideShareUiState(
    val selectedDirection: String = "На игру",
    val onlyMyRegion: Boolean = true,
    val selectedSeatMode: String = "Все",
    val seatModeChips: List<String> = listOf("Все", "Ищу место", "Есть места", "Трансфер"),
    val offerRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North • Teiwaz_", "Сб 17:30 | 3 места | Москва → Полигон Северный", "есть места"),
        ShellWireframeRow("CQB Sunday Mix • Raven", "Вс 09:40 | 1 место | СПб → Bunker Arena", "1 место"),
        ShellWireframeRow("Тренировка [EW] • Ghost", "Ср 19:10 | трансфер с метро + багаж", "трансфер"),
    ),
    val requestRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Нужен водитель на сценарку", "Пт/Сб | 2 человека + снаряга | Москва", "запрос"),
        ShellWireframeRow("Ищу место до полигона", "Вс 08:00 | Казань | 1 игрок", "ищу"),
    ),
    val logisticsRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Точки сбора", "Метро, парковки, КПП полигона и удобные гео-точки", "geo"),
        ShellWireframeRow("Багаж/снаряга", "Отдельные слоты под крупные кейсы и общий лимит", "bag"),
        ShellWireframeRow("Топливо/взнос", "Деление расходов и быстрый расчёт по участникам", "rub"),
    ),
)

sealed interface RideShareAction {
    data object ToggleDirection : RideShareAction
    data object ToggleOnlyMyRegion : RideShareAction
    data class SelectSeatMode(val mode: String) : RideShareAction
    data object OpenCalendarClicked : RideShareAction
    data object OpenPolygonsClicked : RideShareAction
    data object OpenTripDetailClicked : RideShareAction
    data object OpenCreateTripClicked : RideShareAction
    data object OpenMyRouteClicked : RideShareAction
}

class RideShareViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RideShareUiState())
    val uiState: StateFlow<RideShareUiState> = _uiState.asStateFlow()

    fun onAction(action: RideShareAction) {
        when (action) {
            RideShareAction.ToggleDirection -> {
                val next = if (_uiState.value.selectedDirection == "На игру") "Обратно" else "На игру"
                _uiState.value = _uiState.value.copy(selectedDirection = next)
            }

            RideShareAction.ToggleOnlyMyRegion -> {
                _uiState.value = _uiState.value.copy(onlyMyRegion = !_uiState.value.onlyMyRegion)
            }

            is RideShareAction.SelectSeatMode -> {
                _uiState.value = _uiState.value.copy(selectedSeatMode = action.mode)
            }

            RideShareAction.OpenCalendarClicked -> Unit
            RideShareAction.OpenPolygonsClicked -> Unit
            RideShareAction.OpenTripDetailClicked -> Unit
            RideShareAction.OpenCreateTripClicked -> Unit
            RideShareAction.OpenMyRouteClicked -> Unit
        }
    }
}

@Composable
fun RideShareShellRoute(
    onOpenCalendar: () -> Unit = {},
    onOpenPolygons: () -> Unit = {},
    onOpenTripDetail: () -> Unit = {},
    onOpenCreateTrip: () -> Unit = {},
    onOpenMyRoute: () -> Unit = {},
    rideShareViewModel: RideShareViewModel = viewModel(),
) {
    val uiState by rideShareViewModel.uiState.collectAsState()
    RideShareShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                RideShareAction.OpenCalendarClicked -> onOpenCalendar()
                RideShareAction.OpenPolygonsClicked -> onOpenPolygons()
                RideShareAction.OpenTripDetailClicked -> onOpenTripDetail()
                RideShareAction.OpenCreateTripClicked -> onOpenCreateTrip()
                RideShareAction.OpenMyRouteClicked -> onOpenMyRoute()
                else -> rideShareViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun RideShareShellScreen(
    uiState: RideShareUiState,
    onAction: (RideShareAction) -> Unit,
) {
    WireframePage(
        title = "Попутчики",
        subtitle = "Большой каркас поездок: предложения мест, запросы, точки сбора, логистика и привязка к играм/полигонам.",
        primaryActionLabel = "Создать поездку (заглушка)",
    ) {
        WireframeSection(
            title = "Локальный поиск и режим",
            subtitle = "Контекстный поиск только по поездкам: направление, наличие мест, регион и дата/игра.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Направление" to uiState.selectedDirection,
                    "Мой регион" to if (uiState.onlyMyRegion) "Да" else "Нет",
                    "Места" to uiState.selectedSeatMode,
                ),
            )
            WireframeChipRow(
                labels = uiState.seatModeChips.map { chip ->
                    if (chip == uiState.selectedSeatMode) "[$chip]" else chip
                },
            )
        }
        WireframeSection(
            title = "Предложения поездок",
            subtitle = "Водители и участники с местами в машине на ближайшие игры.",
        ) {
            uiState.offerRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Запросы поездок",
            subtitle = "Игроки и команды, которым нужен трансфер/места до полигона.",
        ) {
            uiState.requestRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Логистика",
            subtitle = "Точки сбора, багаж, расходы на топливо и организационные пометки.",
        ) {
            uiState.logisticsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка wiring страницы и переходов в смежные разделы.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(RideShareAction.ToggleDirection) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить направление") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.ToggleOnlyMyRegion) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить регион") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedSeatMode == "Все") "Есть места" else "Все"
                        onAction(RideShareAction.SelectSeatMode(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить режим мест") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.OpenCalendarClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть календарь игр") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.OpenPolygonsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть полигоны") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.OpenTripDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть карточку поездки") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.OpenCreateTripClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть создание поездки") }
                OutlinedButton(
                    onClick = { onAction(RideShareAction.OpenMyRouteClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть мой маршрут") }
            }
        }
    }
}

data class PolygonsUiState(
    val selectedRegion: String = "Москва и область",
    val nightGamesEnabled: Boolean = true,
    val selectedTerrain: String = "Все",
    val terrainChips: List<String> = listOf("Все", "Лес", "CQB", "Заброшка", "Микс"),
    val polygonRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Полигон Северный", "Лес/поля | 35 км | Парковка, хоззона, вода", "4.8"),
        ShellWireframeRow("Bunker Arena", "CQB | 12 км | Свет, прокат, таймеры", "4.6"),
        ShellWireframeRow("Старая База", "Заброшка | 58 км | Ночные игры по записи", "4.5"),
    ),
    val rulesRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Допуски и FPS", "Лимиты по классам приводов и дистанциям", "важно"),
        ShellWireframeRow("Парковка/въезд", "Окно въезда, схема шлагбаума, контакты", "логистика"),
        ShellWireframeRow("Связь", "Рекомендуемые каналы / резервные частоты", "рация"),
    ),
)

sealed interface PolygonsAction {
    data object CycleRegion : PolygonsAction
    data object ToggleNightGames : PolygonsAction
    data class SelectTerrain(val terrain: String) : PolygonsAction
    data object OpenCalendarClicked : PolygonsAction
    data object OpenPolygonDetailClicked : PolygonsAction
    data object OpenPolygonEditorClicked : PolygonsAction
    data object OpenPolygonRulesMapClicked : PolygonsAction
}

class PolygonsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PolygonsUiState())
    val uiState: StateFlow<PolygonsUiState> = _uiState.asStateFlow()

    fun onAction(action: PolygonsAction) {
        when (action) {
            PolygonsAction.CycleRegion -> {
                val next = when (_uiState.value.selectedRegion) {
                    "Москва и область" -> "Санкт-Петербург"
                    "Санкт-Петербург" -> "Казань"
                    else -> "Москва и область"
                }
                _uiState.value = _uiState.value.copy(selectedRegion = next)
            }

            PolygonsAction.ToggleNightGames -> {
                _uiState.value = _uiState.value.copy(nightGamesEnabled = !_uiState.value.nightGamesEnabled)
            }

            is PolygonsAction.SelectTerrain -> {
                _uiState.value = _uiState.value.copy(selectedTerrain = action.terrain)
            }

            PolygonsAction.OpenCalendarClicked -> Unit
            PolygonsAction.OpenPolygonDetailClicked -> Unit
            PolygonsAction.OpenPolygonEditorClicked -> Unit
            PolygonsAction.OpenPolygonRulesMapClicked -> Unit
        }
    }
}

@Composable
fun PolygonsShellRoute(
    onOpenCalendar: () -> Unit = {},
    onOpenPolygonDetail: () -> Unit = {},
    onOpenPolygonEditor: () -> Unit = {},
    onOpenPolygonRulesMap: () -> Unit = {},
    polygonsViewModel: PolygonsViewModel = viewModel(),
) {
    val uiState by polygonsViewModel.uiState.collectAsState()
    PolygonsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                PolygonsAction.OpenCalendarClicked -> onOpenCalendar()
                PolygonsAction.OpenPolygonDetailClicked -> onOpenPolygonDetail()
                PolygonsAction.OpenPolygonEditorClicked -> onOpenPolygonEditor()
                PolygonsAction.OpenPolygonRulesMapClicked -> onOpenPolygonRulesMap()
                else -> polygonsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun PolygonsShellScreen(
    uiState: PolygonsUiState,
    onAction: (PolygonsAction) -> Unit,
) {
    WireframePage(
        title = "Полигоны",
        subtitle = "Большой каркас каталога полигонов: поиск площадок, правила допуска, инфраструктура и привязка к календарю игр.",
        primaryActionLabel = "Добавить полигон (заглушка)",
    ) {
        WireframeSection(
            title = "Локальный поиск и фильтры",
            subtitle = "Контекстный поиск по полигонам: регион, тип местности, ночные игры, инфраструктура.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Регион" to uiState.selectedRegion,
                    "Ночные" to if (uiState.nightGamesEnabled) "Да" else "Нет",
                    "Тип" to uiState.selectedTerrain,
                ),
            )
            WireframeChipRow(
                labels = uiState.terrainChips.map { if (it == uiState.selectedTerrain) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Каталог полигонов",
            subtitle = "Карточки площадок с типом местности, удалённостью и рейтингом.",
        ) {
            uiState.polygonRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Правила и инфраструктура",
            subtitle = "FPS, парковка, въезд, связь, техзона и бытовая информация.",
        ) {
            uiState.rulesRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state/action wiring и связи с календарём игр.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(PolygonsAction.CycleRegion) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить регион") }
                OutlinedButton(
                    onClick = { onAction(PolygonsAction.ToggleNightGames) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить ночные игры") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedTerrain == "Все") "Лес" else "Все"
                        onAction(PolygonsAction.SelectTerrain(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить тип площадки") }
                OutlinedButton(
                    onClick = { onAction(PolygonsAction.OpenCalendarClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть календарь игр") }
                OutlinedButton(
                    onClick = { onAction(PolygonsAction.OpenPolygonDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть карточку полигона") }
                OutlinedButton(
                    onClick = { onAction(PolygonsAction.OpenPolygonEditorClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть редактор полигона") }
                OutlinedButton(
                    onClick = { onAction(PolygonsAction.OpenPolygonRulesMapClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть правила и карту") }
            }
        }
    }
}

data class ShopsUiState(
    val selectedCategory: String = "Все",
    val categories: List<String> = listOf("Все", "Приводы", "Снаряга", "Оптика", "Расходники", "Прокат"),
    val selectedCity: String = "Москва",
    val shopRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Airsoft Hub", "Приводы, тюнинг, расходники | Москва | Самовывоз + доставка", "4.9"),
        ShellWireframeRow("CQB Store", "Экипировка и защита | СПб | Есть прокат на игры", "4.7"),
        ShellWireframeRow("Field Supply", "Газ, шары, батареи | Казань | Быстрые сборы", "4.5"),
    ),
    val promoRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Скидки к выходным", "Наборы для игры + расходники со скидкой", "акция"),
        ShellWireframeRow("Партнёрские магазины", "Проверенные продавцы с рейтингом сообщества", "партнёр"),
    ),
)

sealed interface ShopsAction {
    data class SelectCategory(val category: String) : ShopsAction
    data object CycleCity : ShopsAction
    data object OpenMarketplaceClicked : ShopsAction
    data object OpenShopDetailClicked : ShopsAction
    data object OpenShopEditorClicked : ShopsAction
}

class ShopsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShopsUiState())
    val uiState: StateFlow<ShopsUiState> = _uiState.asStateFlow()

    fun onAction(action: ShopsAction) {
        when (action) {
            is ShopsAction.SelectCategory -> {
                _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            }

            ShopsAction.CycleCity -> {
                val next = when (_uiState.value.selectedCity) {
                    "Москва" -> "Санкт-Петербург"
                    "Санкт-Петербург" -> "Казань"
                    else -> "Москва"
                }
                _uiState.value = _uiState.value.copy(selectedCity = next)
            }

            ShopsAction.OpenMarketplaceClicked -> Unit
            ShopsAction.OpenShopDetailClicked -> Unit
            ShopsAction.OpenShopEditorClicked -> Unit
        }
    }
}

@Composable
fun ShopsShellRoute(
    onOpenMarketplace: () -> Unit = {},
    onOpenShopDetail: () -> Unit = {},
    onOpenShopEditor: () -> Unit = {},
    shopsViewModel: ShopsViewModel = viewModel(),
) {
    val uiState by shopsViewModel.uiState.collectAsState()
    ShopsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ShopsAction.OpenMarketplaceClicked -> onOpenMarketplace()
                ShopsAction.OpenShopDetailClicked -> onOpenShopDetail()
                ShopsAction.OpenShopEditorClicked -> onOpenShopEditor()
                else -> shopsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ShopsShellScreen(
    uiState: ShopsUiState,
    onAction: (ShopsAction) -> Unit,
) {
    WireframePage(
        title = "Магазины",
        subtitle = "Большой каркас каталога магазинов: категории, города, партнёрские точки, акции и связка с барахолкой.",
        primaryActionLabel = "Добавить магазин (заглушка)",
    ) {
        WireframeSection(
            title = "Локальный поиск и категории",
            subtitle = "Поиск только по магазинам: категория, город, наличие, партнёрские статусы.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Категория" to uiState.selectedCategory,
                    "Город" to uiState.selectedCity,
                    "Выдача" to "Магазины",
                ),
            )
            WireframeChipRow(
                labels = uiState.categories.map { if (it == uiState.selectedCategory) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Каталог магазинов",
            subtitle = "Карточки магазинов, ассортимента и форматов получения товара.",
        ) {
            uiState.shopRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Промо и партнёры",
            subtitle = "Акции, партнёрские предложения и подборки перед играми.",
        ) {
            uiState.promoRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка поиска по магазинам и перехода в барахолку.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ShopsAction.CycleCity) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить город") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == "Все") "Снаряга" else "Все"
                        onAction(ShopsAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить категорию") }
                OutlinedButton(
                    onClick = { onAction(ShopsAction.OpenMarketplaceClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть барахолку") }
                OutlinedButton(
                    onClick = { onAction(ShopsAction.OpenShopDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть карточку магазина") }
                OutlinedButton(
                    onClick = { onAction(ShopsAction.OpenShopEditorClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть редактор магазина") }
            }
        }
    }
}

data class ServicesUiState(
    val selectedCategory: String = "Все",
    val onlyVerified: Boolean = true,
    val categories: List<String> = listOf("Все", "Тюнинг", "Ремонт", "Инструкторы", "Судьи", "Фото/Видео", "Прокат"),
    val serviceRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Мастерская North Tech", "Ремонт/тюнинг AEG, диагностика, настройка хопа", "4.9"),
        ShellWireframeRow("Medic Training Lab", "Подготовка медиков и командные тренировки", "4.8"),
        ShellWireframeRow("Field Ref Team", "Судейство игр и контроль сценарных событий", "4.7"),
    ),
    val requestRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Нужен техмастер", "Перед игрой — проверить 3 привода до субботы", "запрос"),
        ShellWireframeRow("Фотограф на игру", "Сценарка на 80+ игроков, полный день", "тендер"),
    ),
)

sealed interface ServicesAction {
    data class SelectCategory(val category: String) : ServicesAction
    data object ToggleOnlyVerified : ServicesAction
    data object OpenProfileClicked : ServicesAction
    data object OpenServiceDetailClicked : ServicesAction
    data object OpenServiceEditorClicked : ServicesAction
}

class ServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    fun onAction(action: ServicesAction) {
        when (action) {
            is ServicesAction.SelectCategory -> {
                _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            }

            ServicesAction.ToggleOnlyVerified -> {
                _uiState.value = _uiState.value.copy(onlyVerified = !_uiState.value.onlyVerified)
            }

            ServicesAction.OpenProfileClicked -> Unit
            ServicesAction.OpenServiceDetailClicked -> Unit
            ServicesAction.OpenServiceEditorClicked -> Unit
        }
    }
}

@Composable
fun ServicesShellRoute(
    onOpenProfile: () -> Unit = {},
    onOpenServiceDetail: () -> Unit = {},
    onOpenServiceEditor: () -> Unit = {},
    servicesViewModel: ServicesViewModel = viewModel(),
) {
    val uiState by servicesViewModel.uiState.collectAsState()
    ServicesShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ServicesAction.OpenProfileClicked -> onOpenProfile()
                ServicesAction.OpenServiceDetailClicked -> onOpenServiceDetail()
                ServicesAction.OpenServiceEditorClicked -> onOpenServiceEditor()
                else -> servicesViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ServicesShellScreen(
    uiState: ServicesUiState,
    onAction: (ServicesAction) -> Unit,
) {
    WireframePage(
        title = "Услуги",
        subtitle = "Большой каркас услуг: ремонт, тюнинг, инструкторы, судьи, медиа и заявки от игроков/команд.",
        primaryActionLabel = "Создать заявку (заглушка)",
    ) {
        WireframeSection(
            title = "Локальный поиск услуг",
            subtitle = "Поиск только по услугам: категория, подтверждённые исполнители, рейтинг и доступность.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Категория" to uiState.selectedCategory,
                    "Проверенные" to if (uiState.onlyVerified) "Да" else "Нет",
                    "Выдача" to "Исполнители",
                ),
            )
            WireframeChipRow(
                labels = uiState.categories.map { if (it == uiState.selectedCategory) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Исполнители",
            subtitle = "Каталог мастерских, инструкторов и сервисных специалистов.",
        ) {
            uiState.serviceRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Заявки и запросы",
            subtitle = "Площадка запросов от игроков и команд на услуги.",
        ) {
            uiState.requestRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка контекстного поиска услуг и перехода к профилю/портфолио.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ServicesAction.ToggleOnlyVerified) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить фильтр проверенных") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == "Все") "Ремонт" else "Все"
                        onAction(ServicesAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить категорию") }
                OutlinedButton(
                    onClick = { onAction(ServicesAction.OpenProfileClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть профиль исполнителя") }
                OutlinedButton(
                    onClick = { onAction(ServicesAction.OpenServiceDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть карточку услуги") }
                OutlinedButton(
                    onClick = { onAction(ServicesAction.OpenServiceEditorClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть редактор услуги") }
            }
        }
    }
}

data class RideShareTripDetailUiState(
    val rideId: String = "",
    val direction: String = "На игру",
    val seatsInfo: String = "3/4",
    val status: String = "Сбор",
    val routeRows: List<ShellWireframeRow> = emptyList(),
    val passengerRows: List<ShellWireframeRow> = emptyList(),
    val notesRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface RideShareTripDetailAction {
    data object ToggleDirection : RideShareTripDetailAction
    data object ToggleStatus : RideShareTripDetailAction
}

class RideShareTripDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RideShareTripDetailUiState())
    val uiState: StateFlow<RideShareTripDetailUiState> = _uiState.asStateFlow()

    fun load(rideId: String) {
        if (_uiState.value.rideId == rideId) return
        _uiState.value = RideShareTripDetailUiState(
            rideId = rideId,
            direction = if (rideId.contains("return")) "Обратно" else "На игру",
            seatsInfo = if (rideId.contains("cqb")) "1/3" else "3/4",
            status = if (rideId.contains("draft")) "Черновик" else "Сбор",
            routeRows = listOf(
                ShellWireframeRow("Точка сбора", "м. Савеловская, парковка у ТЦ", "17:30"),
                ShellWireframeRow("Промежуточная точка", "АЗС на МКАД, встреча команды", "18:05"),
                ShellWireframeRow("Прибытие", "Полигон Северный / КПП", "19:00"),
            ),
            passengerRows = listOf(
                ShellWireframeRow("Teiwaz_", "Водитель | 1 кейс + 1 рюкзак", "ok"),
                ShellWireframeRow("Ghost", "Пассажир | 1 привод", "подтвержден"),
                ShellWireframeRow("Raven", "Пассажир | без багажа", "ожидание"),
            ),
            notesRows = listOf(
                ShellWireframeRow("Топливо", "Делим расходы поровну между пассажирами", "руб"),
                ShellWireframeRow("Связь", "Канал рации на сборе и резервный номер", "link"),
                ShellWireframeRow("План Б", "При опоздании — прямой заезд на полигон", "plan"),
            ),
        )
    }

    fun onAction(action: RideShareTripDetailAction) {
        when (action) {
            RideShareTripDetailAction.ToggleDirection -> {
                val next = if (_uiState.value.direction == "На игру") "Обратно" else "На игру"
                _uiState.value = _uiState.value.copy(direction = next)
            }

            RideShareTripDetailAction.ToggleStatus -> {
                val next = when (_uiState.value.status) {
                    "Сбор" -> "В пути"
                    "В пути" -> "Завершено"
                    else -> "Сбор"
                }
                _uiState.value = _uiState.value.copy(status = next)
            }
        }
    }
}

@Composable
fun RideShareTripDetailSkeletonRoute(
    rideId: String,
    rideShareTripDetailViewModel: RideShareTripDetailViewModel = viewModel(),
) {
    LaunchedEffect(rideId) {
        rideShareTripDetailViewModel.load(rideId)
    }
    val uiState by rideShareTripDetailViewModel.uiState.collectAsState()
    RideShareTripDetailSkeletonScreen(
        uiState = uiState,
        onAction = rideShareTripDetailViewModel::onAction,
    )
}

@Composable
private fun RideShareTripDetailSkeletonScreen(
    uiState: RideShareTripDetailUiState,
    onAction: (RideShareTripDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка поездки",
        subtitle = "Каркас деталей поездки: маршрут, участники, места, логистика и статусы. ID: ${uiState.rideId}",
        primaryActionLabel = "Открыть чат поездки (заглушка)",
    ) {
        WireframeSection(
            title = "Сводка",
            subtitle = "Основные параметры поездки и текущий статус.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Направление" to uiState.direction,
                    "Места" to uiState.seatsInfo,
                    "Статус" to uiState.status,
                ),
            )
        }
        WireframeSection(
            title = "Маршрут",
            subtitle = "Точки сбора, промежуточные остановки и ETA.",
        ) {
            uiState.routeRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Участники",
            subtitle = "Водитель, пассажиры, статус подтверждения и багаж.",
        ) {
            uiState.passengerRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Пометки",
            subtitle = "Расходы, связь и организационные заметки.",
        ) {
            uiState.notesRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка переходов и state wiring.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(RideShareTripDetailAction.ToggleStatus) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить статус поездки") }
                OutlinedButton(
                    onClick = { onAction(RideShareTripDetailAction.ToggleDirection) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить направление") }
            }
        }
    }
}

data class RideShareTripEditorUiState(
    val editorMode: EditorMode = EditorMode.Create,
    val editorRefId: String = "",
    val direction: String = "На игру",
    val seatCount: Int = 3,
    val fuelSplitEnabled: Boolean = true,
    val selectedPickupZone: String = "Север",
    val pickupZoneChips: List<String> = listOf("Север", "Юг", "Восток", "Запад", "Центр"),
)

sealed interface RideShareTripEditorAction {
    data object ToggleDirection : RideShareTripEditorAction
    data object IncrementSeats : RideShareTripEditorAction
    data object DecrementSeats : RideShareTripEditorAction
    data object ToggleFuelSplit : RideShareTripEditorAction
    data class SelectPickupZone(val zone: String) : RideShareTripEditorAction
}

class RideShareTripEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RideShareTripEditorUiState())
    val uiState: StateFlow<RideShareTripEditorUiState> = _uiState.asStateFlow()

    fun load(editorMode: EditorMode, editorRefId: String) {
        val current = _uiState.value
        if (current.editorMode == editorMode && current.editorRefId == editorRefId) return
        _uiState.value = current.copy(
            editorMode = editorMode,
            editorRefId = editorRefId,
            direction = if (editorMode == EditorMode.Edit) "Обратно" else "На игру",
            seatCount = if (editorMode == EditorMode.Edit) 2 else 3,
            fuelSplitEnabled = editorMode != EditorMode.Draft,
        )
    }

    fun onAction(action: RideShareTripEditorAction) {
        when (action) {
            RideShareTripEditorAction.ToggleDirection -> {
                val next = if (_uiState.value.direction == "На игру") "Обратно" else "На игру"
                _uiState.value = _uiState.value.copy(direction = next)
            }

            RideShareTripEditorAction.IncrementSeats -> {
                _uiState.value = _uiState.value.copy(seatCount = (_uiState.value.seatCount + 1).coerceAtMost(8))
            }

            RideShareTripEditorAction.DecrementSeats -> {
                _uiState.value = _uiState.value.copy(seatCount = (_uiState.value.seatCount - 1).coerceAtLeast(1))
            }

            RideShareTripEditorAction.ToggleFuelSplit -> {
                _uiState.value = _uiState.value.copy(fuelSplitEnabled = !_uiState.value.fuelSplitEnabled)
            }

            is RideShareTripEditorAction.SelectPickupZone -> {
                _uiState.value = _uiState.value.copy(selectedPickupZone = action.zone)
            }
        }
    }
}

@Composable
fun RideShareTripEditorSkeletonRoute(
    editorMode: EditorMode,
    editorRefId: String,
    rideShareTripEditorViewModel: RideShareTripEditorViewModel = viewModel(),
) {
    LaunchedEffect(editorMode, editorRefId) {
        rideShareTripEditorViewModel.load(editorMode, editorRefId)
    }
    val uiState by rideShareTripEditorViewModel.uiState.collectAsState()
    RideShareTripEditorSkeletonScreen(
        uiState = uiState,
        onAction = rideShareTripEditorViewModel::onAction,
    )
}

@Composable
private fun RideShareTripEditorSkeletonScreen(
    uiState: RideShareTripEditorUiState,
    onAction: (RideShareTripEditorAction) -> Unit,
) {
    WireframePage(
        title = "Редактор поездки",
        subtitle = "Каркас создания/редактирования поездки. Режим: ${uiState.editorMode.label} | Объект: ${uiState.editorRefId}",
        primaryActionLabel = "Сохранить поездку (заглушка)",
    ) {
        WireframeSection(
            title = "Основные параметры",
            subtitle = "Направление, количество мест и схема компенсации топлива.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.editorMode.label,
                    "Направление" to uiState.direction,
                    "Мест" to uiState.seatCount.toString(),
                ),
            )
            WireframeItemRow(
                "Топливо / взнос",
                "Делить расходы между пассажирами",
                if (uiState.fuelSplitEnabled) "вкл" else "выкл",
            )
        }
        WireframeSection(
            title = "Точки сбора",
            subtitle = "Предзаготовка выбора района/зоны посадки.",
        ) {
            WireframeChipRow(
                labels = uiState.pickupZoneChips.map { chip ->
                    if (chip == uiState.selectedPickupZone) "[$chip]" else chip
                },
            )
            WireframeItemRow("Зона посадки", "Район/метро для сбора", uiState.selectedPickupZone)
        }
        WireframeSection(
            title = "Поля формы (каркас)",
            subtitle = "Даты/время, описание багажных мест, комментарии, контакт и правила посадки.",
        ) {
            WireframeItemRow("Время выезда", "Селект даты/времени и дедлайн подтверждения", "datetime")
            WireframeItemRow("Багаж", "Сколько кейсов/рюкзаков помещается", "capacity")
            WireframeItemRow("Описание", "Примечания для пассажиров и условия", "text")
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка editor-state wiring.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(RideShareTripEditorAction.ToggleDirection) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить направление") }
                OutlinedButton(
                    onClick = { onAction(RideShareTripEditorAction.IncrementSeats) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Увеличить количество мест") }
                OutlinedButton(
                    onClick = { onAction(RideShareTripEditorAction.DecrementSeats) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Уменьшить количество мест") }
                OutlinedButton(
                    onClick = { onAction(RideShareTripEditorAction.ToggleFuelSplit) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить деление топлива") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedPickupZone == uiState.pickupZoneChips.first()) {
                            uiState.pickupZoneChips.getOrElse(1) { uiState.selectedPickupZone }
                        } else {
                            uiState.pickupZoneChips.first()
                        }
                        onAction(RideShareTripEditorAction.SelectPickupZone(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить зону посадки") }
            }
        }
    }
}

data class RideShareMyRouteUiState(
    val selectedMode: String = "Сегодня",
    val modes: List<String> = listOf("Сегодня", "Неделя", "Архив"),
    val upcomingRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Night Raid North", "Сб 17:30 | Водитель | 3 места", "активно"),
        ShellWireframeRow("CQB Sunday Mix", "Вс 09:40 | Пассажир | подтверждено", "ok"),
    ),
    val routePlanRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Сбор", "м. Савеловская, 17:30", "T-0"),
        ShellWireframeRow("Колонна", "АЗС на выезде, 18:05", "T+35"),
        ShellWireframeRow("КПП", "Полигон Северный, 19:00", "ETA"),
    ),
)

sealed interface RideShareMyRouteAction {
    data object ToggleMode : RideShareMyRouteAction
}

class RideShareMyRouteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RideShareMyRouteUiState())
    val uiState: StateFlow<RideShareMyRouteUiState> = _uiState.asStateFlow()

    fun onAction(action: RideShareMyRouteAction) {
        when (action) {
            RideShareMyRouteAction.ToggleMode -> {
                val current = _uiState.value
                val idx = current.modes.indexOf(current.selectedMode)
                val next = current.modes[(idx + 1).mod(current.modes.size)]
                _uiState.value = current.copy(selectedMode = next)
            }
        }
    }
}

@Composable
fun RideShareMyRouteSkeletonRoute(
    rideShareMyRouteViewModel: RideShareMyRouteViewModel = viewModel(),
) {
    val uiState by rideShareMyRouteViewModel.uiState.collectAsState()
    RideShareMyRouteSkeletonScreen(
        uiState = uiState,
        onAction = rideShareMyRouteViewModel::onAction,
    )
}

@Composable
private fun RideShareMyRouteSkeletonScreen(
    uiState: RideShareMyRouteUiState,
    onAction: (RideShareMyRouteAction) -> Unit,
) {
    WireframePage(
        title = "Мой маршрут",
        subtitle = "Персональный каркас поездок: мои активные трансферы, точки сборов и быстрые напоминания.",
        primaryActionLabel = "Открыть ближайшую поездку (заглушка)",
    ) {
        WireframeSection(
            title = "Режим",
            subtitle = "Сегодня / неделя / архив поездок.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Период" to uiState.selectedMode,
                    "Поездок" to uiState.upcomingRows.size.toString(),
                    "Точек" to uiState.routePlanRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.modes.map { if (it == uiState.selectedMode) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Мои поездки",
            subtitle = "Список личных поездок/участий по режиму.",
        ) {
            uiState.upcomingRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "План маршрута",
            subtitle = "Быстрые точки по ближайшей поездке.",
        ) {
            uiState.routePlanRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring для персонального экрана маршрута.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(RideShareMyRouteAction.ToggleMode) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить период") }
            }
        }
    }
}

data class PolygonDetailUiState(
    val polygonId: String = "",
    val selectedTab: String = "Сводка",
    val tabs: List<String> = listOf("Сводка", "Правила", "Инфра", "Контакты"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val ruleRows: List<ShellWireframeRow> = emptyList(),
    val infraRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface PolygonDetailAction {
    data object CycleTab : PolygonDetailAction
}

class PolygonDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PolygonDetailUiState())
    val uiState: StateFlow<PolygonDetailUiState> = _uiState.asStateFlow()

    fun load(polygonId: String) {
        if (_uiState.value.polygonId == polygonId) return
        _uiState.value = PolygonDetailUiState(
            polygonId = polygonId,
            selectedTab = "Сводка",
            summaryRows = listOf(
                ShellWireframeRow("Название", "Полигон Северный / лесной массив", "4.8"),
                ShellWireframeRow("Форматы", "Тренировки, сценарные, ночные игры", "форматы"),
                ShellWireframeRow("Доступ", "По заявке организатору, регистрация до 18:00", "допуск"),
            ),
            ruleRows = listOf(
                ShellWireframeRow("Ограничение FPS", "130 (автомат), 170 (болтовки)", "limit"),
                ShellWireframeRow("Пиротехника", "Только сертифицированная, по согласованию", "rules"),
                ShellWireframeRow("Сигнал стоп", "Общий канал + голосовая команда", "safety"),
            ),
            infraRows = listOf(
                ShellWireframeRow("Парковка", "Охраняемая площадка на 25-30 машин", "ok"),
                ShellWireframeRow("Техзона", "Навес, столы, розетки, хроно", "equip"),
                ShellWireframeRow("Связь", "Слабая LTE, рации рекомендуются", "radio"),
            ),
        )
    }

    fun onAction(action: PolygonDetailAction) {
        when (action) {
            PolygonDetailAction.CycleTab -> {
                val current = _uiState.value
                val idx = current.tabs.indexOf(current.selectedTab)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
fun PolygonDetailSkeletonRoute(
    polygonId: String,
    polygonDetailViewModel: PolygonDetailViewModel = viewModel(),
) {
    LaunchedEffect(polygonId) {
        polygonDetailViewModel.load(polygonId)
    }
    val uiState by polygonDetailViewModel.uiState.collectAsState()
    PolygonDetailSkeletonScreen(
        uiState = uiState,
        onAction = polygonDetailViewModel::onAction,
    )
}

@Composable
private fun PolygonDetailSkeletonScreen(
    uiState: PolygonDetailUiState,
    onAction: (PolygonDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка полигона",
        subtitle = "Детали полигона: допуск, правила, инфраструктура и контактная информация. ID: ${uiState.polygonId}",
        primaryActionLabel = "Открыть календарь полигона (заглушка)",
    ) {
        WireframeSection(
            title = "Навигация по карточке",
            subtitle = "Секции будущей карточки полигона.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedTab,
                    "Секций" to uiState.tabs.size.toString(),
                    "Правил" to uiState.ruleRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Сводка",
            subtitle = "Название, форматы игр, режим допуска и общая информация.",
        ) {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Правила",
            subtitle = "Ограничения, безопасность, пиротехника и поведение на полигоне.",
        ) {
            uiState.ruleRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Инфраструктура",
            subtitle = "Парковка, техзона, навигация, связь и бытовые условия.",
        ) {
            uiState.infraRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring и переключения внутренних разделов.",
        ) {
            Button(
                onClick = { onAction(PolygonDetailAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел карточки") }
        }
    }
}

data class PolygonEditorUiState(
    val editorMode: EditorMode = EditorMode.Create,
    val editorRefId: String = "",
    val selectedTerrain: String = "Лес",
    val terrainChips: List<String> = listOf("Лес", "CQB", "Смешанный", "Индустриальный"),
    val nightGamesAllowed: Boolean = true,
    val parkingEnabled: Boolean = true,
    val fpsLimit: Int = 130,
)

sealed interface PolygonEditorAction {
    data class SelectTerrain(val terrain: String) : PolygonEditorAction
    data object ToggleNightGames : PolygonEditorAction
    data object ToggleParking : PolygonEditorAction
    data object CycleFpsLimit : PolygonEditorAction
}

class PolygonEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PolygonEditorUiState())
    val uiState: StateFlow<PolygonEditorUiState> = _uiState.asStateFlow()

    fun load(editorMode: EditorMode, editorRefId: String) {
        val current = _uiState.value
        if (current.editorMode == editorMode && current.editorRefId == editorRefId) return
        _uiState.value = current.copy(
            editorMode = editorMode,
            editorRefId = editorRefId,
            selectedTerrain = if (editorMode == EditorMode.Edit) "Смешанный" else "Лес",
            nightGamesAllowed = editorMode != EditorMode.Draft,
            parkingEnabled = true,
            fpsLimit = if (editorMode == EditorMode.Edit) 140 else 130,
        )
    }

    fun onAction(action: PolygonEditorAction) {
        when (action) {
            is PolygonEditorAction.SelectTerrain -> {
                _uiState.value = _uiState.value.copy(selectedTerrain = action.terrain)
            }

            PolygonEditorAction.ToggleNightGames -> {
                _uiState.value = _uiState.value.copy(
                    nightGamesAllowed = !_uiState.value.nightGamesAllowed,
                )
            }

            PolygonEditorAction.ToggleParking -> {
                _uiState.value = _uiState.value.copy(
                    parkingEnabled = !_uiState.value.parkingEnabled,
                )
            }

            PolygonEditorAction.CycleFpsLimit -> {
                val next = when (_uiState.value.fpsLimit) {
                    130 -> 140
                    140 -> 150
                    else -> 130
                }
                _uiState.value = _uiState.value.copy(fpsLimit = next)
            }
        }
    }
}

@Composable
fun PolygonEditorSkeletonRoute(
    editorMode: EditorMode,
    editorRefId: String,
    polygonEditorViewModel: PolygonEditorViewModel = viewModel(),
) {
    LaunchedEffect(editorMode, editorRefId) {
        polygonEditorViewModel.load(editorMode, editorRefId)
    }
    val uiState by polygonEditorViewModel.uiState.collectAsState()
    PolygonEditorSkeletonScreen(
        uiState = uiState,
        onAction = polygonEditorViewModel::onAction,
    )
}

@Composable
private fun PolygonEditorSkeletonScreen(
    uiState: PolygonEditorUiState,
    onAction: (PolygonEditorAction) -> Unit,
) {
    WireframePage(
        title = "Редактор полигона",
        subtitle = "Каркас создания/редактирования полигона. Режим: ${uiState.editorMode.label} | Объект: ${uiState.editorRefId}",
        primaryActionLabel = "Сохранить полигон (заглушка)",
    ) {
        WireframeSection(
            title = "Основные параметры",
            subtitle = "Тип площадки, FPS-ограничение, ночные игры и парковка.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.editorMode.label,
                    "Тип" to uiState.selectedTerrain,
                    "FPS" to uiState.fpsLimit.toString(),
                ),
            )
            WireframeItemRow(
                "Ночные игры",
                "Разрешены ли ночные сценарии на площадке",
                if (uiState.nightGamesAllowed) "да" else "нет",
            )
            WireframeItemRow(
                "Парковка",
                "Наличие парковки и схема доступа",
                if (uiState.parkingEnabled) "есть" else "нет",
            )
        }
        WireframeSection(
            title = "Категории и зона",
            subtitle = "Классификация полигона и тегирование для поиска.",
        ) {
            WireframeChipRow(
                labels = uiState.terrainChips.map { chip ->
                    if (chip == uiState.selectedTerrain) "[$chip]" else chip
                },
            )
            WireframeItemRow("Теги", "Лес/CQB/смешанный/инфра/ночные игры", "chips")
        }
        WireframeSection(
            title = "Поля формы (каркас)",
            subtitle = "Адрес, гео-точки, контакты, схема въезда, правила и фото.",
        ) {
            WireframeItemRow("Адрес и геометка", "Координаты, точка КПП, парковка", "geo")
            WireframeItemRow("Контакты", "Организатор, телефон, Telegram", "contacts")
            WireframeItemRow("Правила", "FPS, пиротехника, возраст, медик", "rules")
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка editor-state wiring для полигона.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(PolygonEditorAction.CycleFpsLimit) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить лимит FPS") }
                OutlinedButton(
                    onClick = { onAction(PolygonEditorAction.ToggleNightGames) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить ночные игры") }
                OutlinedButton(
                    onClick = { onAction(PolygonEditorAction.ToggleParking) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить парковку") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedTerrain == uiState.terrainChips.first()) {
                            uiState.terrainChips.getOrElse(1) { uiState.selectedTerrain }
                        } else {
                            uiState.terrainChips.first()
                        }
                        onAction(PolygonEditorAction.SelectTerrain(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить тип полигона") }
            }
        }
    }
}

data class PolygonRulesMapUiState(
    val polygonId: String = "",
    val selectedLayer: String = "Схема",
    val layers: List<String> = listOf("Схема", "Безопасность", "Логистика"),
    val mapRows: List<ShellWireframeRow> = emptyList(),
    val safetyRows: List<ShellWireframeRow> = emptyList(),
    val logisticsRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface PolygonRulesMapAction {
    data object CycleLayer : PolygonRulesMapAction
}

class PolygonRulesMapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PolygonRulesMapUiState())
    val uiState: StateFlow<PolygonRulesMapUiState> = _uiState.asStateFlow()

    fun load(polygonId: String) {
        if (_uiState.value.polygonId == polygonId) return
        _uiState.value = PolygonRulesMapUiState(
            polygonId = polygonId,
            selectedLayer = "Схема",
            mapRows = listOf(
                ShellWireframeRow("КПП / въезд", "Точка встречи, контроль допуска, парковка", "map"),
                ShellWireframeRow("Игровые зоны", "Лес, CQB сектор, деревня, мёртвая зона", "zones"),
                ShellWireframeRow("Маршрут колонны", "Подъезд для команд и разгрузки", "route"),
            ),
            safetyRows = listOf(
                ShellWireframeRow("Мёртвая зона", "Правила масок, чехлов и безопасности", "safe"),
                ShellWireframeRow("Медпункт", "Точка медика и эвакуационный маршрут", "med"),
                ShellWireframeRow("Стоп-игра", "Сигналы и резервный канал связи", "stop"),
            ),
            logisticsRows = listOf(
                ShellWireframeRow("Парковка", "Разделение по командам/организаторам", "park"),
                ShellWireframeRow("Техзона", "Навесы, столы, зарядка аккумуляторов", "tech"),
                ShellWireframeRow("Навигация", "Точки навигации для новичков", "nav"),
            ),
        )
    }

    fun onAction(action: PolygonRulesMapAction) {
        when (action) {
            PolygonRulesMapAction.CycleLayer -> {
                val current = _uiState.value
                val idx = current.layers.indexOf(current.selectedLayer)
                val next = current.layers[(idx + 1).mod(current.layers.size)]
                _uiState.value = current.copy(selectedLayer = next)
            }
        }
    }
}

@Composable
fun PolygonRulesMapSkeletonRoute(
    polygonId: String,
    polygonRulesMapViewModel: PolygonRulesMapViewModel = viewModel(),
) {
    LaunchedEffect(polygonId) {
        polygonRulesMapViewModel.load(polygonId)
    }
    val uiState by polygonRulesMapViewModel.uiState.collectAsState()
    PolygonRulesMapSkeletonScreen(
        uiState = uiState,
        onAction = polygonRulesMapViewModel::onAction,
    )
}

@Composable
private fun PolygonRulesMapSkeletonScreen(
    uiState: PolygonRulesMapUiState,
    onAction: (PolygonRulesMapAction) -> Unit,
) {
    WireframePage(
        title = "Правила и карта полигона",
        subtitle = "Каркас большого экрана схемы полигона: слои карты, безопасность и логистика. ID: ${uiState.polygonId}",
        primaryActionLabel = "Открыть полноэкранную карту (заглушка)",
    ) {
        WireframeSection(
            title = "Слои карты",
            subtitle = "Переключение информационных слоёв будущей карты полигона.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Слой" to uiState.selectedLayer,
                    "Точек" to uiState.mapRows.size.toString(),
                    "Правил" to uiState.safetyRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.layers.map { if (it == uiState.selectedLayer) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Схема полигона",
            subtitle = "Ключевые зоны, маршруты и ориентиры.",
        ) {
            uiState.mapRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Безопасность",
            subtitle = "Мёртвая зона, медпункт, сигналы остановки и безопасные маршруты.",
        ) {
            uiState.safetyRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Логистика",
            subtitle = "Парковка, техзона, размещение команд и бытовые точки.",
        ) {
            uiState.logisticsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка переключения слоёв карты/правил.",
        ) {
            Button(
                onClick = { onAction(PolygonRulesMapAction.CycleLayer) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить слой карты") }
        }
    }
}

data class ShopDetailUiState(
    val shopId: String = "",
    val selectedSection: String = "Сводка",
    val sections: List<String> = listOf("Сводка", "Каталог", "Доставка", "Контакты"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val catalogRows: List<ShellWireframeRow> = emptyList(),
    val logisticsRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface ShopDetailAction {
    data object CycleSection : ShopDetailAction
}

class ShopDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState: StateFlow<ShopDetailUiState> = _uiState.asStateFlow()

    fun load(shopId: String) {
        if (_uiState.value.shopId == shopId) return
        _uiState.value = ShopDetailUiState(
            shopId = shopId,
            summaryRows = listOf(
                ShellWireframeRow("Название", "Airsoft Hub / партнёрский магазин", "4.9"),
                ShellWireframeRow("Город", "Москва | самовывоз + доставка", "city"),
                ShellWireframeRow("Формат", "Розница, сайт, предзаказ, выдача на играх", "format"),
            ),
            catalogRows = listOf(
                ShellWireframeRow("Приводы", "AEG/GBB, стартовые комплекты и тюнинг", "каталог"),
                ShellWireframeRow("Снаряжение", "Разгрузки, форма, защита, обувь", "gear"),
                ShellWireframeRow("Расходники", "Шары, газ, аккумы, зарядка", "stock"),
            ),
            logisticsRows = listOf(
                ShellWireframeRow("Доставка", "СДЭК/курьер/самовывоз, сроки по регионам", "ship"),
                ShellWireframeRow("Гарантия", "Обмен/возврат и сервисное сопровождение", "warranty"),
                ShellWireframeRow("Выдача на игре", "Партнёрские события и пункты выдачи", "pickup"),
            ),
        )
    }

    fun onAction(action: ShopDetailAction) {
        when (action) {
            ShopDetailAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }
        }
    }
}

@Composable
fun ShopDetailSkeletonRoute(
    shopId: String,
    shopDetailViewModel: ShopDetailViewModel = viewModel(),
) {
    LaunchedEffect(shopId) {
        shopDetailViewModel.load(shopId)
    }
    val uiState by shopDetailViewModel.uiState.collectAsState()
    ShopDetailSkeletonScreen(
        uiState = uiState,
        onAction = shopDetailViewModel::onAction,
    )
}

@Composable
private fun ShopDetailSkeletonScreen(
    uiState: ShopDetailUiState,
    onAction: (ShopDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка магазина",
        subtitle = "Детали магазина: ассортимент, логистика, сервис и контакты. ID: ${uiState.shopId}",
        primaryActionLabel = "Открыть каталог магазина (заглушка)",
    ) {
        WireframeSection(
            title = "Навигация по карточке",
            subtitle = "Разделы будущей страницы магазина.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Категорий" to uiState.catalogRows.size.toString(),
                    "Логистика" to uiState.logisticsRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Сводка",
            subtitle = "Основная информация о магазине и формате работы.",
        ) {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Каталог",
            subtitle = "Ключевые категории ассортимента.",
        ) {
            uiState.catalogRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Логистика и сервис",
            subtitle = "Доставка, гарантия, выдача на играх и условия обслуживания.",
        ) {
            uiState.logisticsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring карточки магазина.",
        ) {
            Button(
                onClick = { onAction(ShopDetailAction.CycleSection) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел карточки") }
        }
    }
}

data class ShopEditorUiState(
    val editorMode: EditorMode = EditorMode.Create,
    val editorRefId: String = "",
    val selectedCategory: String = "Приводы",
    val categoryChips: List<String> = listOf("Приводы", "Снаряга", "Оптика", "Расходники", "Прокат"),
    val selectedCity: String = "Москва",
    val deliveryEnabled: Boolean = true,
    val pickupOnGamesEnabled: Boolean = false,
)

sealed interface ShopEditorAction {
    data class SelectCategory(val category: String) : ShopEditorAction
    data object CycleCity : ShopEditorAction
    data object ToggleDelivery : ShopEditorAction
    data object TogglePickupOnGames : ShopEditorAction
}

class ShopEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShopEditorUiState())
    val uiState: StateFlow<ShopEditorUiState> = _uiState.asStateFlow()

    fun load(editorMode: EditorMode, editorRefId: String) {
        val current = _uiState.value
        if (current.editorMode == editorMode && current.editorRefId == editorRefId) return
        _uiState.value = current.copy(
            editorMode = editorMode,
            editorRefId = editorRefId,
            selectedCategory = if (editorMode == EditorMode.Edit) "Снаряга" else "Приводы",
            selectedCity = if (editorMode == EditorMode.Edit) "Санкт-Петербург" else "Москва",
            deliveryEnabled = true,
            pickupOnGamesEnabled = editorMode == EditorMode.Edit,
        )
    }

    fun onAction(action: ShopEditorAction) {
        when (action) {
            is ShopEditorAction.SelectCategory -> {
                _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            }

            ShopEditorAction.CycleCity -> {
                val next = when (_uiState.value.selectedCity) {
                    "Москва" -> "Санкт-Петербург"
                    "Санкт-Петербург" -> "Казань"
                    else -> "Москва"
                }
                _uiState.value = _uiState.value.copy(selectedCity = next)
            }

            ShopEditorAction.ToggleDelivery -> {
                _uiState.value = _uiState.value.copy(deliveryEnabled = !_uiState.value.deliveryEnabled)
            }

            ShopEditorAction.TogglePickupOnGames -> {
                _uiState.value = _uiState.value.copy(
                    pickupOnGamesEnabled = !_uiState.value.pickupOnGamesEnabled,
                )
            }
        }
    }
}

@Composable
fun ShopEditorSkeletonRoute(
    editorMode: EditorMode,
    editorRefId: String,
    shopEditorViewModel: ShopEditorViewModel = viewModel(),
) {
    LaunchedEffect(editorMode, editorRefId) {
        shopEditorViewModel.load(editorMode, editorRefId)
    }
    val uiState by shopEditorViewModel.uiState.collectAsState()
    ShopEditorSkeletonScreen(
        uiState = uiState,
        onAction = shopEditorViewModel::onAction,
    )
}

@Composable
private fun ShopEditorSkeletonScreen(
    uiState: ShopEditorUiState,
    onAction: (ShopEditorAction) -> Unit,
) {
    WireframePage(
        title = "Редактор магазина",
        subtitle = "Каркас создания/редактирования магазина. Режим: ${uiState.editorMode.label} | Объект: ${uiState.editorRefId}",
        primaryActionLabel = "Сохранить магазин (заглушка)",
    ) {
        WireframeSection(
            title = "Основные параметры",
            subtitle = "Категория, город, доставка и выдача на играх.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.editorMode.label,
                    "Категория" to uiState.selectedCategory,
                    "Город" to uiState.selectedCity,
                ),
            )
            WireframeItemRow(
                "Доставка",
                "Работа с заказами и отправками",
                if (uiState.deliveryEnabled) "вкл" else "выкл",
            )
            WireframeItemRow(
                "Выдача на играх",
                "Партнёрские события и точки выдачи",
                if (uiState.pickupOnGamesEnabled) "вкл" else "выкл",
            )
        }
        WireframeSection(
            title = "Каталог и теги",
            subtitle = "Категории магазина и фильтры для контекстного поиска.",
        ) {
            WireframeChipRow(
                labels = uiState.categoryChips.map { chip ->
                    if (chip == uiState.selectedCategory) "[$chip]" else chip
                },
            )
            WireframeItemRow("Теги", "Бренды, наличие, прокат, самовывоз, тюнинг", "chips")
        }
        WireframeSection(
            title = "Поля формы (каркас)",
            subtitle = "Контакты, график, адрес, сайт/Telegram, условия доставки и оплаты.",
        ) {
            WireframeItemRow("Контакты", "Телефон, Telegram, менеджер", "contacts")
            WireframeItemRow("Адрес и график", "Режим работы, карта, точка выдачи", "address")
            WireframeItemRow("Логистика", "Доставка/оплата/сроки/возврат", "shipping")
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка editor-state wiring для магазина.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ShopEditorAction.CycleCity) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить город") }
                OutlinedButton(
                    onClick = { onAction(ShopEditorAction.ToggleDelivery) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить доставку") }
                OutlinedButton(
                    onClick = { onAction(ShopEditorAction.TogglePickupOnGames) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить выдачу на играх") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == uiState.categoryChips.first()) {
                            uiState.categoryChips.getOrElse(1) { uiState.selectedCategory }
                        } else {
                            uiState.categoryChips.first()
                        }
                        onAction(ShopEditorAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить категорию магазина") }
            }
        }
    }
}

data class ServiceDetailUiState(
    val serviceId: String = "",
    val selectedSection: String = "Сводка",
    val sections: List<String> = listOf("Сводка", "Портфолио", "Условия", "Отзывы"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val portfolioRows: List<ShellWireframeRow> = emptyList(),
    val conditionsRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface ServiceDetailAction {
    data object CycleSection : ServiceDetailAction
}

class ServiceDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    fun load(serviceId: String) {
        if (_uiState.value.serviceId == serviceId) return
        _uiState.value = ServiceDetailUiState(
            serviceId = serviceId,
            summaryRows = listOf(
                ShellWireframeRow("Услуга", "Тюнинг AEG / настройка хоп-ап и шимминг", "4.9"),
                ShellWireframeRow("Исполнитель", "North Tech | Москва | верифицирован", "pro"),
                ShellWireframeRow("Срок", "1-3 дня в зависимости от очереди", "ETA"),
            ),
            portfolioRows = listOf(
                ShellWireframeRow("Кейсы", "Примеры работ: DMR, CQB, техобслуживание", "portfolio"),
                ShellWireframeRow("Оборудование", "Хронограф, токовые тесты, инструменты", "gear"),
                ShellWireframeRow("Специализация", "AEG/GBB, хоп, электрика, диагностика", "spec"),
            ),
            conditionsRows = listOf(
                ShellWireframeRow("Стоимость", "Диагностика / базовый / полный тюнинг", "price"),
                ShellWireframeRow("Гарантия", "Повторная проверка и сопровождение после игры", "warranty"),
                ShellWireframeRow("Логистика", "Приём в мастерской / передача на игре", "pickup"),
            ),
        )
    }

    fun onAction(action: ServiceDetailAction) {
        when (action) {
            ServiceDetailAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }
        }
    }
}

@Composable
fun ServiceDetailSkeletonRoute(
    serviceId: String,
    serviceDetailViewModel: ServiceDetailViewModel = viewModel(),
) {
    LaunchedEffect(serviceId) {
        serviceDetailViewModel.load(serviceId)
    }
    val uiState by serviceDetailViewModel.uiState.collectAsState()
    ServiceDetailSkeletonScreen(
        uiState = uiState,
        onAction = serviceDetailViewModel::onAction,
    )
}

@Composable
private fun ServiceDetailSkeletonScreen(
    uiState: ServiceDetailUiState,
    onAction: (ServiceDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка услуги",
        subtitle = "Детали услуги: исполнитель, портфолио, условия и отзывы. ID: ${uiState.serviceId}",
        primaryActionLabel = "Оставить заявку (заглушка)",
    ) {
        WireframeSection(
            title = "Навигация по карточке",
            subtitle = "Разделы будущей карточки услуги.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Портфолио" to uiState.portfolioRows.size.toString(),
                    "Условия" to uiState.conditionsRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Сводка",
            subtitle = "Что за услуга, кто исполнитель, сроки и базовые метрики.",
        ) {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Портфолио и специализация",
            subtitle = "Кейсы, оборудование, направления работ и подтверждения.",
        ) {
            uiState.portfolioRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Условия",
            subtitle = "Стоимость, гарантия, логистика передачи и ограничения.",
        ) {
            uiState.conditionsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring карточки услуги.",
        ) {
            Button(
                onClick = { onAction(ServiceDetailAction.CycleSection) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел карточки") }
        }
    }
}

data class ServiceEditorUiState(
    val editorMode: EditorMode = EditorMode.Create,
    val editorRefId: String = "",
    val selectedCategory: String = "Ремонт",
    val categories: List<String> = listOf("Ремонт", "Тюнинг", "Инструктор", "Судья", "Фото/Видео"),
    val onlyVerified: Boolean = true,
    val fieldServiceEnabled: Boolean = false,
    val responseMode: String = "В течение дня",
)

sealed interface ServiceEditorAction {
    data class SelectCategory(val category: String) : ServiceEditorAction
    data object ToggleVerified : ServiceEditorAction
    data object ToggleFieldService : ServiceEditorAction
    data object CycleResponseMode : ServiceEditorAction
}

class ServiceEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceEditorUiState())
    val uiState: StateFlow<ServiceEditorUiState> = _uiState.asStateFlow()

    fun load(editorMode: EditorMode, editorRefId: String) {
        val current = _uiState.value
        if (current.editorMode == editorMode && current.editorRefId == editorRefId) return
        _uiState.value = current.copy(
            editorMode = editorMode,
            editorRefId = editorRefId,
            selectedCategory = if (editorMode == EditorMode.Edit) "Тюнинг" else "Ремонт",
            onlyVerified = editorMode != EditorMode.Draft,
            fieldServiceEnabled = editorMode == EditorMode.Edit,
            responseMode = if (editorMode == EditorMode.Edit) "1-3 часа" else "В течение дня",
        )
    }

    fun onAction(action: ServiceEditorAction) {
        when (action) {
            is ServiceEditorAction.SelectCategory -> {
                _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            }

            ServiceEditorAction.ToggleVerified -> {
                _uiState.value = _uiState.value.copy(onlyVerified = !_uiState.value.onlyVerified)
            }

            ServiceEditorAction.ToggleFieldService -> {
                _uiState.value = _uiState.value.copy(
                    fieldServiceEnabled = !_uiState.value.fieldServiceEnabled,
                )
            }

            ServiceEditorAction.CycleResponseMode -> {
                val next = when (_uiState.value.responseMode) {
                    "В течение дня" -> "1-3 часа"
                    "1-3 часа" -> "Сразу"
                    else -> "В течение дня"
                }
                _uiState.value = _uiState.value.copy(responseMode = next)
            }
        }
    }
}

@Composable
fun ServiceEditorSkeletonRoute(
    editorMode: EditorMode,
    editorRefId: String,
    serviceEditorViewModel: ServiceEditorViewModel = viewModel(),
) {
    LaunchedEffect(editorMode, editorRefId) {
        serviceEditorViewModel.load(editorMode, editorRefId)
    }
    val uiState by serviceEditorViewModel.uiState.collectAsState()
    ServiceEditorSkeletonScreen(
        uiState = uiState,
        onAction = serviceEditorViewModel::onAction,
    )
}

@Composable
private fun ServiceEditorSkeletonScreen(
    uiState: ServiceEditorUiState,
    onAction: (ServiceEditorAction) -> Unit,
) {
    WireframePage(
        title = "Редактор услуги",
        subtitle = "Каркас создания/редактирования услуги. Режим: ${uiState.editorMode.label} | Объект: ${uiState.editorRefId}",
        primaryActionLabel = "Сохранить услугу (заглушка)",
    ) {
        WireframeSection(
            title = "Основные параметры",
            subtitle = "Категория, SLA ответа, выездной формат и требования к верификации.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.editorMode.label,
                    "Категория" to uiState.selectedCategory,
                    "Ответ" to uiState.responseMode,
                ),
            )
            WireframeItemRow(
                "Только верифицированные",
                "Фильтр/статус публикации в каталоге услуг",
                if (uiState.onlyVerified) "да" else "нет",
            )
            WireframeItemRow(
                "Выезд на полигон",
                "Возможность оказывать услугу на месте проведения игры",
                if (uiState.fieldServiceEnabled) "да" else "нет",
            )
        }
        WireframeSection(
            title = "Категории и теги",
            subtitle = "Классификация услуги для контекстного поиска и выдачи.",
        ) {
            WireframeChipRow(
                labels = uiState.categories.map { if (it == uiState.selectedCategory) "[$it]" else it },
            )
            WireframeItemRow("Теги", "AEG/GBB, инструктор, судейство, фото, ремонт", "chips")
        }
        WireframeSection(
            title = "Поля формы (каркас)",
            subtitle = "Описание услуги, прайс, примеры работ, география, контакты и SLA.",
        ) {
            WireframeItemRow("Описание", "Что входит в услугу и ограничения", "text")
            WireframeItemRow("Стоимость", "Базовая цена / пакеты / доп. работы", "price")
            WireframeItemRow("Контакты и слот", "Связь, график, окно приёма заявок", "schedule")
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка editor-state wiring для услуги.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ServiceEditorAction.CycleResponseMode) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить SLA ответа") }
                OutlinedButton(
                    onClick = { onAction(ServiceEditorAction.ToggleVerified) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить верификацию") }
                OutlinedButton(
                    onClick = { onAction(ServiceEditorAction.ToggleFieldService) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить выездной формат") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == uiState.categories.first()) {
                            uiState.categories.getOrElse(1) { uiState.selectedCategory }
                        } else {
                            uiState.categories.first()
                        }
                        onAction(ServiceEditorAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить категорию услуги") }
            }
        }
    }
}

data class GameCalendarDetailUiState(
    val gameId: String = "",
    val selectedTab: String = "Сводка",
    val tabs: List<String> = listOf("Сводка", "Состав", "Сценарий", "Статусы"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val rosterRows: List<ShellWireframeRow> = emptyList(),
    val scenarioRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface GameCalendarDetailAction {
    data object CycleTab : GameCalendarDetailAction
}

class GameCalendarDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameCalendarDetailUiState())
    val uiState: StateFlow<GameCalendarDetailUiState> = _uiState.asStateFlow()

    fun load(gameId: String) {
        if (_uiState.value.gameId == gameId) return
        _uiState.value = GameCalendarDetailUiState(
            gameId = gameId,
            summaryRows = listOf(
                ShellWireframeRow("Событие", "Night Raid North | Полигон Северный", "открыто"),
                ShellWireframeRow("Время", "Сб 19:00 | сбор с 17:30", "time"),
                ShellWireframeRow("Формат", "Сценарная игра | 40-80 игроков", "format"),
            ),
            rosterRows = listOf(
                ShellWireframeRow("Команда [EW]", "14 игроков | подтверждено 11", "команда"),
                ShellWireframeRow("Роль: медик", "1/2 занято, нужен ещё 1", "роль"),
                ShellWireframeRow("Роль: саппорт", "1/2 занято", "роль"),
            ),
            scenarioRows = listOf(
                ShellWireframeRow("Брифинг", "Черновик брифинга и вводной легенды", "doc"),
                ShellWireframeRow("Точки задачи", "3 ключевые точки и тайминги захвата", "map"),
                ShellWireframeRow("Ограничения", "Пиротехника, FPS, ночной режим", "rules"),
            ),
        )
    }

    fun onAction(action: GameCalendarDetailAction) {
        when (action) {
            GameCalendarDetailAction.CycleTab -> {
                val current = _uiState.value
                val idx = current.tabs.indexOf(current.selectedTab)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
fun GameCalendarDetailSkeletonRoute(
    gameId: String,
    gameCalendarDetailViewModel: GameCalendarDetailViewModel = viewModel(),
) {
    LaunchedEffect(gameId) {
        gameCalendarDetailViewModel.load(gameId)
    }
    val uiState by gameCalendarDetailViewModel.uiState.collectAsState()
    GameCalendarDetailSkeletonScreen(
        uiState = uiState,
        onAction = gameCalendarDetailViewModel::onAction,
    )
}

@Composable
private fun GameCalendarDetailSkeletonScreen(
    uiState: GameCalendarDetailUiState,
    onAction: (GameCalendarDetailAction) -> Unit,
) {
    WireframePage(
        title = "Карточка игры",
        subtitle = "Детали игры: сводка, состав, сценарий и статусы участия. ID: ${uiState.gameId}",
        primaryActionLabel = "Открыть чат игры (заглушка)",
    ) {
        WireframeSection(
            title = "Разделы карточки",
            subtitle = "Вкладки будущего экрана игры.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedTab,
                    "Участники" to uiState.rosterRows.size.toString(),
                    "Сценарий" to uiState.scenarioRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Сводка",
            subtitle = "Базовая информация о событии и статусе регистрации.",
        ) {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Состав и роли",
            subtitle = "Подтверждение участия, роли и слотирование команды.",
        ) {
            uiState.rosterRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Сценарий и правила",
            subtitle = "Брифинг, задачи, ограничения и документы игры.",
        ) {
            uiState.scenarioRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring карточки игры.",
        ) {
            Button(
                onClick = { onAction(GameCalendarDetailAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел карточки") }
        }
    }
}

data class GameCalendarEditorUiState(
    val editorMode: EditorMode = EditorMode.Create,
    val editorRefId: String = "",
    val selectedFormat: String = "Сценарная",
    val formatChips: List<String> = listOf("Сценарная", "Тренировка", "CQB", "Турнир"),
    val registrationOpen: Boolean = true,
    val needRoleSlots: Boolean = true,
    val selectedTimeWindow: String = "Вечер",
)

sealed interface GameCalendarEditorAction {
    data class SelectFormat(val format: String) : GameCalendarEditorAction
    data object ToggleRegistration : GameCalendarEditorAction
    data object ToggleRoleSlots : GameCalendarEditorAction
    data object CycleTimeWindow : GameCalendarEditorAction
}

class GameCalendarEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameCalendarEditorUiState())
    val uiState: StateFlow<GameCalendarEditorUiState> = _uiState.asStateFlow()

    fun load(editorMode: EditorMode, editorRefId: String) {
        val current = _uiState.value
        if (current.editorMode == editorMode && current.editorRefId == editorRefId) return
        _uiState.value = current.copy(
            editorMode = editorMode,
            editorRefId = editorRefId,
            selectedFormat = if (editorMode == EditorMode.Edit) "Тренировка" else "Сценарная",
            registrationOpen = editorMode != EditorMode.Draft,
            needRoleSlots = true,
            selectedTimeWindow = if (editorMode == EditorMode.Edit) "День" else "Вечер",
        )
    }

    fun onAction(action: GameCalendarEditorAction) {
        when (action) {
            is GameCalendarEditorAction.SelectFormat -> {
                _uiState.value = _uiState.value.copy(selectedFormat = action.format)
            }

            GameCalendarEditorAction.ToggleRegistration -> {
                _uiState.value = _uiState.value.copy(
                    registrationOpen = !_uiState.value.registrationOpen,
                )
            }

            GameCalendarEditorAction.ToggleRoleSlots -> {
                _uiState.value = _uiState.value.copy(
                    needRoleSlots = !_uiState.value.needRoleSlots,
                )
            }

            GameCalendarEditorAction.CycleTimeWindow -> {
                val next = when (_uiState.value.selectedTimeWindow) {
                    "Утро" -> "День"
                    "День" -> "Вечер"
                    else -> "Утро"
                }
                _uiState.value = _uiState.value.copy(selectedTimeWindow = next)
            }
        }
    }
}

@Composable
fun GameCalendarEditorSkeletonRoute(
    editorMode: EditorMode,
    editorRefId: String,
    gameCalendarEditorViewModel: GameCalendarEditorViewModel = viewModel(),
) {
    LaunchedEffect(editorMode, editorRefId) {
        gameCalendarEditorViewModel.load(editorMode, editorRefId)
    }
    val uiState by gameCalendarEditorViewModel.uiState.collectAsState()
    GameCalendarEditorSkeletonScreen(
        uiState = uiState,
        onAction = gameCalendarEditorViewModel::onAction,
    )
}

@Composable
private fun GameCalendarEditorSkeletonScreen(
    uiState: GameCalendarEditorUiState,
    onAction: (GameCalendarEditorAction) -> Unit,
) {
    WireframePage(
        title = "Редактор игры",
        subtitle = "Каркас создания/редактирования игры. Режим: ${uiState.editorMode.label} | Объект: ${uiState.editorRefId}",
        primaryActionLabel = "Сохранить игру (заглушка)",
    ) {
        WireframeSection(
            title = "Основные параметры",
            subtitle = "Формат игры, временное окно, регистрация и роли.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.editorMode.label,
                    "Формат" to uiState.selectedFormat,
                    "Окно" to uiState.selectedTimeWindow,
                ),
            )
            WireframeItemRow(
                "Регистрация",
                "Открыта ли запись на игру",
                if (uiState.registrationOpen) "открыта" else "закрыта",
            )
            WireframeItemRow(
                "Слоты ролей",
                "Нужен ли контроль ролей/слотов",
                if (uiState.needRoleSlots) "да" else "нет",
            )
        }
        WireframeSection(
            title = "Формат и классификация",
            subtitle = "Фильтры выдачи календаря и событий.",
        ) {
            WireframeChipRow(
                labels = uiState.formatChips.map { if (it == uiState.selectedFormat) "[$it]" else it },
            )
            WireframeItemRow("Теги", "Ночь / CQB / сценарий / тренировка / турнир", "chips")
        }
        WireframeSection(
            title = "Поля формы (каркас)",
            subtitle = "Дата/время, полигон, лимиты, брифинг, регистрация, роли, правила.",
        ) {
            WireframeItemRow("Дата и время", "Старт, сбор, окончание, дедлайны", "datetime")
            WireframeItemRow("Полигон", "Привязка к карточке полигона и карте", "polygon")
            WireframeItemRow("Сценарий", "Легенда, задачи, тайминги, ограничения", "scenario")
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка editor-state wiring для игры.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(GameCalendarEditorAction.CycleTimeWindow) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить временное окно") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarEditorAction.ToggleRegistration) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить регистрацию") }
                OutlinedButton(
                    onClick = { onAction(GameCalendarEditorAction.ToggleRoleSlots) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить слоты ролей") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedFormat == uiState.formatChips.first()) {
                            uiState.formatChips.getOrElse(1) { uiState.selectedFormat }
                        } else {
                            uiState.formatChips.first()
                        }
                        onAction(GameCalendarEditorAction.SelectFormat(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить формат игры") }
            }
        }
    }
}

data class GameCalendarLogisticsUiState(
    val gameId: String = "",
    val selectedMode: String = "Команда",
    val modes: List<String> = listOf("Команда", "Транспорт", "Чек-лист"),
    val transportRows: List<ShellWireframeRow> = emptyList(),
    val packingRows: List<ShellWireframeRow> = emptyList(),
    val checkinRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface GameCalendarLogisticsAction {
    data object CycleMode : GameCalendarLogisticsAction
}

class GameCalendarLogisticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameCalendarLogisticsUiState())
    val uiState: StateFlow<GameCalendarLogisticsUiState> = _uiState.asStateFlow()

    fun load(gameId: String) {
        if (_uiState.value.gameId == gameId) return
        _uiState.value = GameCalendarLogisticsUiState(
            gameId = gameId,
            transportRows = listOf(
                ShellWireframeRow("Попутчики", "2 машины, 5 мест свободно", "rideshare"),
                ShellWireframeRow("Сбор колонны", "18:05 | АЗС на выезде", "ETA"),
                ShellWireframeRow("Маршрут", "КПП / парковка / разгрузка", "route"),
            ),
            packingRows = listOf(
                ShellWireframeRow("Аккумы", "Проверка заряда и резервных батарей", "check"),
                ShellWireframeRow("Медпак", "Командный и личный комплект", "check"),
                ShellWireframeRow("Радиосвязь", "Канал, резерв, гарнитуры", "check"),
            ),
            checkinRows = listOf(
                ShellWireframeRow("КПП", "Список команды и подтверждение допуска", "entry"),
                ShellWireframeRow("Хронограф", "Проверка FPS по ролям", "chrono"),
                ShellWireframeRow("Брифинг", "Сбор в техзоне / подтверждение ролей", "briefing"),
            ),
        )
    }

    fun onAction(action: GameCalendarLogisticsAction) {
        when (action) {
            GameCalendarLogisticsAction.CycleMode -> {
                val current = _uiState.value
                val idx = current.modes.indexOf(current.selectedMode)
                val next = current.modes[(idx + 1).mod(current.modes.size)]
                _uiState.value = current.copy(selectedMode = next)
            }
        }
    }
}

@Composable
fun GameCalendarLogisticsSkeletonRoute(
    gameId: String,
    gameCalendarLogisticsViewModel: GameCalendarLogisticsViewModel = viewModel(),
) {
    LaunchedEffect(gameId) {
        gameCalendarLogisticsViewModel.load(gameId)
    }
    val uiState by gameCalendarLogisticsViewModel.uiState.collectAsState()
    GameCalendarLogisticsSkeletonScreen(
        uiState = uiState,
        onAction = gameCalendarLogisticsViewModel::onAction,
    )
}

@Composable
private fun GameCalendarLogisticsSkeletonScreen(
    uiState: GameCalendarLogisticsUiState,
    onAction: (GameCalendarLogisticsAction) -> Unit,
) {
    WireframePage(
        title = "Логистика игры",
        subtitle = "Каркас логистики: транспорт, чек-листы, заезд на полигон и контроль готовности. ID: ${uiState.gameId}",
        primaryActionLabel = "Открыть попутчиков (заглушка)",
    ) {
        WireframeSection(
            title = "Режим логистики",
            subtitle = "Команда / транспорт / чек-лист подготовки.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.selectedMode,
                    "Транспорт" to uiState.transportRows.size.toString(),
                    "Чек-лист" to uiState.packingRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.modes.map { if (it == uiState.selectedMode) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Транспорт",
            subtitle = "Попутчики, сбор колонны, маршрут и точки прибытия.",
        ) {
            uiState.transportRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Подготовка и упаковка",
            subtitle = "Чек-листы снаряжения и командной готовности.",
        ) {
            uiState.packingRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Заезд и регистрация",
            subtitle = "КПП, хроно, брифинг и контроль состава на месте.",
        ) {
            uiState.checkinRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring логистики игры.",
        ) {
            Button(
                onClick = { onAction(GameCalendarLogisticsAction.CycleMode) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить режим логистики") }
        }
    }
}
