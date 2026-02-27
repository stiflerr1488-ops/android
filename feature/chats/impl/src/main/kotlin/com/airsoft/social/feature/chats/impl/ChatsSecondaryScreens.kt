package com.airsoft.social.feature.chats.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.ChatsRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.ChatMessageItem
import com.airsoft.social.core.model.ChatThreadPreview
import com.airsoft.social.core.model.MessageDeliveryState
import com.airsoft.social.core.model.PlayerCardPreview
import com.airsoft.social.core.model.UserRole
import com.airsoft.social.core.model.toAccountAccess
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val CHAT_GUARD_EMAIL_REQUIRED = "chat_guard_email_required"

data class ChatRoomMessageRow(
    val sender: String,
    val content: String,
    val timeLabel: String,
)

data class ChatRoomUiState(
    val chatId: String = "chat-demo",
    val title: String = "Чат",
    val headerSubtitle: String = "0 сообщений",
    val soundLabel: String = "Звук вкл",
    val messages: List<ChatRoomMessageRow> = emptyList(),
    val canSendMessages: Boolean = false,
    val sendGuardMessage: String? = null,
    val composerChips: List<String> = listOf("Шаблон", "Вложить", "Фото", "Локация", "Голос"),
)

sealed interface ChatRoomAction {
    data object ToggleSound : ChatRoomAction
    data object SendDemoMessage : ChatRoomAction
    data class SubmitText(val text: String) : ChatRoomAction
}

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatRoomUiState())
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null
    private var threadsJob: Job? = null
    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.authState.collect { state ->
                val canSend = (state as? AuthState.SignedIn)
                    ?.session
                    ?.toAccountAccess()
                    ?.canSendChatMessages == true
                _uiState.value = _uiState.value.copy(
                    canSendMessages = canSend,
                    sendGuardMessage = if (canSend) null else CHAT_GUARD_EMAIL_REQUIRED,
                )
            }
        }
    }

    fun load(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        _uiState.value = _uiState.value.copy(chatId = chatId)

        threadsJob?.cancel()
        messagesJob?.cancel()

        threadsJob = viewModelScope.launch {
            chatsRepository.observeThreads().collect { threads ->
                val thread = threads.firstOrNull { it.chatId == chatId }
                _uiState.value = _uiState.value.copy(
                    title = chatRoomTitle(thread, chatId),
                    headerSubtitle = chatRoomHeaderSubtitle(thread, _uiState.value.messages),
                    soundLabel = if ((thread?.unreadCount ?: 0) > 0) "Звук вкл" else _uiState.value.soundLabel,
                )
            }
        }

        messagesJob = viewModelScope.launch {
            chatsRepository.observeMessages(chatId).collect { messages ->
                val rows = messages.takeLast(20).map(::chatMessageRow)
                _uiState.value = _uiState.value.copy(
                    messages = rows,
                    headerSubtitle = chatRoomHeaderSubtitle(null, rows),
                )
            }
        }

        viewModelScope.launch {
            chatsRepository.markChatRead(chatId)
        }
    }

    fun onAction(action: ChatRoomAction) {
        when (action) {
            ChatRoomAction.ToggleSound -> {
                _uiState.value = _uiState.value.copy(
                    soundLabel = if (_uiState.value.soundLabel == "Звук вкл") "Звук выкл" else "Звук вкл",
                )
            }

            ChatRoomAction.SendDemoMessage -> {
                sendMessage("Тестовое сообщение из нового shell")
            }

            is ChatRoomAction.SubmitText -> {
                sendMessage(action.text)
            }
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return
        val chatId = currentChatId ?: return
        if (!_uiState.value.canSendMessages) {
            _uiState.value = _uiState.value.copy(
                sendGuardMessage = CHAT_GUARD_EMAIL_REQUIRED,
            )
            return
        }
        viewModelScope.launch {
            chatsRepository.sendTextMessage(
                chatId = chatId,
                text = text.trim(),
            )
            chatsRepository.markChatRead(chatId)
        }
    }
}

@Composable
fun ChatRoomSkeletonRoute(
    chatId: String,
    onOpenAuth: () -> Unit = {},
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel(),
) {
    LaunchedEffect(chatId) {
        chatRoomViewModel.load(chatId)
    }
    val uiState by chatRoomViewModel.uiState.collectAsState()
    ChatRoomRouteScreen(
        uiState = uiState,
        onAction = chatRoomViewModel::onAction,
        onOpenAuth = onOpenAuth,
    )
}

