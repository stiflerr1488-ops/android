package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/**
 * Imports a KMZ (zip) or a bare KML into app internal storage and parses it.
 *
 * Security note: protects against path traversal ("../").
 */
object KmzMapImporter {

    suspend fun import(application: Application, uri: Uri): TacticalMap = withContext(Dispatchers.IO) {
        val mapsRoot = File(application.filesDir, "maps").apply { mkdirs() }
        val id = UUID.randomUUID().toString()
        val mapDir = File(mapsRoot, id).apply { mkdirs() }
        val nameGuess = uri.lastPathSegment?.take(64).orEmpty().ifBlank { "map" }

        val src = File(mapDir, "source")
        copySource(application, uri, src)

        val zipEntryKmls = if (isZip(src)) {
            unzipKmz(src, mapDir)
        } else {
            src.copyTo(File(mapDir, "doc.kml"), overwrite = true)
            emptyList()
        }

        val mainKmlRelativePath = pickMainKmlRelativePath(mapDir, zipEntryKmls)
            ?: throw IllegalArgumentException("No KML found in archive.")
        val mainKmlFile = File(mapDir, mainKmlRelativePath)

        val parsed = parseKml(mainKmlFile, mapDir)
        val mapName = parsed.docName
            ?: nameGuess.removeSuffix(".kmz").removeSuffix(".kml").ifBlank { "Map" }

        TacticalMap(
            id = id,
            name = mapName,
            dirPath = mapDir.absolutePath,
            mainKmlRelativePath = mainKmlRelativePath,
            sourceUriString = uri.toString(),
            groundOverlay = parsed.groundOverlay,
            points = parsed.points,
            lines = parsed.lines,
            polygons = parsed.polygons,
        )
    }

    private fun copySource(application: Application, uri: Uri, outFile: File) {
        try {
            application.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Failed to open file." }
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to open file.", e)
        }
    }

    private fun isZip(src: File): Boolean {
        return src.inputStream().use { ins ->
            val b0 = ins.read()
            val b1 = ins.read()
            b0 == 'P'.code && b1 == 'K'.code
        }
    }

