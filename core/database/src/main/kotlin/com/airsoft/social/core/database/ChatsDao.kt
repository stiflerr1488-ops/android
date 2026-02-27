package com.airsoft.social.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatsDao {
    @Query("SELECT * FROM chat_threads ORDER BY updated_at_ms DESC")
    fun observeAll(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :chatId LIMIT 1")
    suspend fun get(chatId: String): ChatThreadEntity?

    @Query("SELECT COUNT(*) FROM chat_threads")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ChatThreadEntity>)

    @Query("UPDATE chat_threads SET unread_count = 0 WHERE id = :chatId")
    suspend fun markRead(chatId: String)

    @Query("UPDATE chat_threads SET last_message = :lastMessage, updated_at_ms = :updatedAtMs WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMessage: String?, updatedAtMs: Long)
}
