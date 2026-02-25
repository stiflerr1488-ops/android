package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
class KmzMapImporterTest {

    @Test
    fun import_kmz_reads_placemark_point_name_description() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val kmz = File(app.cacheDir, "import_ok_${UUID.randomUUID()}.kmz")
        writeZip(
            kmz,
            mapOf(
                "doc.kml" to """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <kml xmlns="http://www.opengis.net/kml/2.2">
                      <Document>
                        <name>Training map</name>
                        <Placemark>
                          <name>Alpha</name>
                          <description>First marker</description>
                          <Point><coordinates>37.620000,55.750000,0</coordinates></Point>
                        </Placemark>
                      </Document>
                    </kml>
                """.trimIndent().toByteArray(Charsets.UTF_8),
            ),
        )

        val map = KmzMapImporter.import(app, Uri.fromFile(kmz))

        assertEquals("doc.kml", map.mainKmlRelativePath)
        assertEquals(Uri.fromFile(kmz).toString(), map.sourceUriString)
        assertEquals(1, map.points.size)
        assertEquals("Alpha", map.points.first().name)
        assertEquals("First marker", map.points.first().description)
        assertEquals(55.75, map.points.first().lat, 1e-6)
        assertEquals(37.62, map.points.first().lon, 1e-6)
    }

    @Test
    fun import_kmz_without_kml_returns_error() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val kmz = File(app.cacheDir, "import_no_kml_${UUID.randomUUID()}.kmz")
        writeZip(
            kmz,
            mapOf(
                "files/readme.txt" to "no kml here".toByteArray(Charsets.UTF_8),
            ),
        )

        val error = runCatching {
            KmzMapImporter.import(app, Uri.fromFile(kmz))
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message.orEmpty().contains("No KML found in archive", ignoreCase = true))
    }

    private fun writeZip(file: File, entries: Map<String, ByteArray>) {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }
}

