package com.airsoft.social.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY created_at_ms ASC")
    fun observeByChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET delivery_state = :deliveryState WHERE id = :messageId")
    suspend fun updateDeliveryState(messageId: String, deliveryState: String)

    @Query("UPDATE chat_messages SET is_read = 1 WHERE chat_id = :chatId")
    suspend fun markRead(chatId: String)
}
