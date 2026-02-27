package com.airsoft.social.core.model

enum class MessageDeliveryState {
    SENDING,
    SENT,
    FAILED,
}

data class ChatThreadPreview(
    val chatId: String,
    val title: String,
    val chatType: ChatType,
    val lastMessage: String?,
    val unreadCount: Int,
    val updatedAtMs: Long,
)

data class ChatMessageItem(
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderCallsign: String,
    val content: String,
    val createdAtMs: Long,
    val isMine: Boolean,
    val deliveryState: MessageDeliveryState,
    val isDeleted: Boolean = false,
)

data class PlayerCardPreview(
    val userId: String,
    val callsign: String,
    val region: String?,
    val teamName: String?,
    val roles: Set<UserRole>,
    val bio: String?,
    val isOnline: Boolean,
    val isVerified: Boolean,
    val rating: Float?,
)
