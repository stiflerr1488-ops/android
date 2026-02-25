package com.airsoft.social.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AirsoftDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        fun build(context: Context): AirsoftDatabase =
            Room.databaseBuilder(
                context,
                AirsoftDatabase::class.java,
                "airsoft-social.db",
            ).fallbackToDestructiveMigration().build()
    }
}