@Composable
private fun ChatRoomRouteScreen(
    uiState: ChatRoomUiState,
    onAction: (ChatRoomAction) -> Unit,
    onOpenAuth: () -> Unit,
) {
    WireframePage(
        title = "Чат",
        subtitle = "Каркас экрана беседы с лентой, медиа и быстрыми действиями. ID чата: ${uiState.chatId}",
        primaryActionLabel = if (uiState.canSendMessages) {
            stringResource(R.string.chat_room_primary_send)
        } else {
            stringResource(R.string.chat_room_primary_registration_required)
        },
        onPrimaryAction = { onAction(ChatRoomAction.SendDemoMessage) },
    ) {
        WireframeSection(
            title = "Шапка чата",
            subtitle = "Участники, статус, закреплённое сообщение и настройки звука.",
        ) {
            WireframeItemRow(uiState.title, uiState.headerSubtitle, uiState.soundLabel)
            WireframeChipRow(listOf("Закреп", "Файлы", "Медиа", "Упоминания"))
            OutlinedButton(
                onClick = { onAction(ChatRoomAction.ToggleSound) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.material3.Text("Переключить звук")
            }
        }
        WireframeSection(
            title = "Лента сообщений",
            subtitle = "Data-driven лента сообщений через ChatsRepository.",
        ) {
            if (uiState.messages.isEmpty()) {
                WireframeItemRow("Сообщений нет", "Для этого чата пока нет данных")
            } else {
                uiState.messages.forEach { message ->
                    WireframeItemRow(message.sender, message.content, message.timeLabel)
                }
            }
        }
        WireframeSection(
            title = "Область ввода",
            subtitle = "Текст, вложения, голосовое, быстрые шаблоны.",
        ) {
            uiState.sendGuardMessage?.let { reason ->
                WireframeItemRow(
                    title = stringResource(R.string.chat_room_guard_title),
                    subtitle = resolveChatGuardMessage(reason),
                )
                OutlinedButton(
                    onClick = onOpenAuth,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text(stringResource(R.string.chat_room_action_open_auth))
                }
            }
            WireframeChipRow(uiState.composerChips)
        }
    }
}

data class PlayerCardUiState(
    val playerId: String = "player-demo",
    val callsign: String = "Игрок",
    val profileSubtitle: String = "Профиль не найден",
    val ratingLabel: String = "-",
    val tags: List<String> = emptyList(),
    val availabilityRows: List<Pair<String, String>> = emptyList(),
)

sealed interface PlayerCardAction {
    data object ToggleFavorite : PlayerCardAction
}

@HiltViewModel
class PlayerCardViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerCardUiState())
    val uiState: StateFlow<PlayerCardUiState> = _uiState.asStateFlow()

    private var currentPlayerId: String? = null
    private var observeJob: Job? = null

    fun load(playerId: String) {
        if (currentPlayerId == playerId) return
        currentPlayerId = playerId
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            chatsRepository.observePlayerCard(playerId).collect { player ->
                _uiState.value = playerCardState(user = player, playerId = playerId)
            }
        }
    }

    fun onAction(action: PlayerCardAction) {
        when (action) {
            PlayerCardAction.ToggleFavorite -> {
                val tags = _uiState.value.tags.toMutableList()
                val favorite = "В закладки"
                if (tags.contains(favorite)) tags.remove(favorite) else tags.add(0, favorite)
                _uiState.value = _uiState.value.copy(tags = tags)
            }
        }
    }
}

@Composable
fun PlayerCardSkeletonRoute(
    playerId: String,
    playerCardViewModel: PlayerCardViewModel = hiltViewModel(),
) {
    LaunchedEffect(playerId) {
        playerCardViewModel.load(playerId)
    }
    val uiState by playerCardViewModel.uiState.collectAsState()
    PlayerCardRouteScreen(
        uiState = uiState,
        onAction = playerCardViewModel::onAction,
    )
}

