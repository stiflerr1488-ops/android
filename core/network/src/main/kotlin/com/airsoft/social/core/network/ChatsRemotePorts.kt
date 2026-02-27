package com.airsoft.social.core.network

import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.model.Chat
import com.airsoft.social.core.model.ChatMessage
import com.airsoft.social.core.model.User

interface ChatsRemoteDataSource {
    suspend fun fetchThreads(): List<Chat>
    suspend fun fetchUsers(): List<User>
    suspend fun getUser(userId: String): User?
}

interface MessagesRemoteDataSource {
    suspend fun fetchMessages(chatId: String): List<ChatMessage>
    suspend fun sendTextMessage(chatId: String, text: String): AppResult<Unit>
}
