package com.airsoft.social.feature.chats.impl

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
import com.airsoft.social.core.data.ChatsRepository
import com.airsoft.social.core.model.ChatThreadPreview
import com.airsoft.social.core.model.ChatType
import com.airsoft.social.core.model.PlayerCardPreview
import com.airsoft.social.core.model.UserRole
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

data class ChatsSearchRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class ChatsSearchUiState(
    val query: String = "",
    val queryHint: String = "игрок / команда / чат / позывной",
    val scopes: List<String> = listOf("Чаты", "Игроки"),
    val selectedScope: String = "Чаты",
    val filters: List<String> = listOf("Все", "Непрочитанные", "Командные", "Личные"),
    val selectedFilter: String = "Все",
    val chatRows: List<ChatsSearchRow> = emptyList(),
    val playerRows: List<ChatsSearchRow> = emptyList(),
)

sealed interface ChatsSearchAction {
    data object CycleDemoQuery : ChatsSearchAction
    data object ClearQuery : ChatsSearchAction
    data object ToggleScope : ChatsSearchAction
    data object ToggleFilter : ChatsSearchAction
    data object OpenFirstChatClicked : ChatsSearchAction
    data object OpenFirstPlayerClicked : ChatsSearchAction
}

@HiltViewModel
class ChatsSearchViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatsSearchUiState())
    val uiState: StateFlow<ChatsSearchUiState> = _uiState.asStateFlow()

    private var threads: List<ChatThreadPreview> = emptyList()
    private var players: List<PlayerCardPreview> = emptyList()
    private var threadsJob: Job? = null
    private var playersJob: Job? = null

    init {
        threadsJob = viewModelScope.launch {
            chatsRepository.observeThreads().collect {
                threads = it
                rebuildUiState()
            }
        }
        playersJob = viewModelScope.launch {
            chatsRepository.observePlayersDirectory().collect {
                players = it
                rebuildUiState()
            }
        }
    }

    fun onAction(action: ChatsSearchAction) {
        when (action) {
            ChatsSearchAction.CycleDemoQuery -> {
                val current = _uiState.value.query
                val next = when (current) {
                    "" -> "ghost"
                    "ghost" -> "ew"
                    else -> ""
                }
                _uiState.value = _uiState.value.copy(query = next)
                rebuildUiState()
            }

            ChatsSearchAction.ClearQuery -> {
                _uiState.value = _uiState.value.copy(query = "")
                rebuildUiState()
            }

            ChatsSearchAction.ToggleScope -> {
                val next = if (_uiState.value.selectedScope == "Чаты") "Игроки" else "Чаты"
                _uiState.value = _uiState.value.copy(selectedScope = next)
            }

            ChatsSearchAction.ToggleFilter -> {
                val current = _uiState.value.selectedFilter
                val nextIndex = (_uiState.value.filters.indexOf(current) + 1)
                    .takeIf { it >= 0 } ?: 0
                val next = _uiState.value.filters[nextIndex % _uiState.value.filters.size]
                _uiState.value = _uiState.value.copy(selectedFilter = next)
                rebuildUiState()
            }

            ChatsSearchAction.OpenFirstChatClicked,
            ChatsSearchAction.OpenFirstPlayerClicked,
            -> Unit
        }
    }

    private fun rebuildUiState() {
        val query = _uiState.value.query.trim().lowercase(Locale.getDefault())
        val selectedFilter = _uiState.value.selectedFilter

        val filteredThreads = threads
            .asSequence()
            .filter { thread ->
                when (selectedFilter) {
                    "Непрочитанные" -> thread.unreadCount > 0
                    "Командные" -> thread.chatType == ChatType.TEAM
                    "Личные" -> thread.chatType == ChatType.DIRECT
                    else -> true
                }
            }
            .filter { thread ->
                query.isBlank() || listOf(thread.title, thread.lastMessage.orEmpty())
                    .any { it.lowercase(Locale.getDefault()).contains(query) }
            }
            .sortedByDescending { it.updatedAtMs }
            .map { thread ->
                ChatsSearchRow(
                    id = thread.chatId,
                    title = thread.title,
                    subtitle = thread.lastMessage ?: "Без сообщений",
                    trailing = if (thread.unreadCount > 0) thread.unreadCount.toString() else null,
                )
            }
            .toList()

        val filteredPlayers = players
            .asSequence()
            .filter { it.userId != "self" }
            .filter { user ->
                query.isBlank() || listOf(
                    user.callsign,
                    user.teamName.orEmpty(),
                    user.region.orEmpty(),
                    user.bio.orEmpty(),
                ).any { it.lowercase(Locale.getDefault()).contains(query) }
            }
            .sortedBy { it.callsign.lowercase(Locale.getDefault()) }
            .map { user ->
                ChatsSearchRow(
                    id = user.userId,
                    title = user.callsign,
                    subtitle = buildList {
                        add(user.roles.firstOrNull()?.let(::roleLabel) ?: "Игрок")
                        add(user.region ?: "Регион не указан")
                        user.teamName?.let(::add)
                    }.joinToString(" | "),
                    trailing = user.rating?.let { String.format(Locale.US, "%.1f", it) },
                )
            }
            .toList()

        _uiState.value = _uiState.value.copy(
            chatRows = filteredThreads,
            playerRows = filteredPlayers,
        )
    }
}

