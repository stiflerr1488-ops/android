package com.airsoft.social.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "chat_type")
    val chatType: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "last_message")
    val lastMessage: String?,
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int,
    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long,
)
