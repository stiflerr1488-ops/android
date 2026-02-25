package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.teamcompass.MainDispatcherRule
import com.example.teamcompass.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapActionsCoordinatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun importMap_updatesState_andClearsBusyOnSuccess() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val imported = baseMap(id = "map-imported")
        var state = UiState()
        val errors = mutableListOf<String>()
        val coordinator = MapActionsCoordinator(
            application = app,
            mapCoordinator = MapCoordinator(importer = { _, _ -> imported }),
            scope = this,
            coroutineExceptionHandler = failOnCoroutineError(),
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { message, _ -> errors += message },
        )

        coordinator.importMap(Uri.parse("content://maps/in.kmz"))
        advanceUntilIdle()

        assertEquals(imported, state.activeMap)
        assertTrue(state.mapEnabled)
        assertFalse(state.isBusy)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun saveChangesToSource_emitsErrorWhenActiveMapMissing() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        var state = UiState()
        val errors = mutableListOf<String>()
        val coordinator = MapActionsCoordinator(
            application = app,
            mapCoordinator = MapCoordinator(),
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined),
            coroutineExceptionHandler = failOnCoroutineError(),
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { message, _ -> errors += message },
        )

        coordinator.saveChangesToSource(
            newPoints = listOf(KmlPoint(id = "p-1", name = "Point", lat = 55.0, lon = 37.0)),
        )

        assertEquals(listOf(app.getString(R.string.map_not_loaded)), errors)
    }

    @Test
    fun saveChangesAs_mergesMapPoints_andClearsBusy() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val map = baseMap(
            id = "map-1",
            sourceUriString = "content://maps/source.kmz",
            points = listOf(KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0)),
        )
        val newPoint = KmlPoint(id = "p-2", name = "Two", lat = 55.1, lon = 37.1)
        val deletedPoint = KmlPoint(id = "p-1", name = "One", lat = 55.0, lon = 37.0)
        var state = UiState(
            map = MapUiState(activeMap = map),
        )
        val coordinator = MapActionsCoordinator(
            application = app,
            mapCoordinator = MapCoordinator(
                saver = { _, _, _, _, _ -> Unit },
            ),
            scope = this,
            coroutineExceptionHandler = failOnCoroutineError(),
            readState = { state },
            updateState = { transform -> state = transform(state) },
            emitError = { _, _ -> Unit },
        )

        coordinator.saveChangesAs(
            uri = Uri.parse("content://maps/out.kmz"),
            newPoints = listOf(newPoint),
            deletedPoints = listOf(deletedPoint),
        )
        advanceUntilIdle()

        assertFalse(state.isBusy)
        assertNotNull(state.activeMap)
        assertEquals(listOf("p-2"), state.activeMap!!.points.map { it.id })
    }

    private fun failOnCoroutineError(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            throw AssertionError("Unexpected coroutine exception in test", throwable)
        }
    }

    private fun baseMap(
        id: String,
        sourceUriString: String? = null,
        points: List<KmlPoint> = emptyList(),
    ): TacticalMap {
        return TacticalMap(
            id = id,
            name = "Map",
            dirPath = "/tmp/map",
            mainKmlRelativePath = "doc.kml",
            sourceUriString = sourceUriString,
            points = points,
        )
    }
}

