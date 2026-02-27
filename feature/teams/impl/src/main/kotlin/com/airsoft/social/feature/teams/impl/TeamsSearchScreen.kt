package com.airsoft.social.feature.teams.impl

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
import com.airsoft.social.core.data.TeamsRepository
import com.airsoft.social.core.model.GameStyle
import com.airsoft.social.core.model.Team
import com.airsoft.social.core.model.TeamRecruitingPostPreview
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeamsSearchRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class TeamsSearchUiState(
    val query: String = "",
    val scopes: List<String> = listOf("Команды", "Набор"),
    val selectedScope: String = "Команды",
    val filters: List<String> = listOf("Все", "Открытые", "Вериф.", "CQB", "Лес"),
    val selectedFilter: String = "Все",
    val teamRows: List<TeamsSearchRow> = emptyList(),
    val recruitingRows: List<TeamsSearchRow> = emptyList(),
)

sealed interface TeamsSearchAction {
    data object CycleDemoQuery : TeamsSearchAction
    data object ToggleScope : TeamsSearchAction
    data object ToggleFilter : TeamsSearchAction
    data object ClearQuery : TeamsSearchAction
    data object OpenFirstTeamClicked : TeamsSearchAction
}

@HiltViewModel
class TeamsSearchViewModel @Inject constructor(
    private val teamsRepository: TeamsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamsSearchUiState())
    val uiState: StateFlow<TeamsSearchUiState> = _uiState.asStateFlow()

    private var myTeam: Team? = null
    private var recruitingFeed: List<TeamRecruitingPostPreview> = emptyList()
    private var myTeamJob: Job? = null
    private var recruitingJob: Job? = null

    init {
        myTeamJob = viewModelScope.launch {
            teamsRepository.observeMyTeam().collect {
                myTeam = it
                rebuildUiState()
            }
        }
        recruitingJob = viewModelScope.launch {
            teamsRepository.observeRecruitingFeed().collect {
                recruitingFeed = it
                rebuildUiState()
            }
        }
    }

    fun onAction(action: TeamsSearchAction) {
        when (action) {
            TeamsSearchAction.CycleDemoQuery -> {
                val next = when (_uiState.value.query) {
                    "" -> "ew"
                    "ew" -> "медик"
                    else -> ""
                }
                _uiState.value = _uiState.value.copy(query = next)
                rebuildUiState()
            }
            TeamsSearchAction.ToggleScope -> {
                _uiState.value = _uiState.value.copy(
                    selectedScope = if (_uiState.value.selectedScope == "Команды") "Набор" else "Команды",
                )
            }
            TeamsSearchAction.ToggleFilter -> {
                val filters = _uiState.value.filters
                val idx = filters.indexOf(_uiState.value.selectedFilter)
                val next = filters[(if (idx < 0) 0 else idx + 1) % filters.size]
                _uiState.value = _uiState.value.copy(selectedFilter = next)
                rebuildUiState()
            }
            TeamsSearchAction.ClearQuery -> {
                _uiState.value = _uiState.value.copy(query = "")
                rebuildUiState()
            }
            TeamsSearchAction.OpenFirstTeamClicked -> Unit
        }
    }

    private fun rebuildUiState() {
        val query = _uiState.value.query.trim().lowercase(Locale.getDefault())
        val filter = _uiState.value.selectedFilter

        val teamRows = listOfNotNull(myTeam)
            .asSequence()
            .filter { team -> matchesTeamFilter(team, filter) }
            .filter { team ->
                query.isBlank() || listOf(team.name, team.shortName, team.region, team.description.orEmpty())
                    .any { it.lowercase(Locale.getDefault()).contains(query) }
            }
            .map { team ->
                TeamsSearchRow(
                    id = team.id,
                    title = "[${team.shortName}] ${team.name}",
                    subtitle = listOf(
                        team.region,
                        if (team.isOpenForJoin) "Открыт набор" else "Набор закрыт",
                        team.description ?: "Без описания",
                    ).joinToString(" | "),
                    trailing = if (team.isVerified) "V" else team.memberCount.toString(),
                )
            }
            .toList()

        val recruitingRows = recruitingFeed
            .asSequence()
            .filter { post ->
                query.isBlank() || listOf(post.title, post.subtitle, post.tags.joinToString(" "))
                    .any { it.lowercase(Locale.getDefault()).contains(query) }
            }
            .map { post ->
                TeamsSearchRow(
                    id = post.teamId,
                    title = post.title,
                    subtitle = post.subtitle,
                    trailing = post.actionLabel,
                )
            }
            .toList()

        _uiState.value = _uiState.value.copy(teamRows = teamRows, recruitingRows = recruitingRows)
    }
}

