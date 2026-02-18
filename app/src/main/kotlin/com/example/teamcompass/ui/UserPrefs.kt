package com.example.teamcompass.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("teamcompass_prefs")

class UserPrefs(private val context: Context) {
    private val KEY_CALLSIGN = stringPreferencesKey("callsign")
    private val KEY_TEAM = stringPreferencesKey("teamCode")

    private val KEY_DEFAULT_MODE = stringPreferencesKey("defaultMode")
    private val KEY_GAME_INTERVAL_SEC = intPreferencesKey("gameIntervalSec")
    private val KEY_GAME_DISTANCE_M = intPreferencesKey("gameDistanceM")
    private val KEY_SILENT_INTERVAL_SEC = intPreferencesKey("silentIntervalSec")
    private val KEY_SILENT_DISTANCE_M = intPreferencesKey("silentDistanceM")

    val callsignFlow: Flow<String> = context.dataStore.data.map { it[KEY_CALLSIGN] ?: "" }
    val teamCodeFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TEAM] }

    val defaultModeFlow: Flow<TrackingMode> = context.dataStore.data.map {
        when (it[KEY_DEFAULT_MODE]) {
            TrackingMode.SILENT.name -> TrackingMode.SILENT
            else -> TrackingMode.GAME
        }
    }

    val gameIntervalSecFlow: Flow<Int> = context.dataStore.data.map { it[KEY_GAME_INTERVAL_SEC] ?: 3 }
    val gameDistanceMFlow: Flow<Int> = context.dataStore.data.map { it[KEY_GAME_DISTANCE_M] ?: 10 }
    val silentIntervalSecFlow: Flow<Int> = context.dataStore.data.map { it[KEY_SILENT_INTERVAL_SEC] ?: 10 }
    val silentDistanceMFlow: Flow<Int> = context.dataStore.data.map { it[KEY_SILENT_DISTANCE_M] ?: 30 }

    suspend fun setCallsign(value: String) {
        context.dataStore.edit { it[KEY_CALLSIGN] = value }
    }

    suspend fun setTeamCode(code: String?) {
        context.dataStore.edit {
            if (code == null) it.remove(KEY_TEAM) else it[KEY_TEAM] = code
        }
    }

    suspend fun setDefaultMode(mode: TrackingMode) {
        context.dataStore.edit { it[KEY_DEFAULT_MODE] = mode.name }
    }

    suspend fun setGamePolicy(intervalSec: Int, distanceM: Int) {
        context.dataStore.edit {
            it[KEY_GAME_INTERVAL_SEC] = intervalSec
            it[KEY_GAME_DISTANCE_M] = distanceM
        }
    }

    suspend fun setSilentPolicy(intervalSec: Int, distanceM: Int) {
        context.dataStore.edit {
            it[KEY_SILENT_INTERVAL_SEC] = intervalSec
            it[KEY_SILENT_DISTANCE_M] = distanceM
        }
    }
}