@Composable
fun ChatsSearchRoute(
    onOpenChatRoom: (String) -> Unit,
    onOpenPlayerCard: (String) -> Unit,
    viewModel: ChatsSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ChatsSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ChatsSearchAction.OpenFirstChatClicked -> {
                    uiState.chatRows.firstOrNull()?.let { onOpenChatRoom(it.id) }
                }
                ChatsSearchAction.OpenFirstPlayerClicked -> {
                    uiState.playerRows.firstOrNull()?.let { onOpenPlayerCard(it.id) }
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ChatsSearchScreen(
    uiState: ChatsSearchUiState,
    onAction: (ChatsSearchAction) -> Unit,
) {
    WireframePage(
        title = "Поиск по чатам и игрокам",
        subtitle = "Контекстный поиск раздела чатов: ищет только чаты и игроков, без смешивания с командами/товарами.",
        primaryActionLabel = "Открыть первый чат",
        onPrimaryAction = { onAction(ChatsSearchAction.OpenFirstChatClicked) },
    ) {
        WireframeSection(
            title = "Запрос",
            subtitle = "Здесь будет строка поиска, debounce и recent queries для чатов/игроков.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Запрос" to if (uiState.query.isBlank()) "(пусто)" else uiState.query,
                    "Область" to uiState.selectedScope,
                    "Фильтр" to uiState.selectedFilter,
                ),
            )
            WireframeChipRow(labels = listOf(uiState.queryHint, "ghost", "ew", "непрочитанные"))
        }
        WireframeSection(
            title = "Область и фильтры",
            subtitle = "Поиск только по чатам/игрокам внутри раздела коммуникаций.",
        ) {
            WireframeChipRow(
                labels = uiState.scopes.map { if (it == uiState.selectedScope) "[$it]" else it },
            )
            WireframeChipRow(
                labels = uiState.filters.map { if (it == uiState.selectedFilter) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Найденные чаты",
            subtitle = "Командные, личные и групповые чаты из ChatsRepository.",
        ) {
            if (uiState.chatRows.isEmpty()) {
                WireframeItemRow("Ничего не найдено", "Измените запрос или фильтр")
            } else {
                uiState.chatRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
            }
        }
        WireframeSection(
            title = "Найденные игроки",
            subtitle = "Каталог игроков/контактов внутри раздела чатов.",
        ) {
            if (uiState.playerRows.isEmpty()) {
                WireframeItemRow("Игроки не найдены", "Попробуйте другой позывной или регион")
            } else {
                uiState.playerRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
            }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка route/action wiring до полноценного поиска с backend.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ChatsSearchAction.CycleDemoQuery) },
                    modifier = Modifier.fillMaxWidth(),
                ) { androidx.compose.material3.Text("Подставить demo-запрос") }
                OutlinedButton(
                    onClick = { onAction(ChatsSearchAction.ToggleScope) },
                    modifier = Modifier.fillMaxWidth(),
                ) { androidx.compose.material3.Text("Переключить область") }
                OutlinedButton(
                    onClick = { onAction(ChatsSearchAction.ToggleFilter) },
                    modifier = Modifier.fillMaxWidth(),
                ) { androidx.compose.material3.Text("Переключить фильтр") }
                OutlinedButton(
                    onClick = { onAction(ChatsSearchAction.OpenFirstPlayerClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { androidx.compose.material3.Text("Открыть карточку первого игрока") }
                OutlinedButton(
                    onClick = { onAction(ChatsSearchAction.ClearQuery) },
                    modifier = Modifier.fillMaxWidth(),
                ) { androidx.compose.material3.Text("Очистить запрос") }
            }
        }
    }
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.PLAYER -> "Игрок"
    UserRole.CAPTAIN -> "Капитан"
    UserRole.ORGANIZER -> "Организатор"
    UserRole.SELLER -> "Продавец"
    UserRole.TECH_MASTER -> "Техмастер"
    UserRole.SHOP_PARTNER -> "Партнер"
    UserRole.MODERATOR -> "Модератор"
    UserRole.ADMIN -> "Админ"
}