@Composable
fun TeamsSearchRoute(
    onOpenTeamDetail: (String) -> Unit,
    viewModel: TeamsSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    TeamsSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                TeamsSearchAction.OpenFirstTeamClicked -> {
                    (uiState.teamRows.firstOrNull() ?: uiState.recruitingRows.firstOrNull())?.id?.let(onOpenTeamDetail)
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun TeamsSearchScreen(
    uiState: TeamsSearchUiState,
    onAction: (TeamsSearchAction) -> Unit,
) {
    WireframePage(
        title = "Поиск команд",
        subtitle = "Контекстный поиск раздела команд: ищет только команды и посты набора, без событий/товаров.",
        primaryActionLabel = "Открыть первую команду",
        onPrimaryAction = { onAction(TeamsSearchAction.OpenFirstTeamClicked) },
    ) {
        WireframeSection(
            title = "Запрос и режим",
            subtitle = "Здесь будет строка поиска по командам и постам набора.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Запрос" to if (uiState.query.isBlank()) "(пусто)" else uiState.query,
                    "Область" to uiState.selectedScope,
                    "Фильтр" to uiState.selectedFilter,
                ),
            )
            WireframeChipRow(listOf("команда", "набор", "медик", "cqb", "лес"))
        }
        WireframeSection(title = "Область и фильтры", subtitle = "Фильтры поиска только по командам.") {
            WireframeChipRow(uiState.scopes.map { if (it == uiState.selectedScope) "[$it]" else it })
            WireframeChipRow(uiState.filters.map { if (it == uiState.selectedFilter) "[$it]" else it })
        }
        WireframeSection(title = "Команды", subtitle = "Результаты по карточкам команд.") {
            if (uiState.teamRows.isEmpty()) {
                WireframeItemRow("Команды не найдены", "Измените запрос или фильтр")
            } else {
                uiState.teamRows.forEach { WireframeItemRow(it.title, it.subtitle, it.trailing) }
            }
        }
        WireframeSection(title = "Посты набора", subtitle = "Результаты по ленте набора в команды.") {
            if (uiState.recruitingRows.isEmpty()) {
                WireframeItemRow("Посты набора не найдены", "Поменяйте запрос")
            } else {
                uiState.recruitingRows.forEach { WireframeItemRow(it.title, it.subtitle, it.trailing) }
            }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка поиска/навигации для раздела команд.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(TeamsSearchAction.CycleDemoQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Подставить demo-запрос")
                }
                OutlinedButton(onClick = { onAction(TeamsSearchAction.ToggleScope) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Переключить область")
                }
                OutlinedButton(onClick = { onAction(TeamsSearchAction.ToggleFilter) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Переключить фильтр")
                }
                OutlinedButton(onClick = { onAction(TeamsSearchAction.ClearQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Очистить запрос")
                }
            }
        }
    }
}

private fun matchesTeamFilter(team: Team, filter: String): Boolean = when (filter) {
    "Открытые" -> team.isOpenForJoin
    "Вериф." -> team.isVerified
    "CQB" -> team.gameStyles.contains(GameStyle.CQB)
    "Лес" -> team.gameStyles.contains(GameStyle.FOREST)
    else -> true
}
