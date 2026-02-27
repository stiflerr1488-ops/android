package com.airsoft.social.feature.directories.impl

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

internal data class ContextSearchSeed(
    val title: String,
    val subtitle: String,
    val queryHint: String,
    val scopes: List<String>,
    val filters: List<String>,
    val recentQueries: List<String>,
    val resultRows: List<ShellWireframeRow>,
    val suggestionRows: List<ShellWireframeRow>,
    val primaryActionLabel: String,
)

data class ContextSearchUiState(
    val title: String,
    val subtitle: String,
    val queryHint: String,
    val query: String,
    val scopes: List<String>,
    val selectedScope: String,
    val filters: List<String>,
    val selectedFilter: String,
    val recentQueries: List<String>,
    val resultRows: List<ShellWireframeRow>,
    val suggestionRows: List<ShellWireframeRow>,
    val primaryActionLabel: String,
)

sealed interface ContextSearchAction {
    data object CycleDemoQuery : ContextSearchAction
    data object ClearQuery : ContextSearchAction
    data class SelectScope(val scope: String) : ContextSearchAction
    data class SelectFilter(val filter: String) : ContextSearchAction
    data object OpenPrimaryTargetClicked : ContextSearchAction
}

internal abstract class BaseContextSearchViewModel(
    seed: ContextSearchSeed,
) : ViewModel() {
    private val seedState = seed
    private val _uiState = MutableStateFlow(seed.toUiState())
    val uiState: StateFlow<ContextSearchUiState> = _uiState.asStateFlow()

    fun onAction(action: ContextSearchAction) {
        when (action) {
            ContextSearchAction.CycleDemoQuery -> {
                val current = _uiState.value.query
                val next = when (current) {
                    "" -> seedState.recentQueries.firstOrNull().orEmpty()
                    seedState.recentQueries.firstOrNull().orEmpty() -> seedState.recentQueries.getOrNull(1).orEmpty()
                    else -> ""
                }
                _uiState.value = _uiState.value.copy(query = next)
            }

            ContextSearchAction.ClearQuery -> {
                _uiState.value = _uiState.value.copy(query = "")
            }

            is ContextSearchAction.SelectScope -> {
                _uiState.value = _uiState.value.copy(selectedScope = action.scope)
            }

            is ContextSearchAction.SelectFilter -> {
                _uiState.value = _uiState.value.copy(selectedFilter = action.filter)
            }

            ContextSearchAction.OpenPrimaryTargetClicked -> Unit
        }
    }
}

private fun ContextSearchSeed.toUiState(): ContextSearchUiState = ContextSearchUiState(
    title = title,
    subtitle = subtitle,
    queryHint = queryHint,
    query = "",
    scopes = scopes,
    selectedScope = scopes.firstOrNull().orEmpty(),
    filters = filters,
    selectedFilter = filters.firstOrNull().orEmpty(),
    recentQueries = recentQueries,
    resultRows = resultRows,
    suggestionRows = suggestionRows,
    primaryActionLabel = primaryActionLabel,
)

internal class GameCalendarSearchViewModel : BaseContextSearchViewModel(
    seed = ContextSearchSeed(
        title = "Поиск по календарю игр",
        subtitle = "Контекстный поиск только по играм: даты, форматы, полигоны, участие и роли.",
        queryHint = "Night Raid / CQB / полигон / организатор",
        scopes = listOf("Игры", "Мои записи", "Организаторы"),
        filters = listOf("Все", "На неделе", "Выходные", "Турниры", "Сценарные"),
        recentQueries = listOf("night raid", "суббота cqb", "полигон северный"),
        resultRows = listOf(
            ShellWireframeRow("Night Raid North", "Сб 19:00 | Полигон Северный | 42 игрока", "игра"),
            ShellWireframeRow("CQB Sunday Mix", "Вс 11:00 | Bunker Arena | есть места", "игра"),
            ShellWireframeRow("Teiwaz_ / мои записи", "2 подтверждено, 1 ожидание ответа", "мой"),
        ),
        suggestionRows = listOf(
            ShellWireframeRow("Фильтр по ролям", "Медики, саппорты, водители, организаторы"),
            ShellWireframeRow("Фильтр по логистике", "Нужен трансфер / есть машина / ночёвка"),
        ),
        primaryActionLabel = "Открыть календарь",
    ),
)

