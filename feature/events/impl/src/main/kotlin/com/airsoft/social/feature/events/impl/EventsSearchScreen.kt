package com.airsoft.social.feature.events.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.EventsRepository
import com.airsoft.social.core.model.GameEvent
import com.airsoft.social.core.model.GameFormat
import com.airsoft.social.core.model.RegistrationStatus
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventsSearchRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class EventsSearchUiState(
    val query: String = "",
    val filters: List<String> = listOf("Все", "CQB", "Сценарные", "Тренировки", "Открыта запись"),
    val selectedFilter: String = "Все",
    val resultRows: List<EventsSearchRow> = emptyList(),
)

sealed interface EventsSearchAction {
    data object CycleDemoQuery : EventsSearchAction
    data object ToggleFilter : EventsSearchAction
    data object ClearQuery : EventsSearchAction
    data object OpenFirstEventClicked : EventsSearchAction
}

@HiltViewModel
class EventsSearchViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsSearchUiState())
    val uiState: StateFlow<EventsSearchUiState> = _uiState.asStateFlow()

    private var events: List<GameEvent> = emptyList()

    init {
        viewModelScope.launch {
            eventsRepository.observeEvents().collect {
                events = it
                rebuildUiState()
            }
        }
    }

    fun onAction(action: EventsSearchAction) {
        when (action) {
            EventsSearchAction.CycleDemoQuery -> {
                val next = when (_uiState.value.query) {
                    "" -> "night"
                    "night" -> "cqb"
                    else -> ""
                }
                _uiState.value = _uiState.value.copy(query = next)
                rebuildUiState()
            }
            EventsSearchAction.ToggleFilter -> {
                val filters = _uiState.value.filters
                val idx = filters.indexOf(_uiState.value.selectedFilter)
                _uiState.value = _uiState.value.copy(
                    selectedFilter = filters[(if (idx < 0) 0 else idx + 1) % filters.size],
                )
                rebuildUiState()
            }
            EventsSearchAction.ClearQuery -> {
                _uiState.value = _uiState.value.copy(query = "")
                rebuildUiState()
            }
            EventsSearchAction.OpenFirstEventClicked -> Unit
        }
    }

    private fun rebuildUiState() {
        val query = _uiState.value.query.trim().lowercase(Locale.getDefault())
        val filter = _uiState.value.selectedFilter
        val rows = events
            .asSequence()
            .filter { matchesFilter(it, filter) }
            .filter { event ->
                query.isBlank() || listOf(
                    event.title,
                    event.organizerName,
                    event.fieldName.orEmpty(),
                    event.location.name,
                ).any { it.lowercase(Locale.getDefault()).contains(query) }
            }
            .sortedBy { it.startDate.time }
            .map { event ->
                EventsSearchRow(
                    id = event.id,
                    title = event.title,
                    subtitle = listOf(
                        eventDateLabel(event),
                        event.fieldName ?: event.location.name,
                        event.organizerName,
                    ).joinToString(" | "),
                    trailing = event.currentPlayers.toString(),
                )
            }
            .toList()
        _uiState.value = _uiState.value.copy(resultRows = rows)
    }
}

@Composable
fun EventsSearchRoute(
    onOpenEventDetail: (String) -> Unit,
    viewModel: EventsSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    EventsSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                EventsSearchAction.OpenFirstEventClicked -> uiState.resultRows.firstOrNull()?.id?.let(onOpenEventDetail)
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun EventsSearchScreen(
    uiState: EventsSearchUiState,
    onAction: (EventsSearchAction) -> Unit,
) {
    WireframePage(
        title = "Поиск игр",
        subtitle = "Контекстный поиск раздела игр: ищет только игры/турниры/тренировки.",
        primaryActionLabel = "Открыть первую игру",
        onPrimaryAction = { onAction(EventsSearchAction.OpenFirstEventClicked) },
    ) {
        WireframeSection(
            title = "Запрос",
            subtitle = "Поиск по названию игры, полигону, организатору и формату.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Запрос" to if (uiState.query.isBlank()) "(пусто)" else uiState.query,
                    "Фильтр" to uiState.selectedFilter,
                    "Найдено" to uiState.resultRows.size.toString(),
                ),
            )
            WireframeChipRow(listOf("night", "cqb", "турнир", "тренировка", "выходные"))
            WireframeChipRow(uiState.filters.map { if (it == uiState.selectedFilter) "[$it]" else it })
        }
        WireframeSection(
            title = "Результаты",
            subtitle = "Выдача только по событиям/играм, без полигонов и товаров.",
        ) {
            if (uiState.resultRows.isEmpty()) {
                WireframeItemRow("События не найдены", "Измените фильтр или запрос")
            } else {
                uiState.resultRows.forEach { WireframeItemRow(it.title, it.subtitle, it.trailing) }
            }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка поведения контекстного поиска игр.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(EventsSearchAction.CycleDemoQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Подставить demo-запрос")
                }
                OutlinedButton(onClick = { onAction(EventsSearchAction.ToggleFilter) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Переключить фильтр")
                }
                OutlinedButton(onClick = { onAction(EventsSearchAction.ClearQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Очистить запрос")
                }
            }
        }
    }
}

private fun matchesFilter(event: GameEvent, filter: String): Boolean = when (filter) {
    "CQB" -> event.gameFormat == GameFormat.CQB
    "Сценарные" -> event.gameFormat == GameFormat.SCENARIO
    "Тренировки" -> event.gameFormat == GameFormat.TRAINING
    "Открыта запись" -> event.registrationStatus == RegistrationStatus.OPEN
    else -> true
}

private fun eventDateLabel(event: GameEvent): String =
    SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(event.startDate)
