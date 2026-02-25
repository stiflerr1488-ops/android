package com.example.teamcompass.ui

import androidx.lifecycle.SavedStateHandle
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

internal object TeamCompassSavedStateKeys {
    const val TEAM_CODE = "state_team_code"
    const val DEFAULT_MODE = "state_default_mode"
    const val PLAYER_MODE = "state_player_mode"
    const val IS_TRACKING = "state_is_tracking"
    const val MY_SOS_UNTIL_MS = "state_my_sos_until_ms"
    const val TARGET_FILTER_PRESET = "state_target_filter_preset"
    const val TARGET_FILTER_NEAR_RADIUS_M = "state_target_filter_near_radius_m"
    const val TARGET_FILTER_SHOW_DEAD = "state_target_filter_show_dead"
    const val TARGET_FILTER_SHOW_STALE = "state_target_filter_show_stale"
    const val TARGET_FILTER_FOCUS_MODE = "state_target_filter_focus_mode"
}

internal data class TeamCompassRestoredState(
    val teamCode: String?,
    val defaultMode: TrackingMode,
    val playerMode: PlayerMode,
    val isTracking: Boolean,
    val mySosUntilMs: Long,
    val targetFilterState: TargetFilterState,
)

private data class TeamCompassSavedStateSnapshot(
    val teamCode: String?,
    val defaultMode: TrackingMode,
    val playerMode: PlayerMode,
    val isTracking: Boolean,
    val mySosUntilMs: Long,
    val targetFilterPreset: TargetFilterPreset,
    val targetFilterNearRadiusM: Int,
    val targetFilterShowDead: Boolean,
    val targetFilterShowStale: Boolean,
    val targetFilterFocusMode: Boolean,
)

internal class TeamCompassSavedStateBinder {
    fun restore(savedStateHandle: SavedStateHandle?): TeamCompassRestoredState {
        val defaultTargetFilterState = TargetFilterState()
        return TeamCompassRestoredState(
            teamCode = savedStateHandle
                ?.get<String>(TeamCompassSavedStateKeys.TEAM_CODE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
            defaultMode = savedStateHandle
                ?.get<String>(TeamCompassSavedStateKeys.DEFAULT_MODE)
                ?.let(::parseTrackingModeOrNull)
                ?: TrackingMode.GAME,
            playerMode = savedStateHandle
                ?.get<String>(TeamCompassSavedStateKeys.PLAYER_MODE)
                ?.let(::parsePlayerModeOrNull)
                ?: PlayerMode.GAME,
            isTracking = savedStateHandle
                ?.get<Boolean>(TeamCompassSavedStateKeys.IS_TRACKING)
                ?: false,
            mySosUntilMs = savedStateHandle
                ?.get<Long>(TeamCompassSavedStateKeys.MY_SOS_UNTIL_MS)
                ?: 0L,
            targetFilterState = TargetFilterState(
                preset = TargetFilterPreset.fromRaw(
                    savedStateHandle?.get<String>(TeamCompassSavedStateKeys.TARGET_FILTER_PRESET),
                ),
                nearRadiusM = (
                    savedStateHandle?.get<Int>(TeamCompassSavedStateKeys.TARGET_FILTER_NEAR_RADIUS_M)
                        ?: defaultTargetFilterState.nearRadiusM
                    ).coerceIn(50, 500),
                showDead = savedStateHandle?.get<Boolean>(TeamCompassSavedStateKeys.TARGET_FILTER_SHOW_DEAD)
                    ?: defaultTargetFilterState.showDead,
                showStale = savedStateHandle?.get<Boolean>(TeamCompassSavedStateKeys.TARGET_FILTER_SHOW_STALE)
                    ?: defaultTargetFilterState.showStale,
                focusMode = savedStateHandle?.get<Boolean>(TeamCompassSavedStateKeys.TARGET_FILTER_FOCUS_MODE)
                    ?: defaultTargetFilterState.focusMode,
            ),
        )
    }

    fun bind(
        scope: CoroutineScope,
        savedStateHandle: SavedStateHandle?,
        uiStateFlow: StateFlow<UiState>,
        coroutineExceptionHandler: CoroutineExceptionHandler? = null,
        @Suppress("UNUSED_PARAMETER") logWarning: (String, Throwable?) -> Unit = { _, _ -> },
    ) {
        val handle = savedStateHandle ?: return
        val launchContext = coroutineExceptionHandler ?: EmptyCoroutineContext
        scope.launch(launchContext) {
            uiStateFlow
                .map { state ->
                    TeamCompassSavedStateSnapshot(
                        teamCode = state.teamCode,
                        defaultMode = state.defaultMode,
                        playerMode = state.playerMode,
                        isTracking = state.isTracking,
                        mySosUntilMs = state.mySosUntilMs,
                        targetFilterPreset = state.targetFilterState.preset,
                        targetFilterNearRadiusM = state.targetFilterState.nearRadiusM,
                        targetFilterShowDead = state.targetFilterState.showDead,
                        targetFilterShowStale = state.targetFilterState.showStale,
                        targetFilterFocusMode = state.targetFilterState.focusMode,
                    )
                }
                .distinctUntilChanged()
                .collectLatest { snapshot ->
                    if (snapshot.teamCode.isNullOrBlank()) {
                        handle.remove<String>(TeamCompassSavedStateKeys.TEAM_CODE)
                    } else {
                        handle[TeamCompassSavedStateKeys.TEAM_CODE] = snapshot.teamCode
                    }
                    handle[TeamCompassSavedStateKeys.DEFAULT_MODE] = snapshot.defaultMode.name
                    handle[TeamCompassSavedStateKeys.PLAYER_MODE] = snapshot.playerMode.name
                    handle[TeamCompassSavedStateKeys.IS_TRACKING] = snapshot.isTracking
                    handle[TeamCompassSavedStateKeys.MY_SOS_UNTIL_MS] = snapshot.mySosUntilMs
                    handle[TeamCompassSavedStateKeys.TARGET_FILTER_PRESET] = snapshot.targetFilterPreset.name
                    handle[TeamCompassSavedStateKeys.TARGET_FILTER_NEAR_RADIUS_M] = snapshot.targetFilterNearRadiusM
                    handle[TeamCompassSavedStateKeys.TARGET_FILTER_SHOW_DEAD] = snapshot.targetFilterShowDead
                    handle[TeamCompassSavedStateKeys.TARGET_FILTER_SHOW_STALE] = snapshot.targetFilterShowStale
                    handle[TeamCompassSavedStateKeys.TARGET_FILTER_FOCUS_MODE] = snapshot.targetFilterFocusMode
                }
        }
    }

    private fun parseTrackingModeOrNull(value: String): TrackingMode? =
        runCatching { TrackingMode.valueOf(value) }.getOrNull()

    private fun parsePlayerModeOrNull(value: String): PlayerMode? =
        runCatching { PlayerMode.valueOf(value) }.getOrNull()
}
