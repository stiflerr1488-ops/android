package com.airsoft.social.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "chat_id", index = true)
    val chatId: String,
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    @ColumnInfo(name = "sender_callsign")
    val senderCallsign: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "created_at_ms")
    val createdAtMs: Long,
    @ColumnInfo(name = "is_mine")
    val isMine: Boolean,
    @ColumnInfo(name = "delivery_state")
    val deliveryState: String,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
)
