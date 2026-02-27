package com.airsoft.social.feature.search.impl

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
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.search.api.SearchFeatureApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchWireframeRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class SearchUiState(
    val selectedCategory: String = "Игроки",
    val categories: List<String> = listOf("Игроки", "Команды", "Игры", "Барахолка", "Полигоны"),
    val recentRows: List<SearchWireframeRow> = listOf(
        SearchWireframeRow("ночная игра москва", "Недавний запрос"),
        SearchWireframeRow("команда cqb", "Недавний запрос"),
        SearchWireframeRow("плитник multicam", "Недавний запрос"),
    ),
    val suggestionRows: List<SearchWireframeRow> = listOf(
        SearchWireframeRow("Игроки рядом", "Поиск игроков / матчмейкинг"),
        SearchWireframeRow("Команды ищут медиков", "Доска набора в команды"),
        SearchWireframeRow("Игры на выходных", "Ближайшие игры и турниры"),
        SearchWireframeRow("Б/у приводы", "Объявления барахолки"),
    ),
)

sealed interface SearchAction {
    data class SelectCategory(val category: String) : SearchAction
    data object OpenPlayersClicked : SearchAction
    data object OpenTeamsClicked : SearchAction
    data object OpenEventsClicked : SearchAction
    data object OpenMarketplaceClicked : SearchAction
}

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.SelectCategory -> _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            SearchAction.OpenPlayersClicked -> Unit
            SearchAction.OpenTeamsClicked -> Unit
            SearchAction.OpenEventsClicked -> Unit
            SearchAction.OpenMarketplaceClicked -> Unit
        }
    }
}

@Composable
fun SearchFeatureRoute(
    onOpenPlayers: () -> Unit = {},
    onOpenTeams: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
) {
    val uiState by searchViewModel.uiState.collectAsState()
    SearchFeatureScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SearchAction.OpenPlayersClicked -> onOpenPlayers()
                SearchAction.OpenTeamsClicked -> onOpenTeams()
                SearchAction.OpenEventsClicked -> onOpenEvents()
                SearchAction.OpenMarketplaceClicked -> onOpenMarketplace()
                is SearchAction.SelectCategory -> searchViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun SearchFeatureScreen(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
) {
    WireframePage(
        title = SearchFeatureApi.contract.title,
        subtitle = "Каркас глобального поиска по игрокам, командам, играм, барахолке и полигонам.",
        primaryActionLabel = "Искать (заглушка)",
    ) {
        WireframeSection(
            title = "Область поиска",
            subtitle = "Категории и будущий строковый поиск.",
        ) {
            WireframeChipRow(
                labels = uiState.categories.map { category ->
                    if (category == uiState.selectedCategory) "[$category]" else category
                },
            )
        }
        WireframeSection(
            title = "Недавние запросы",
            subtitle = "Заглушки истории поиска из локального хранилища.",
        ) {
            uiState.recentRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Быстрые переходы",
            subtitle = "Переход в основные разделы, пока backend поиска не готов.",
        ) {
            uiState.suggestionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(SearchAction.OpenPlayersClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть Игроки / Чаты") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenTeamsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть Команды") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenEventsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть Игры") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenMarketplaceClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть Барахолку") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == "Игроки") "Барахолка" else "Игроки"
                        onAction(SearchAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить категорию") }
            }
        }
    }
}