internal class RideShareSearchViewModel : BaseContextSearchViewModel(
    seed = ContextSearchSeed(
        title = "Поиск попутчиков",
        subtitle = "Контекстный поиск поездок: ищет только предложения/запросы поездок, без игр, команд и товаров.",
        queryHint = "куда / откуда / есть места / ищу место / полигон",
        scopes = listOf("Поездки", "Есть места", "Ищу место"),
        filters = listOf("Все", "На игру", "Обратно", "Сегодня", "Выходные", "Трансфер"),
        recentQueries = listOf("полигон северный", "ищу место москва", "есть места cqb"),
        resultRows = listOf(
            ShellWireframeRow("Night Raid North • 3 места", "Москва → Полигон Северный | Сб 17:30 | Teiwaz_", "есть"),
            ShellWireframeRow("Bunker Arena • 1 место", "СПб → Bunker Arena | Вс 09:40 | Raven", "1"),
            ShellWireframeRow("Ищу место до сценарки", "Казань | 1 игрок + снаряга | Ghost", "запрос"),
        ),
        suggestionRows = listOf(
            ShellWireframeRow("Фильтр по багажу", "Отдельно отмечать машины с местом под крупные кейсы"),
            ShellWireframeRow("Расходы и взносы", "Показывать поездки с делением топлива/платный трансфер"),
        ),
        primaryActionLabel = "Открыть попутчиков",
    ),
)

internal class PolygonsSearchViewModel : BaseContextSearchViewModel(
    seed = ContextSearchSeed(
        title = "Поиск полигонов",
        subtitle = "Контекстный поиск только по полигонам: тип местности, инфраструктура, правила и доступность.",
        queryHint = "лес / cqb / парковка / ночные игры",
        scopes = listOf("Полигоны", "Правила", "Инфраструктура"),
        filters = listOf("Все", "Лес", "CQB", "Заброшка", "Ночные"),
        recentQueries = listOf("лес москва", "cqb парковка", "ночные игры"),
        resultRows = listOf(
            ShellWireframeRow("Полигон Северный", "Лес/поля | вода, парковка, техзона", "4.8"),
            ShellWireframeRow("Bunker Arena", "CQB | прокат, свет, хрон", "4.6"),
            ShellWireframeRow("Старая База", "Заброшка | ночные по записи", "4.5"),
        ),
        suggestionRows = listOf(
            ShellWireframeRow("Фильтр FPS", "Показывать полигоны с нужными лимитами"),
            ShellWireframeRow("Транспорт", "Сортировка по дистанции и доступности парковки"),
        ),
        primaryActionLabel = "Открыть полигоны",
    ),
)

internal class ShopsSearchViewModel : BaseContextSearchViewModel(
    seed = ContextSearchSeed(
        title = "Поиск магазинов",
        subtitle = "Контекстный поиск только по магазинам: категории, наличие, город, партнёры и акции.",
        queryHint = "привод / шары / аккумулятор / прокат",
        scopes = listOf("Магазины", "Акции", "Партнёры"),
        filters = listOf("Все", "Приводы", "Снаряга", "Расходники", "Прокат"),
        recentQueries = listOf("аккум 11.1", "очки cqb", "шары 0.28"),
        resultRows = listOf(
            ShellWireframeRow("Airsoft Hub", "Москва | приводы, тюнинг, доставка", "4.9"),
            ShellWireframeRow("CQB Store", "СПб | экипировка, защита, прокат", "4.7"),
            ShellWireframeRow("Field Supply", "Казань | расходники и быстрые сборы", "4.5"),
        ),
        suggestionRows = listOf(
            ShellWireframeRow("Только рядом", "Фильтр по городу и самовывозу"),
            ShellWireframeRow("Партнёрские цены", "Показывать только магазины-партнёры"),
        ),
        primaryActionLabel = "Открыть магазины",
    ),
)

