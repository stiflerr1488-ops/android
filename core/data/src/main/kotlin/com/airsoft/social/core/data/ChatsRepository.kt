package com.airsoft.social.core.data

import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.database.ChatMessageEntity
import com.airsoft.social.core.database.ChatThreadEntity
import com.airsoft.social.core.database.ChatsDao
import com.airsoft.social.core.database.MessagesDao
import com.airsoft.social.core.model.Chat
import com.airsoft.social.core.model.ChatMessage
import com.airsoft.social.core.model.ChatMessageItem
import com.airsoft.social.core.model.ChatThreadPreview
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.MessageDeliveryState
import com.airsoft.social.core.model.PlayerCardPreview
import com.airsoft.social.core.model.toAccountAccess
import com.airsoft.social.core.network.ChatsRemoteDataSource
import com.airsoft.social.core.network.MessagesRemoteDataSource
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ChatsRepository {
    fun observeThreads(): Flow<List<ChatThreadPreview>>
    fun observePlayersDirectory(): Flow<List<PlayerCardPreview>>
    fun observeMessages(chatId: String): Flow<List<ChatMessageItem>>
    suspend fun sendTextMessage(chatId: String, text: String): AppResult<Unit>
    suspend fun markChatRead(chatId: String): AppResult<Unit>
    fun observePlayerCard(userId: String): Flow<PlayerCardPreview?>
}

class OfflineFirstChatsRepository(
    private val localDataSource: ChatsLocalDataSource,
    private val chatsRemoteDataSource: ChatsRemoteDataSource,
    private val messagesRemoteDataSource: MessagesRemoteDataSource,
    private val sessionRepository: SessionRepository? = null,
) : ChatsRepository {
    private val seedMutex = Mutex()
    @Volatile
    private var seeded = false

    override fun observeThreads(): Flow<List<ChatThreadPreview>> =
        localDataSource.observeThreads()
            .onStart { ensureSeeded() }
            .map { threads -> threads.map(::threadPreviewFromEntity) }

    override fun observePlayersDirectory(): Flow<List<PlayerCardPreview>> = flow {
        ensureSeeded()
        emit(chatsRemoteDataSource.fetchUsers().map(::playerCardPreviewFromUser))
    }

    override fun observeMessages(chatId: String): Flow<List<ChatMessageItem>> =
        localDataSource.observeMessages(chatId)
            .onStart { ensureSeeded() }
            .map { messages -> messages.map(::messageItemFromEntity) }

    override suspend fun sendTextMessage(chatId: String, text: String): AppResult<Unit> {
        val normalized = text.trim()
        if (normalized.isEmpty()) return AppResult.Failure(AppError.Validation("Пустое сообщение"))

        if (!canCurrentSessionSendMessages()) return AppResult.Failure(AppError.Unauthorized)
        ensureSeeded()

        val now = System.currentTimeMillis()
        val localMessageId = "local-${UUID.randomUUID()}"
        val sender = resolveSenderIdentity()
        val pendingEntity = ChatMessageEntity(
            id = localMessageId,
            chatId = chatId,
            senderId = sender.id,
            senderCallsign = sender.callsign,
            content = normalized,
            createdAtMs = now,
            isMine = true,
            deliveryState = MessageDeliveryState.SENDING.name,
            isRead = true,
            isDeleted = false,
        )
        localDataSource.upsertMessage(pendingEntity)
        localDataSource.updateThreadLastMessage(chatId = chatId, lastMessage = normalized, updatedAtMs = now)
        localDataSource.markChatRead(chatId)

        return when (val result = messagesRemoteDataSource.sendTextMessage(chatId = chatId, text = normalized)) {
            is AppResult.Success -> {
                localDataSource.updateMessageDeliveryState(localMessageId, MessageDeliveryState.SENT)
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> {
                localDataSource.updateMessageDeliveryState(localMessageId, MessageDeliveryState.FAILED)
                result
            }
        }
    }

    override suspend fun markChatRead(chatId: String): AppResult<Unit> = try {
        ensureSeeded()
        localDataSource.markChatRead(chatId)
        AppResult.Success(Unit)
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.ThrowableError(throwable))
    }

    override fun observePlayerCard(userId: String): Flow<PlayerCardPreview?> = flow {
        ensureSeeded()
        emit(chatsRemoteDataSource.getUser(userId)?.let(::playerCardPreviewFromUser))
    }

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            if (localDataSource.threadCount() > 0) {
                seeded = true
                return
            }
            val threads = chatsRemoteDataSource.fetchThreads()
            localDataSource.upsertThreads(threads.map(::threadEntityFromChat))
            threads.forEach { thread ->
                val messages = messagesRemoteDataSource.fetchMessages(thread.id)
                if (messages.isNotEmpty()) {
                    localDataSource.upsertMessages(messages.map(::messageEntityFromChatMessage))
                }
            }
            seeded = true
        }
    }

    private suspend fun resolveSenderIdentity(): SenderIdentity {
        val session = when (val state = sessionRepository?.currentAuthState()) {
            is AuthState.SignedIn -> state.session
            else -> null
        }
        return SenderIdentity(
            id = session?.userId ?: SELF_USER_ID,
            callsign = session?.displayName?.takeIf { it.isNotBlank() } ?: SELF_CALLSIGN,
        )
    }

    private suspend fun canCurrentSessionSendMessages(): Boolean {
        val state = sessionRepository?.currentAuthState() ?: return true
        val signedIn = state as? AuthState.SignedIn ?: return false
        return signedIn.session.toAccountAccess().canSendChatMessages
    }

    companion object {
        internal const val SELF_USER_ID = "self"
        internal const val SELF_CALLSIGN = "Teiwaz_"
    }
}

