package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import com.example.teamcompass.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles import/save map actions and merges resulting map state into UI.
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class MapActionsCoordinator(
    private val application: Application,
    private val mapCoordinator: MapCoordinator,
    private val scope: CoroutineScope,
    private val coroutineExceptionHandler: CoroutineExceptionHandler,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (String, Throwable?) -> Unit,
) {
    fun importMap(uri: Uri) {
        updateState { it.copy(team = it.team.copy(isBusy = true)) }
        scope.launch(coroutineExceptionHandler) {
            try {
                val map = mapCoordinator.importMap(application, uri)
                updateState {
                    it.copy(
                        map = it.map.copy(activeMap = map, mapEnabled = true),
                        team = it.team.copy(isBusy = false),
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (err: Exception) {
                emitError(string(R.string.vm_error_kmz_load_failed), err)
                updateState { it.copy(team = it.team.copy(isBusy = false)) }
            }
        }
    }

    fun clearMap() {
        updateState { it.copy(map = it.map.copy(activeMap = null, mapEnabled = false)) }
    }

    fun setMapEnabled(enabled: Boolean) {
        updateState { it.copy(map = it.map.copy(mapEnabled = enabled)) }
    }

    fun setMapOpacity(opacity: Float) {
        updateState { it.copy(map = it.map.copy(mapOpacity = opacity.coerceIn(0f, 1f))) }
    }

    fun saveChangesToSource(
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint> = emptyList(),
    ) {
        if (!mapCoordinator.hasPointChanges(newPoints, deletedPoints)) return
        val map = readState().activeMap
        if (map == null) {
            emitError(string(R.string.map_not_loaded), null)
            return
        }

        val sourceUri = mapCoordinator.sourceUriOrNull(map)
        if (sourceUri == null) {
            emitError(string(R.string.vm_error_map_source_no_access), null)
            return
        }

        saveChangesInternal(
            destinationUri = sourceUri,
            map = map,
            newPoints = newPoints,
            deletedPoints = deletedPoints,
        )
    }

    fun saveChangesAs(
        uri: Uri,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint> = emptyList(),
    ) {
        if (!mapCoordinator.hasPointChanges(newPoints, deletedPoints)) return
        val map = readState().activeMap
        if (map == null) {
            emitError(string(R.string.map_not_loaded), null)
            return
        }
        saveChangesInternal(
            destinationUri = uri,
            map = map,
            newPoints = newPoints,
            deletedPoints = deletedPoints,
        )
    }

    private fun saveChangesInternal(
        destinationUri: Uri,
        map: TacticalMap,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint>,
    ) {
        updateState { it.copy(team = it.team.copy(isBusy = true)) }
        scope.launch(coroutineExceptionHandler) {
            try {
                mapCoordinator.saveChanges(
                    application = application,
                    map = map,
                    newPoints = newPoints,
                    deletedPoints = deletedPoints,
                    destinationUri = destinationUri,
                )
                updateState { state ->
                    val active = state.map.activeMap
                    if (active == null || active.id != map.id) {
                        state.copy(team = state.team.copy(isBusy = false))
                    } else {
                        val mergedMap = mapCoordinator.mergePoints(
                            activeMap = active,
                            newPoints = newPoints,
                            deletedPoints = deletedPoints,
                        )
                        state.copy(
                            map = state.map.copy(activeMap = mergedMap),
                            team = state.team.copy(isBusy = false),
                        )
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (err: Exception) {
                emitError(string(R.string.vm_error_file_write_failed), err)
                updateState { it.copy(team = it.team.copy(isBusy = false)) }
            }
        }
    }

    private fun string(resId: Int): String = application.getString(resId)
}