@Composable
private fun PlayerCardRouteScreen(
    uiState: PlayerCardUiState,
    onAction: (PlayerCardAction) -> Unit,
) {
    WireframePage(
        title = "Карточка игрока",
        subtitle = "Каркас публичной карточки игрока из чатов/поиска. ID игрока: ${uiState.playerId}",
        primaryActionLabel = "Начать чат",
    ) {
        WireframeSection(
            title = "Профиль",
            subtitle = "Позывной, город, теги ролей, репутация.",
        ) {
            WireframeItemRow(uiState.callsign, uiState.profileSubtitle, uiState.ratingLabel)
            WireframeChipRow(uiState.tags)
        }
        WireframeSection(
            title = "Доступность",
            subtitle = "Предпочтительные дни, дистанция, форматы и статус по команде.",
        ) {
            uiState.availabilityRows.forEach { (title, subtitle) ->
                WireframeItemRow(title, subtitle)
            }
        }
        WireframeSection(
            title = "Быстрые действия",
            subtitle = "Пригласить, закладки, жалоба, share.",
        ) {
            WireframeChipRow(listOf("Пригласить", "Поделиться", "Жалоба"))
            OutlinedButton(
                onClick = { onAction(PlayerCardAction.ToggleFavorite) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.material3.Text("Переключить закладки")
            }
        }
    }
}

private fun chatRoomTitle(chat: ChatThreadPreview?, chatId: String): String =
    chat?.title ?: "Чат не найден ($chatId)"

private fun chatRoomHeaderSubtitle(
    chat: ChatThreadPreview?,
    messages: List<ChatRoomMessageRow>,
): String {
    val unread = chat?.unreadCount ?: 0
    val last = messages.lastOrNull()?.timeLabel?.substringBefore(" •") ?: "--:--"
    return "${messages.size} сообщений | непрочитано: $unread | $last"
}

private fun chatMessageRow(message: ChatMessageItem): ChatRoomMessageRow {
    val baseTime = timeLabel(Date(message.createdAtMs))
    val delivery = when (message.deliveryState) {
        MessageDeliveryState.SENDING -> "Отправка"
        MessageDeliveryState.SENT -> if (message.isMine) "Отправлено" else null
        MessageDeliveryState.FAILED -> "Ошибка"
    }
    val timeLabel = if (delivery != null) "$baseTime • $delivery" else baseTime
    return ChatRoomMessageRow(
        sender = message.senderCallsign,
        content = if (message.isDeleted) "[Удалено]" else message.content,
        timeLabel = timeLabel,
    )
}

private fun timeLabel(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

@Composable
private fun resolveChatGuardMessage(message: String): String = when (message) {
    CHAT_GUARD_EMAIL_REQUIRED -> stringResource(R.string.chat_guard_email_required)
    else -> message
}

private fun playerCardState(
    user: PlayerCardPreview?,
    playerId: String,
): PlayerCardUiState {
    if (user == null) {
        return PlayerCardUiState(
            playerId = playerId,
            callsign = "Игрок не найден",
            profileSubtitle = "Проверьте userId в маршруте",
            tags = listOf("Профиль пуст"),
            availabilityRows = listOf("Статус" to "Нет данных"),
        )
    }
    val roleTags = user.roles.map(::userRoleChipLabel)
    return PlayerCardUiState(
        playerId = playerId,
        callsign = user.callsign,
        profileSubtitle = buildList {
            add(user.region ?: "Регион не указан")
            add(user.roles.firstOrNull()?.let(::userRoleChipLabel) ?: "Игрок")
            if (!user.bio.isNullOrBlank()) add(user.bio)
        }.joinToString(" | "),
        ratingLabel = user.rating?.let { String.format(Locale.US, "%.1f", it) } ?: "-",
        tags = (roleTags + listOf("CQB", "Лес", "Ночные игры")).distinct(),
        availabilityRows = listOf(
            "Команда" to (user.teamName ?: "Ищет команду"),
            "Статус" to if (user.isOnline) "Сейчас онлайн" else "Оффлайн",
            "Доступность" to "Пт вечер / Сб / Вс",
        ),
    )
}

private fun userRoleChipLabel(role: UserRole): String = when (role) {
    UserRole.PLAYER -> "Игрок"
    UserRole.CAPTAIN -> "Капитан"
    UserRole.ORGANIZER -> "Организатор"
    UserRole.SELLER -> "Продавец"
    UserRole.TECH_MASTER -> "Техмастер"
    UserRole.SHOP_PARTNER -> "Партнер"
    UserRole.MODERATOR -> "Модератор"
    UserRole.ADMIN -> "Админ"
}
