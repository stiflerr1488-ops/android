package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
class KmzMapSaverTest {

    @Test
    fun save_adds_new_placemark_point_into_main_kml() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mapDir = File(app.filesDir, "maps/test_save_${UUID.randomUUID()}").apply { mkdirs() }
        val kmlFile = File(mapDir, "doc.kml")
        kmlFile.writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <name>Save test</name>
              </Document>
            </kml>
            """.trimIndent(),
            Charsets.UTF_8,
        )

        val map = TacticalMap(
            id = "test",
            name = "Save test",
            dirPath = mapDir.absolutePath,
            mainKmlRelativePath = "doc.kml",
            sourceUriString = null,
            points = emptyList(),
        )
        val outputKmz = File(app.cacheDir, "saved_${UUID.randomUUID()}.kmz")
        val newPoint = KmlPoint(
            id = "p1",
            name = "New marker",
            description = "Added by unit test",
            lat = 55.75,
            lon = 37.62,
        )

        KmzMapSaver.saveToUri(
            application = app,
            map = map,
            newPoints = listOf(newPoint),
            destinationUri = Uri.fromFile(outputKmz),
        )

        val entries = readZipEntries(outputKmz)
        val savedKml = entries["doc.kml"]?.toString(Charsets.UTF_8).orEmpty()
        assertTrue(savedKml.contains("<name>New marker</name>"))
        assertTrue(savedKml.contains("<description>Added by unit test</description>"))
        assertTrue(savedKml.contains("37.6200000,55.7500000,0"))
    }

    @Test
    fun save_preserves_non_kml_entries_in_archive() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mapDir = File(app.filesDir, "maps/test_preserve_${UUID.randomUUID()}").apply { mkdirs() }
        File(mapDir, "doc.kml").writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2"><Document/></kml>
            """.trimIndent(),
            Charsets.UTF_8,
        )
        val binaryData = byteArrayOf(1, 2, 3, 4, 5, 99)
        val resourceFile = File(mapDir, "files/icon.bin")
        resourceFile.parentFile?.mkdirs()
        resourceFile.writeBytes(binaryData)
        File(mapDir, "source").writeText("internal temp", Charsets.UTF_8)

        val map = TacticalMap(
            id = "test2",
            name = "Preserve test",
            dirPath = mapDir.absolutePath,
            mainKmlRelativePath = "doc.kml",
            sourceUriString = null,
            points = emptyList(),
        )
        val outputKmz = File(app.cacheDir, "preserve_${UUID.randomUUID()}.kmz")
        val newPoint = KmlPoint(
            id = "p2",
            name = "B",
            description = "",
            lat = 1.0,
            lon = 2.0,
        )

        KmzMapSaver.saveToUri(
            application = app,
            map = map,
            newPoints = listOf(newPoint),
            destinationUri = Uri.fromFile(outputKmz),
        )

        val entries = readZipEntries(outputKmz)
        assertTrue(entries.containsKey("files/icon.bin"))
        assertArrayEquals(binaryData, entries.getValue("files/icon.bin"))
        assertFalse(entries.containsKey("source"))
    }

    private fun readZipEntries(zipFile: File): Map<String, ByteArray> {
        val out = linkedMapOf<String, ByteArray>()
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    out[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return out
    }
}

