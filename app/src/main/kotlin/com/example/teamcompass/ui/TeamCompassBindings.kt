package com.example.teamcompass.ui

import com.example.teamcompass.domain.TrackingController
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal fun launchPrefsBindings(
    scope: CoroutineScope,
    coroutineExceptionHandler: CoroutineExceptionHandler,
    prefs: UserPrefs,
    readState: () -> UiState,
    updateState: ((UiState) -> UiState) -> Unit,
    normalizeTeamCode: (String?) -> String?,
    startListening: (String) -> Unit,
    stopListening: () -> Unit,
    autoBrightnessBinding: AutoBrightnessBinding,
) {
    scope.launch(coroutineExceptionHandler) {
        prefs.callsignFlow.collectLatest { callsign ->
            updateState { it.copy(team = it.team.copy(callsign = callsign)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.teamCodeFlow.collectLatest { storedCode ->
            val normalized = normalizeTeamCode(storedCode)
            val current = readState().teamCode
            if (normalized == current) return@collectLatest

            updateState { it.copy(team = it.team.copy(teamCode = normalized)) }
            val nextState = readState()
            if (nextState.isAuthReady && normalized != null) {
                startListening(normalized)
            } else if (normalized == null) {
                stopListening()
            }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.defaultModeFlow.collectLatest { mode ->
            updateState { it.copy(tracking = it.tracking.copy(defaultMode = mode)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.gameIntervalSecFlow.collectLatest { sec ->
            updateState { it.copy(settings = it.settings.copy(gameIntervalSec = sec)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.gameDistanceMFlow.collectLatest { distance ->
            updateState { it.copy(settings = it.settings.copy(gameDistanceM = distance)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.silentIntervalSecFlow.collectLatest { sec ->
            updateState { it.copy(settings = it.settings.copy(silentIntervalSec = sec)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.silentDistanceMFlow.collectLatest { distance ->
            updateState { it.copy(settings = it.settings.copy(silentDistanceM = distance)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.showCompassHelpOnceFlow.collectLatest { value ->
            updateState { it.copy(settings = it.settings.copy(showCompassHelpOnce = value)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.showOnboardingOnceFlow.collectLatest { value ->
            updateState { it.copy(settings = it.settings.copy(showOnboardingOnce = value)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.controlLayoutEditFlow.collectLatest { enabled ->
            updateState { it.copy(settings = it.settings.copy(controlLayoutEditEnabled = enabled)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.controlPositionsFlow.collectLatest { positions ->
            updateState { it.copy(settings = it.settings.copy(controlPositions = positions)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.autoBrightnessEnabledFlow.collectLatest { enabled ->
            updateState { it.copy(settings = it.settings.copy(autoBrightnessEnabled = enabled)) }
            autoBrightnessBinding.setEnabled(enabled)
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.screenBrightnessFlow.collectLatest { brightness ->
            updateState { it.copy(settings = it.settings.copy(screenBrightness = brightness)) }
            autoBrightnessBinding.setBrightness(brightness)
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.hasStartedOnceFlow.collectLatest { hasStarted ->
            updateState { it.copy(tracking = it.tracking.copy(hasStartedOnce = hasStarted)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        prefs.themeModeFlow.collectLatest { mode ->
            updateState { it.copy(settings = it.settings.copy(themeMode = mode)) }
        }
    }
}

internal fun launchTrackingControllerBindings(
    scope: CoroutineScope,
    coroutineExceptionHandler: CoroutineExceptionHandler,
    trackingController: TrackingController,
    updateState: ((UiState) -> UiState) -> Unit,
    refreshTargetsFromState: () -> Unit,
) {
    scope.launch(coroutineExceptionHandler) {
        trackingController.isTracking.collectLatest { tracking ->
            updateState { it.copy(tracking = it.tracking.copy(isTracking = tracking)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        trackingController.location.collectLatest { location ->
            updateState { state ->
                val tel = if (location == null) {
                    state.tracking.telemetry
                } else {
                    state.tracking.telemetry.copy(lastLocationAtMs = location.timestampMs)
                }
                state.copy(tracking = state.tracking.copy(me = location, telemetry = tel))
            }
            refreshTargetsFromState()
        }
    }

    scope.launch(coroutineExceptionHandler) {
        trackingController.isAnchored.collectLatest { anchored ->
            updateState { it.copy(tracking = it.tracking.copy(isAnchored = anchored)) }
        }
    }

    scope.launch(coroutineExceptionHandler) {
        trackingController.telemetry.collectLatest { telemetry ->
            updateState { state ->
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = state.tracking.telemetry.copy(
                            rtdbWriteErrors = telemetry.rtdbWriteErrors,
                            trackingRestarts = telemetry.trackingRestarts,
                            lastLocationAtMs = telemetry.lastLocationAtMs.takeIf { it > 0L }
                                ?: state.tracking.telemetry.lastLocationAtMs,
                            lastTrackingRestartReason = telemetry.lastTrackingRestartReason,
                        )
                    )
                )
            }
        }
    }
}
