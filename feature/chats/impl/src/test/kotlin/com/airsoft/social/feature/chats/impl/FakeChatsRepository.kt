package com.airsoft.social.feature.chats.impl

import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.ChatsRepository
import com.airsoft.social.core.model.ChatThreadPreview
import com.airsoft.social.core.model.ChatType
import com.airsoft.social.core.model.ChatMessageItem
import com.airsoft.social.core.model.MessageDeliveryState
import com.airsoft.social.core.model.PlayerCardPreview
import com.airsoft.social.core.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeChatsRepository : ChatsRepository {
    private val threads = MutableStateFlow(
        listOf(
            ChatThreadPreview(
                chatId = "team-ew-general",
                title = "Команда [EW] EASY WINNER",
                chatType = ChatType.TEAM,
                lastMessage = "Сбор в 09:00",
                unreadCount = 2,
                updatedAtMs = 1_000L,
            ),
            ChatThreadPreview(
                chatId = "ghost-direct",
                title = "Ghost",
                chatType = ChatType.DIRECT,
                lastMessage = "Ищу команду",
                unreadCount = 0,
                updatedAtMs = 900L,
            ),
        ),
    )

    private val players = MutableStateFlow(
        listOf(
            PlayerCardPreview(
                userId = "ghost",
                callsign = "Ghost",
                region = "Москва",
                teamName = null,
                roles = setOf(UserRole.PLAYER),
                bio = "Штурм",
                isOnline = false,
                isVerified = false,
                rating = 4.8f,
            ),
            PlayerCardPreview(
                userId = "self",
                callsign = "Teiwaz_",
                region = "Москва",
                teamName = "[EW] EASY WINNER",
                roles = setOf(UserRole.CAPTAIN, UserRole.PLAYER),
                bio = "Командир",
                isOnline = true,
                isVerified = true,
                rating = 4.9f,
            ),
        ),
    )

    private val messages = MutableStateFlow(
        listOf(
            ChatMessageItem(
                messageId = "m1",
                chatId = "team-ew-general",
                senderId = "raven",
                senderCallsign = "Raven",
                content = "Принял",
                createdAtMs = 1_000L,
                isMine = false,
                deliveryState = MessageDeliveryState.SENT,
            ),
        ),
    )

    override fun observeThreads(): Flow<List<ChatThreadPreview>> = threads

    override fun observePlayersDirectory(): Flow<List<PlayerCardPreview>> = players

    override fun observeMessages(chatId: String): Flow<List<ChatMessageItem>> =
        messages.map { list -> list.filter { it.chatId == chatId }.sortedBy { it.createdAtMs } }

    override suspend fun sendTextMessage(chatId: String, text: String): AppResult<Unit> {
        if (text.isBlank()) return AppResult.Failure(com.airsoft.social.core.common.AppError.Validation("Пусто"))
        val next = ChatMessageItem(
            messageId = "local-${messages.value.size + 1}",
            chatId = chatId,
            senderId = "self",
            senderCallsign = "Teiwaz_",
            content = text,
            createdAtMs = 2_000L + messages.value.size,
            isMine = true,
            deliveryState = MessageDeliveryState.SENT,
        )
        messages.value = messages.value + next
        threads.value = threads.value.map {
            if (it.chatId == chatId) it.copy(lastMessage = text, unreadCount = 0, updatedAtMs = next.createdAtMs) else it
        }
        return AppResult.Success(Unit)
    }

    override suspend fun markChatRead(chatId: String): AppResult<Unit> {
        threads.value = threads.value.map { if (it.chatId == chatId) it.copy(unreadCount = 0) else it }
        return AppResult.Success(Unit)
    }

    override fun observePlayerCard(userId: String): Flow<PlayerCardPreview?> =
        players.map { list -> list.firstOrNull { it.userId == userId } }
}