    private fun unzipKmz(src: File, mapDir: File): List<String> {
        val kmlEntries = mutableListOf<String>()
        try {
            ZipInputStream(src.inputStream().buffered()).use { zin ->
                var entry: ZipEntry? = zin.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = safeJoin(mapDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            zin.copyTo(out)
                        }
                        val rel = outFile.relativeTo(mapDir).path.replace('\\', '/')
                        if (rel.lowercase().endsWith(".kml")) {
                            kmlEntries += rel
                        }
                    }
                    zin.closeEntry()
                    entry = zin.nextEntry
                }
            }
        } catch (e: ZipException) {
            throw IllegalArgumentException("File is corrupted or not a valid KMZ/KML.", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("File is corrupted or not a valid KMZ/KML.", e)
        }
        return kmlEntries
    }

    private fun pickMainKmlRelativePath(mapDir: File, zipOrderedKmlEntries: List<String>): String? {
        val docKml = File(mapDir, "doc.kml")
        if (docKml.exists()) return "doc.kml"

        val firstZipKml = zipOrderedKmlEntries.firstOrNull()
        if (!firstZipKml.isNullOrBlank()) return firstZipKml

        return mapDir.walkTopDown()
            .firstOrNull { it.isFile && it.extension.equals("kml", ignoreCase = true) }
            ?.relativeTo(mapDir)
            ?.path
            ?.replace('\\', '/')
    }

    private fun safeJoin(root: File, entryName: String): File {
        val cleaned = entryName.replace('\\', '/').trimStart('/')
        val out = File(root, cleaned)
        val canonRoot = root.canonicalPath
        val canonOut = out.canonicalPath
        val rootPrefix = "$canonRoot${File.separator}"
        require(canonOut == canonRoot || canonOut.startsWith(rootPrefix)) { "Invalid KMZ entry path." }
        return out
    }

    private fun parseKml(kmlFile: File, mapDir: File): ParsedKml {
        var docName: String? = null
        var overlay: GroundOverlay? = null
        val points = mutableListOf<KmlPoint>()
        val lines = mutableListOf<KmlLine>()
        val polygons = mutableListOf<KmlPolygon>()

        val parser = Xml.newPullParser().apply {
            setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true)
            setInput(kmlFile.inputStream().buffered(), "UTF-8")
        }

        fun readText(): String {
            var value = ""
            if (parser.next() == XmlPullParser.TEXT) {
                value = parser.text.orEmpty()
                parser.nextTag()
            }
            return value.trim()
        }

        fun skipTag() {
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_TAG -> depth--
                }
            }
        }

        fun parseCoordinates(raw: String): List<Pair<Double, Double>> {
            return raw.trim()
                .split(Regex("\\s+"))
                .mapNotNull { token ->
                    val parts = token.split(',')
                    if (parts.size < 2) return@mapNotNull null
                    val lon = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                    val lat = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                    Pair(lat, lon)
                }
        }

        fun parseArgbColor(raw: String): Long? {
            val normalized = raw.trim().removePrefix("#")
            val hex = when (normalized.length) {
                8 -> normalized
                6 -> "FF$normalized"
                else -> return null
            }
            return hex.toLongOrNull(16)
        }

        fun parseGroundOverlay(): GroundOverlay {
            var name = "Map"
            var href = ""
            var north = 0.0
            var south = 0.0
            var east = 0.0
            var west = 0.0
            var rotation = 0.0

            parser.require(XmlPullParser.START_TAG, null, "GroundOverlay")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "name" -> name = readText().ifBlank { name }
                    "Icon" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            if (parser.name == "href") href = readText() else skipTag()
                        }
                    }
                    "LatLonBox" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            when (parser.name) {
                                "north" -> north = readText().toDoubleOrNull() ?: north
                                "south" -> south = readText().toDoubleOrNull() ?: south
                                "east" -> east = readText().toDoubleOrNull() ?: east
                                "west" -> west = readText().toDoubleOrNull() ?: west
                                "rotation" -> rotation = readText().toDoubleOrNull() ?: rotation
                                else -> skipTag()
                            }
                        }
                    }
                    else -> skipTag()
                }
            }

            val imgFile = safeJoin(mapDir, href)
            val relHref = imgFile.relativeTo(mapDir).path.replace('\\', '/')
            return GroundOverlay(
                name = name,
                imageHref = relHref,
                north = north,
                south = south,
                east = east,
                west = west,
                rotationDeg = rotation,
            )
        }

        fun parsePlacemark() {
            var name = ""
            var description = ""
            var pointCoords: List<Pair<Double, Double>>? = null
            var lineCoords: List<Pair<Double, Double>>? = null
            var polygonOuter: List<Pair<Double, Double>>? = null
            var iconRaw: String? = null
            var colorArgb: Long? = null

            parser.require(XmlPullParser.START_TAG, null, "Placemark")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "name" -> name = readText()
                    "description" -> description = readText()
                    "ExtendedData" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            if (parser.name != "Data") {
                                skipTag()
                                continue
                            }
                            val dataName = parser.getAttributeValue(null, "name").orEmpty()
                            var dataValue: String? = null
                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.eventType != XmlPullParser.START_TAG) continue
                                if (parser.name == "value") {
                                    dataValue = readText()
                                } else {
                                    skipTag()
                                }
                            }
                            when (dataName) {
                                "teamcompass_icon" -> {
                                    iconRaw = dataValue?.trim().orEmpty().ifBlank { null }
                                }
                                "teamcompass_color" -> {
                                    colorArgb = dataValue?.let(::parseArgbColor)
                                }
                            }
                        }
                    }
                    "Point" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            if (parser.name == "coordinates") {
                                pointCoords = parseCoordinates(readText())
                            } else {
                                skipTag()
                            }
                        }
                    }
                    "LineString" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            if (parser.name == "coordinates") {
                                lineCoords = parseCoordinates(readText())
                            } else {
                                skipTag()
                            }
                        }
                    }
                    "Polygon" -> {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.eventType != XmlPullParser.START_TAG) continue
                            when (parser.name) {
                                "outerBoundaryIs" -> {
                                    while (parser.next() != XmlPullParser.END_TAG) {
                                        if (parser.eventType != XmlPullParser.START_TAG) continue
                                        if (parser.name == "coordinates") {
                                            polygonOuter = parseCoordinates(readText())
                                        } else {
                                            skipTag()
                                        }
                                    }
                                }
                                else -> skipTag()
                            }
                        }
                    }
                    else -> skipTag()
                }
            }

            val id = UUID.randomUUID().toString()
            val firstPoint = pointCoords?.firstOrNull()
            if (firstPoint != null) {
                points += KmlPoint(
                    id = id,
                    name = name,
                    description = description,
                    lat = firstPoint.first,
                    lon = firstPoint.second,
                    iconRaw = iconRaw,
                    colorArgb = colorArgb,
                )
            }
            if (!lineCoords.isNullOrEmpty()) {
                lines += KmlLine(id = id, name = name, coords = lineCoords.orEmpty())
            }
            if (!polygonOuter.isNullOrEmpty()) {
                polygons += KmlPolygon(id = id, name = name, outer = polygonOuter.orEmpty())
            }
        }

        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "name" -> {
                        if (docName == null) {
                            docName = readText()
                        } else {
                            readText()
                        }
                    }
                    "GroundOverlay" -> overlay = parseGroundOverlay()
                    "Placemark" -> parsePlacemark()
                }
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to read KML.", e)
        }

        return ParsedKml(
            docName = docName,
            groundOverlay = overlay,
            points = points,
            lines = lines,
            polygons = polygons,
        )
    }
}

private data class ParsedKml(
    val docName: String?,
    val groundOverlay: GroundOverlay?,
    val points: List<KmlPoint>,
    val lines: List<KmlLine>,
    val polygons: List<KmlPolygon>,
)
