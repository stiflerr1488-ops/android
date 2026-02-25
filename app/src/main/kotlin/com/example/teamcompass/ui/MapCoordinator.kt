package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

internal class MapCoordinator(
    private val importer: suspend (Application, Uri) -> TacticalMap = { application, uri ->
        KmzMapImporter.import(application, uri)
    },
    private val saver: suspend (
        Application,
        TacticalMap,
        List<KmlPoint>,
        List<KmlPoint>,
        Uri,
    ) -> Unit = { application, map, newPoints, deletedPoints, destinationUri ->
        KmzMapSaver.saveToUri(
            application = application,
            map = map,
            newPoints = newPoints,
            deletedPoints = deletedPoints,
            destinationUri = destinationUri,
        )
    },
) {
    fun hasPointChanges(newPoints: List<KmlPoint>, deletedPoints: List<KmlPoint>): Boolean {
        return newPoints.isNotEmpty() || deletedPoints.isNotEmpty()
    }

    fun sourceUriOrNull(map: TacticalMap): Uri? {
        val raw = map.sourceUriString?.trim().orEmpty()
        if (raw.isBlank()) return null
        val uri = runCatching { raw.toUri() }
            .onFailure { err -> Log.w(TAG, "Failed to parse map source URI: '$raw'", err) }
            .getOrNull() ?: return null
        if (uri.scheme.isNullOrBlank()) return null
        return uri
    }

    suspend fun importMap(application: Application, uri: Uri): TacticalMap {
        return importer(application, uri)
    }

    suspend fun saveChanges(
        application: Application,
        map: TacticalMap,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint>,
        destinationUri: Uri,
    ) {
        saver(application, map, newPoints, deletedPoints, destinationUri)
    }

    fun mergePoints(
        activeMap: TacticalMap,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint>,
    ): TacticalMap {
        if (newPoints.isEmpty() && deletedPoints.isEmpty()) return activeMap
        val deletedIds = deletedPoints.map { it.id }.toHashSet()
        val merged = activeMap.points
            .filterNot { deletedIds.contains(it.id) }
            .plus(newPoints)
        return activeMap.copy(points = merged)
    }

    private companion object {
        const val TAG = "MapCoordinator"
    }
}