private data class SenderIdentity(
    val id: String,
    val callsign: String,
)

interface ChatsLocalDataSource {
    fun observeThreads(): Flow<List<ChatThreadEntity>>
    fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>>
    suspend fun threadCount(): Int
    suspend fun upsertThreads(entities: List<ChatThreadEntity>)
    suspend fun upsertMessages(entities: List<ChatMessageEntity>)
    suspend fun upsertMessage(entity: ChatMessageEntity)
    suspend fun updateThreadLastMessage(chatId: String, lastMessage: String?, updatedAtMs: Long)
    suspend fun updateMessageDeliveryState(messageId: String, deliveryState: MessageDeliveryState)
    suspend fun markChatRead(chatId: String)
}

class RoomChatsLocalDataSource(
    private val chatsDao: ChatsDao,
    private val messagesDao: MessagesDao,
) : ChatsLocalDataSource {
    override fun observeThreads(): Flow<List<ChatThreadEntity>> = chatsDao.observeAll()

    override fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>> = messagesDao.observeByChat(chatId)

    override suspend fun threadCount(): Int = chatsDao.count()

    override suspend fun upsertThreads(entities: List<ChatThreadEntity>) {
        chatsDao.upsertAll(entities)
    }

    override suspend fun upsertMessages(entities: List<ChatMessageEntity>) {
        messagesDao.upsertAll(entities)
    }

    override suspend fun upsertMessage(entity: ChatMessageEntity) {
        messagesDao.upsert(entity)
    }

    override suspend fun updateThreadLastMessage(chatId: String, lastMessage: String?, updatedAtMs: Long) {
        chatsDao.updateLastMessage(chatId = chatId, lastMessage = lastMessage, updatedAtMs = updatedAtMs)
    }

    override suspend fun updateMessageDeliveryState(messageId: String, deliveryState: MessageDeliveryState) {
        messagesDao.updateDeliveryState(messageId = messageId, deliveryState = deliveryState.name)
    }

    override suspend fun markChatRead(chatId: String) {
        chatsDao.markRead(chatId)
        messagesDao.markRead(chatId)
    }
}

class PreviewChatsRemoteDataSource(
    private val previewRepository: SocialPreviewRepository,
) : ChatsRemoteDataSource {
    override suspend fun fetchThreads(): List<Chat> = previewRepository.listChats()

    override suspend fun fetchUsers() = previewRepository.listUsers()

    override suspend fun getUser(userId: String) = previewRepository.getUser(userId)
}

class PreviewMessagesRemoteDataSource(
    private val previewRepository: SocialPreviewRepository,
    private val shouldFailSend: (chatId: String, text: String) -> Boolean = { _, text ->
        text.contains("#fail", ignoreCase = true)
    },
) : MessagesRemoteDataSource {
    private val sentMessagesState = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    override suspend fun fetchMessages(chatId: String): List<ChatMessage> {
        val seeded = previewRepository.listChatMessages(chatId)
        val localExtra = sentMessagesState.value[chatId].orEmpty()
        return (seeded + localExtra).sortedBy { it.createdAt.time }
    }

    override suspend fun sendTextMessage(chatId: String, text: String): AppResult<Unit> {
        if (shouldFailSend(chatId, text)) {
            return AppResult.Failure(AppError.Network("Fake send failure"))
        }
        val message = ChatMessage(
            id = "remote-${UUID.randomUUID()}",
            chatId = chatId,
            senderId = OfflineFirstChatsRepository.SELF_USER_ID,
            senderCallsign = OfflineFirstChatsRepository.SELF_CALLSIGN,
            content = text,
            createdAt = java.util.Date(),
            isRead = true,
        )
        val current = sentMessagesState.value.toMutableMap()
        val list = current[chatId].orEmpty().toMutableList()
        list += message
        current[chatId] = list
        sentMessagesState.value = current
        return AppResult.Success(Unit)
    }
}

