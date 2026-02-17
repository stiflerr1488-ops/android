package com.example.teamcompass.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("teamcompass_prefs")

class UserPrefs(private val context: Context) {
    private val KEY_CALLSIGN = stringPreferencesKey("callsign")
    private val KEY_TEAM = stringPreferencesKey("teamCode")

    val callsignFlow: Flow<String> = context.dataStore.data.map { it[KEY_CALLSIGN] ?: "" }
    val teamCodeFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TEAM] }

    suspend fun setCallsign(value: String) {
        context.dataStore.edit { it[KEY_CALLSIGN] = value }
    }

    suspend fun setTeamCode(code: String?) {
        context.dataStore.edit {
            if (code == null) it.remove(KEY_TEAM) else it[KEY_TEAM] = code
        }
    }
}
