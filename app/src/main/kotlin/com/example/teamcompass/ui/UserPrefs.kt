package com.example.teamcompass.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    private val KEY_SHOW_COMPASS_HELP_ONCE = booleanPreferencesKey("showCompassHelpOnce")
    private val KEY_SHOW_ONBOARDING_ONCE = booleanPreferencesKey("showOnboardingOnce")
    private val KEY_CONTROL_LAYOUT_EDIT = booleanPreferencesKey("controlLayoutEdit")
    
    // Настройки авто-яркости
    private val KEY_AUTO_BRIGHTNESS_ENABLED = booleanPreferencesKey("autoBrightnessEnabled")
    private val KEY_SCREEN_BRIGHTNESS = floatPreferencesKey("screenBrightness")
    private val KEY_HAS_STARTED_ONCE = booleanPreferencesKey("hasStartedOnce")

    // Настройки темы
    private val KEY_THEME_MODE = stringPreferencesKey("themeMode")

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
    val showCompassHelpOnceFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_COMPASS_HELP_ONCE] ?: true }
    val showOnboardingOnceFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_ONBOARDING_ONCE] ?: true }
    val controlLayoutEditFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONTROL_LAYOUT_EDIT] ?: false }
    
    // Потоки настроек авто-яркости
    val autoBrightnessEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_BRIGHTNESS_ENABLED] ?: true }
    val screenBrightnessFlow: Flow<Float> = context.dataStore.data.map { it[KEY_SCREEN_BRIGHTNESS] ?: 0.8f }
    val hasStartedOnceFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAS_STARTED_ONCE] ?: false }

    // Поток настройки темы
    val themeModeFlow: Flow<com.example.teamcompass.ui.theme.ThemeMode> = context.dataStore.data.map {
        when (it[KEY_THEME_MODE]) {
            "LIGHT" -> com.example.teamcompass.ui.theme.ThemeMode.LIGHT
            "DARK" -> com.example.teamcompass.ui.theme.ThemeMode.DARK
            else -> com.example.teamcompass.ui.theme.ThemeMode.SYSTEM
        }
    }
    val controlPositionsFlow: Flow<Map<CompassControlId, ControlPosition>> = context.dataStore.data.map { prefs ->
        val defaults = defaultCompassControlPositions()
        CompassControlId.entries.associateWith { id ->
            val key = stringPreferencesKey("controlPos_${id.prefKey}")
            val pos = decodeControlPosition(prefs[key]) ?: defaults.getValue(id)
            migrateLegacyCompassPosition(id, pos)
        }
    }

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

    suspend fun setShowCompassHelpOnce(value: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_COMPASS_HELP_ONCE] = value }
    }

    suspend fun setShowOnboardingOnce(value: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_ONBOARDING_ONCE] = value }
    }

    suspend fun setControlLayoutEdit(value: Boolean) {
        context.dataStore.edit { it[KEY_CONTROL_LAYOUT_EDIT] = value }
    }

    suspend fun setControlPosition(id: CompassControlId, pos: ControlPosition) {
        val key = stringPreferencesKey("controlPos_${id.prefKey}")
        context.dataStore.edit { it[key] = pos.encode() }
    }

    suspend fun resetControlPositions() {
        context.dataStore.edit { prefs ->
            CompassControlId.entries.forEach { id ->
                prefs.remove(stringPreferencesKey("controlPos_${id.prefKey}"))
            }
        }
    }
    
    // Методы для настроек авто-яркости
    suspend fun setAutoBrightnessEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_BRIGHTNESS_ENABLED] = enabled }
    }
    
    suspend fun setScreenBrightness(brightness: Float) {
        context.dataStore.edit { it[KEY_SCREEN_BRIGHTNESS] = brightness.coerceIn(0.1f, 1.0f) }
    }
    
    suspend fun setHasStartedOnce(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAS_STARTED_ONCE] = enabled }
    }

    // Методы для настроек темы
    suspend fun setThemeMode(mode: com.example.teamcompass.ui.theme.ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }
}

private fun migrateLegacyCompassPosition(
    id: CompassControlId,
    pos: ControlPosition,
): ControlPosition {
    fun remapIfExact(oldX: Float, oldY: Float, newX: Float, newY: Float): ControlPosition {
        return if (pos.approx(oldX, oldY)) ControlPosition(newX, newY) else pos
    }

    return when (id) {
        CompassControlId.LIST -> remapIfExact(0.95f, 0.14f, 0.92f, 0.20f)
        CompassControlId.ENEMY -> remapIfExact(0.95f, 0.28f, 0.92f, 0.34f)
        CompassControlId.EDIT -> remapIfExact(0.95f, 0.42f, 0.92f, 0.46f)
        CompassControlId.MODE -> when {
            pos.approx(0.95f, 0.62f) -> ControlPosition(0.92f, 0.58f)
            pos.approx(0.95f, 0.56f) -> ControlPosition(0.92f, 0.58f)
            else -> pos
        }
        CompassControlId.TRACK -> when {
            pos.approx(0.95f, 0.76f) -> ControlPosition(0.92f, 0.70f)
            pos.approx(0.95f, 0.70f) -> ControlPosition(0.92f, 0.70f)
            else -> pos
        }
        CompassControlId.SCAN_BLUETOOTH -> remapIfExact(0.80f, 0.70f, 0.92f, 0.70f)
        CompassControlId.SOS -> when {
            pos.approx(0.95f, 0.90f) -> ControlPosition(0.92f, 0.82f)
            pos.approx(0.95f, 0.84f) -> ControlPosition(0.92f, 0.82f)
            else -> pos
        }
        else -> pos
    }
}

private fun ControlPosition.approx(x: Float, y: Float): Boolean {
    return kotlin.math.abs(xNorm - x) < 0.0001f && kotlin.math.abs(yNorm - y) < 0.0001f
}