private fun threadEntityFromChat(chat: Chat): ChatThreadEntity = ChatThreadEntity(
    id = chat.id,
    chatType = chat.type.name,
    title = chat.name ?: defaultChatTitle(chat),
    lastMessage = chat.lastMessage,
    unreadCount = chat.unreadCount,
    updatedAtMs = chat.updatedAt,
)

private fun threadPreviewFromEntity(entity: ChatThreadEntity): ChatThreadPreview = ChatThreadPreview(
    chatId = entity.id,
    title = entity.title,
    chatType = enumValueOf<com.airsoft.social.core.model.ChatType>(entity.chatType),
    lastMessage = entity.lastMessage,
    unreadCount = entity.unreadCount,
    updatedAtMs = entity.updatedAtMs,
)

private fun messageEntityFromChatMessage(message: ChatMessage): ChatMessageEntity = ChatMessageEntity(
    id = message.id,
    chatId = message.chatId,
    senderId = message.senderId,
    senderCallsign = message.senderCallsign,
    content = message.content,
    createdAtMs = message.createdAt.time,
    isMine = message.senderId == OfflineFirstChatsRepository.SELF_USER_ID,
    deliveryState = MessageDeliveryState.SENT.name,
    isRead = message.isRead,
    isDeleted = message.isDeleted,
)

private fun messageItemFromEntity(entity: ChatMessageEntity): ChatMessageItem = ChatMessageItem(
    messageId = entity.id,
    chatId = entity.chatId,
    senderId = entity.senderId,
    senderCallsign = entity.senderCallsign,
    content = entity.content,
    createdAtMs = entity.createdAtMs,
    isMine = entity.isMine,
    deliveryState = enumValueOf<MessageDeliveryState>(entity.deliveryState),
    isDeleted = entity.isDeleted,
)

private fun playerCardPreviewFromUser(user: com.airsoft.social.core.model.User): PlayerCardPreview = PlayerCardPreview(
    userId = user.id,
    callsign = user.callsign,
    region = user.region,
    teamName = user.teamName,
    roles = user.roles,
    bio = user.bio,
    isOnline = user.isOnline,
    isVerified = user.isVerified,
    rating = user.rating,
)

private fun defaultChatTitle(chat: Chat): String = when (chat.type) {
    com.airsoft.social.core.model.ChatType.DIRECT -> "Личный чат"
    com.airsoft.social.core.model.ChatType.GROUP -> "Группа"
    com.airsoft.social.core.model.ChatType.TEAM -> "Командный чат"
    com.airsoft.social.core.model.ChatType.EVENT -> "Чат события"
    com.airsoft.social.core.model.ChatType.GENERAL -> "Общий чат"
    com.airsoft.social.core.model.ChatType.SUPPORT -> "Поддержка"
}

internal class InMemoryChatsLocalDataSource : ChatsLocalDataSource {
    private val threadsState = MutableStateFlow<List<ChatThreadEntity>>(emptyList())
    private val messagesState = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override fun observeThreads(): Flow<List<ChatThreadEntity>> = threadsState.asStateFlow()
        .map { list -> list.sortedByDescending { it.updatedAtMs } }

    override fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>> = messagesState.asStateFlow()
        .map { list -> list.filter { it.chatId == chatId }.sortedBy { it.createdAtMs } }

    override suspend fun threadCount(): Int = threadsState.value.size

    override suspend fun upsertThreads(entities: List<ChatThreadEntity>) {
        val map = threadsState.value.associateBy { it.id }.toMutableMap()
        entities.forEach { map[it.id] = it }
        threadsState.value = map.values.toList()
    }

    override suspend fun upsertMessages(entities: List<ChatMessageEntity>) {
        val map = messagesState.value.associateBy { it.id }.toMutableMap()
        entities.forEach { map[it.id] = it }
        messagesState.value = map.values.toList()
    }

    override suspend fun upsertMessage(entity: ChatMessageEntity) {
        upsertMessages(listOf(entity))
    }

    override suspend fun updateThreadLastMessage(chatId: String, lastMessage: String?, updatedAtMs: Long) {
        threadsState.value = threadsState.value.map { entity ->
            if (entity.id == chatId) entity.copy(lastMessage = lastMessage, updatedAtMs = updatedAtMs, unreadCount = 0) else entity
        }
    }

    override suspend fun updateMessageDeliveryState(messageId: String, deliveryState: MessageDeliveryState) {
        messagesState.value = messagesState.value.map { entity ->
            if (entity.id == messageId) entity.copy(deliveryState = deliveryState.name) else entity
        }
    }

    override suspend fun markChatRead(chatId: String) {
        threadsState.value = threadsState.value.map { entity ->
            if (entity.id == chatId) entity.copy(unreadCount = 0) else entity
        }
        messagesState.value = messagesState.value.map { entity ->
            if (entity.chatId == chatId) entity.copy(isRead = true) else entity
        }
    }
}
