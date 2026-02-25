package com.airsoft.social.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetaDao {
    @Query("SELECT * FROM app_meta WHERE `key` = :key LIMIT 1")
    fun observe(key: String): Flow<AppMetaEntity?>

    @Query("SELECT * FROM app_meta WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppMetaEntity)
}

