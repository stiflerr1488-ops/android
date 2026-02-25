package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapCoordinatorTest {

    @Test
    fun hasPointChanges_detectsAddedOrDeletedPoints() {
        val coordinator = MapCoordinator()
        val point = KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0)

        assertFalse(coordinator.hasPointChanges(emptyList(), emptyList()))
        assertTrue(coordinator.hasPointChanges(listOf(point), emptyList()))
        assertTrue(coordinator.hasPointChanges(emptyList(), listOf(point)))
    }

    @Test
    fun sourceUriOrNull_returnsOnlyUrisWithScheme() {
        val coordinator = MapCoordinator()
        val mapWithContentUri = baseMap(sourceUriString = "content://maps/doc.kmz")
        val mapWithBlankSource = baseMap(sourceUriString = " ")
        val mapWithNoScheme = baseMap(sourceUriString = "maps/doc.kmz")

        assertNotNull(coordinator.sourceUriOrNull(mapWithContentUri))
        assertNull(coordinator.sourceUriOrNull(mapWithBlankSource))
        assertNull(coordinator.sourceUriOrNull(mapWithNoScheme))
    }

    @Test
    fun mergePoints_removesDeletedIds_andAppendsNewPoints() {
        val coordinator = MapCoordinator()
        val original = baseMap(
            points = listOf(
                KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0),
                KmlPoint(id = "p-2", name = "Two", lat = 55.1, lon = 37.1),
            )
        )
        val newPoints = listOf(KmlPoint(id = "p-3", name = "Three", lat = 55.2, lon = 37.2))
        val deletedPoints = listOf(KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0))

        val merged = coordinator.mergePoints(
            activeMap = original,
            newPoints = newPoints,
            deletedPoints = deletedPoints,
        )

        assertEquals(listOf("p-2", "p-3"), merged.points.map { it.id })
    }

    @Test
    fun importAndSave_delegateToInjectedImplementations() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val sourceUri = Uri.parse("content://maps/source.kmz")
        val targetUri = Uri.parse("content://maps/target.kmz")
        val imported = baseMap(sourceUriString = sourceUri.toString())
        var savedDestination: Uri? = null
        var savedMap: TacticalMap? = null
        var savedNewPoints: List<KmlPoint>? = null
        var savedDeletedPoints: List<KmlPoint>? = null

        val coordinator = MapCoordinator(
            importer = { _, _ -> imported },
            saver = { _, map, newPoints, deletedPoints, destinationUri ->
                savedMap = map
                savedNewPoints = newPoints
                savedDeletedPoints = deletedPoints
                savedDestination = destinationUri
            },
        )

        val importResult = coordinator.importMap(app, sourceUri)
        val newPoints = listOf(KmlPoint(id = "p-3", name = "Three", lat = 55.2, lon = 37.2))
        val deletedPoints = listOf(KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0))
        coordinator.saveChanges(
            application = app,
            map = imported,
            newPoints = newPoints,
            deletedPoints = deletedPoints,
            destinationUri = targetUri,
        )

        assertSame(imported, importResult)
        assertSame(imported, savedMap)
        assertEquals(newPoints, savedNewPoints)
        assertEquals(deletedPoints, savedDeletedPoints)
        assertEquals(targetUri, savedDestination)
    }

    private fun baseMap(
        sourceUriString: String? = null,
        points: List<KmlPoint> = emptyList(),
    ): TacticalMap {
        return TacticalMap(
            id = "map-1",
            name = "Map",
            dirPath = "/tmp/map",
            mainKmlRelativePath = "doc.kml",
            sourceUriString = sourceUriString,
            points = points,
        )
    }
}