internal class ServicesSearchViewModel : BaseContextSearchViewModel(
    seed = ContextSearchSeed(
        title = "Поиск услуг",
        subtitle = "Контекстный поиск только по услугам: ремонт, тюнинг, судьи, инструкторы, медиа и прокат.",
        queryHint = "ремонт aeg / инструктор / судья / фотограф",
        scopes = listOf("Исполнители", "Заявки", "Портфолио"),
        filters = listOf("Все", "Ремонт", "Тюнинг", "Судьи", "Инструкторы", "Медиа"),
        recentQueries = listOf("ремонт hop-up", "фотограф на игру", "судья сценарка"),
        resultRows = listOf(
            ShellWireframeRow("North Tech", "Ремонт/тюнинг AEG | Москва", "4.9"),
            ShellWireframeRow("Field Ref Team", "Судейство и контроль сценарок", "4.7"),
            ShellWireframeRow("Medic Training Lab", "Тренировки и подготовка медиков", "4.8"),
        ),
        suggestionRows = listOf(
            ShellWireframeRow("Проверенные исполнители", "Фильтр по рейтингу и верификации"),
            ShellWireframeRow("Запросы команд", "Поиск по активным заявкам на услуги"),
        ),
        primaryActionLabel = "Открыть услуги",
    ),
)

@Composable
fun GameCalendarSearchShellRoute(
    onOpenCalendar: () -> Unit = {},
) {
    val viewModel: GameCalendarSearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    ContextSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ContextSearchAction.OpenPrimaryTargetClicked -> onOpenCalendar()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun RideShareSearchShellRoute(
    onOpenRideShare: () -> Unit = {},
) {
    val viewModel: RideShareSearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    ContextSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ContextSearchAction.OpenPrimaryTargetClicked -> onOpenRideShare()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun PolygonsSearchShellRoute(
    onOpenPolygons: () -> Unit = {},
) {
    val viewModel: PolygonsSearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    ContextSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ContextSearchAction.OpenPrimaryTargetClicked -> onOpenPolygons()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun ShopsSearchShellRoute(
    onOpenShops: () -> Unit = {},
) {
    val viewModel: ShopsSearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    ContextSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ContextSearchAction.OpenPrimaryTargetClicked -> onOpenShops()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun ServicesSearchShellRoute(
    onOpenServices: () -> Unit = {},
) {
    val viewModel: ServicesSearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    ContextSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ContextSearchAction.OpenPrimaryTargetClicked -> onOpenServices()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ContextSearchScreen(
    uiState: ContextSearchUiState,
    onAction: (ContextSearchAction) -> Unit,
) {
    WireframePage(
        title = uiState.title,
        subtitle = uiState.subtitle,
        primaryActionLabel = uiState.primaryActionLabel,
        onPrimaryAction = { onAction(ContextSearchAction.OpenPrimaryTargetClicked) },
    ) {
        WireframeSection(
            title = "Запрос",
            subtitle = "Локальный поиск текущего раздела. Здесь будет строка ввода, debounce и история запросов.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Поиск" to if (uiState.query.isBlank()) "(пусто)" else uiState.query,
                    "Hint" to uiState.queryHint,
                    "Результаты" to uiState.resultRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.recentQueries.map { recent -> if (recent == uiState.query) "[$recent]" else recent },
            )
        }
        WireframeSection(
            title = "Область поиска",
            subtitle = "Контекстные вкладки внутри раздела: что именно ищем на этой странице.",
        ) {
            WireframeChipRow(
                labels = uiState.scopes.map { scope -> if (scope == uiState.selectedScope) "[$scope]" else scope },
            )
            WireframeChipRow(
                labels = uiState.filters.map { filter -> if (filter == uiState.selectedFilter) "[$filter]" else filter },
            )
        }
        WireframeSection(
            title = "Результаты",
            subtitle = "Предпросмотр выдачи только для текущего раздела (не глобальный поиск).",
        ) {
            uiState.resultRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Подсказки и быстрые фильтры",
            subtitle = "Заготовки будущих фильтров, сортировок и шаблонов запросов.",
        ) {
            uiState.suggestionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка action wiring до подключения реального backend/API.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ContextSearchAction.CycleDemoQuery) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Подставить demo-запрос")
                }
                OutlinedButton(
                    onClick = { onAction(ContextSearchAction.ClearQuery) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Очистить запрос")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedScope == uiState.scopes.firstOrNull()) {
                            uiState.scopes.getOrElse(1) { uiState.selectedScope }
                        } else {
                            uiState.scopes.firstOrNull().orEmpty()
                        }
                        onAction(ContextSearchAction.SelectScope(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Переключить область")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedFilter == uiState.filters.firstOrNull()) {
                            uiState.filters.getOrElse(1) { uiState.selectedFilter }
                        } else {
                            uiState.filters.firstOrNull().orEmpty()
                        }
                        onAction(ContextSearchAction.SelectFilter(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Переключить фильтр")
                }
            }
        }
    }
}
