package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
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

        val nameGuess = uri.lastPathSegment?.take(64) ?: "map"

        // Copy source
        val src = File(mapDir, "source")
        application.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Не удалось открыть файл" }
            FileOutputStream(src).use { out ->
                input.copyTo(out)
            }
        }

        // Detect if it's a zip (KMZ) by signature "PK".
        val isZip = src.inputStream().use { ins ->
            val b0 = ins.read()
            val b1 = ins.read()
            b0 == 'P'.code && b1 == 'K'.code
        }

        if (isZip) {
            unzipKmz(src, mapDir)
        } else {
            // Assume plain KML
            src.copyTo(File(mapDir, "doc.kml"), overwrite = true)
        }

        val kmlFile = listOf(
            File(mapDir, "doc.kml"),
        ).firstOrNull { it.exists() } ?: mapDir.walkTopDown().firstOrNull { it.isFile && it.extension.lowercase() == "kml" }
        requireNotNull(kmlFile) { "В KMZ не найден .kml файл" }

        val parsed = parseKml(kmlFile, mapDir)
        val mapName = parsed.first ?: nameGuess.removeSuffix(".kmz").removeSuffix(".kml")

        TacticalMap(
            id = id,
            name = mapName,
            dirPath = mapDir.absolutePath,
            groundOverlay = parsed.second,
            points = parsed.third,
            lines = parsed.fourth,
            polygons = parsed.fifth,
        )
    }

    private fun unzipKmz(src: File, mapDir: File) {
        ZipInputStream(src.inputStream().buffered()).use { zin ->
            var e: ZipEntry? = zin.nextEntry
            while (e != null) {
                val name = e.name
                if (!e.isDirectory) {
                    val outFile = safeJoin(mapDir, name)
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { out ->
                        zin.copyTo(out)
                    }
                }
                zin.closeEntry()
                e = zin.nextEntry
            }
        }
    }

    private fun safeJoin(root: File, entryName: String): File {
        val cleaned = entryName.replace('\\', '/').trimStart('/')
        val out = File(root, cleaned)
        val canonRoot = root.canonicalPath
        val canonOut = out.canonicalPath
        val rootPrefix = "$canonRoot${File.separator}"
        require(canonOut == canonRoot || canonOut.startsWith(rootPrefix)) { "Недопустимый путь в KMZ" }
        return out
    }

    /**
     * Returns: (docName, groundOverlay, points, lines, polygons)
     */
    private fun parseKml(kmlFile: File, mapDir: File): ParsedKml {
        var docName: String? = null
        var overlay: GroundOverlay? = null
        val points = mutableListOf<KmlPoint>()
        val lines = mutableListOf<KmlLine>()
        val polys = mutableListOf<KmlPolygon>()

        val parser = Xml.newPullParser().apply {
            setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true)
            setInput(kmlFile.inputStream().buffered(), "UTF-8")
        }

        fun readText(): String {
            var res = ""
            if (parser.next() == org.xmlpull.v1.XmlPullParser.TEXT) {
                res = parser.text ?: ""
                parser.nextTag()
            }
            return res.trim()
        }

        fun skipTag() {
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    org.xmlpull.v1.XmlPullParser.END_TAG -> depth--
                    org.xmlpull.v1.XmlPullParser.START_TAG -> depth++
                }
            }
        }

        fun parseCoordinates(raw: String): List<Pair<Double, Double>> {
            // KML: "lon,lat[,alt]" separated by spaces/newlines
            return raw
                .trim()
                .split(Regex("\\s+"))
                .mapNotNull { token ->
                    val parts = token.split(',')
                    if (parts.size < 2) null
                    else {
                        val lon = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                        val lat = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                        Pair(lat, lon)
                    }
                }
        }

        fun parseGroundOverlay(): GroundOverlay {
            var name = "Map"
            var href = ""
            var north = 0.0
            var south = 0.0
            var east = 0.0
            var west = 0.0
            var rotation = 0.0

            parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, null, "GroundOverlay")
            while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "name" -> name = readText().ifBlank { name }
                    "Icon" -> {
                        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                            if (parser.name == "href") href = readText() else skipTag()
                        }
                    }
                    "LatLonBox" -> {
                        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
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

            // Normalize href if it points outside (should not after unzip, but just in case).
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
            var pointCoords: List<Pair<Double, Double>>? = null
            var lineCoords: List<Pair<Double, Double>>? = null
            var polyOuter: List<Pair<Double, Double>>? = null

            parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, null, "Placemark")
            while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "name" -> name = readText()
                    "Point" -> {
                        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                            if (parser.name == "coordinates") pointCoords = parseCoordinates(readText()) else skipTag()
                        }
                    }
                    "LineString" -> {
                        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                            if (parser.name == "coordinates") lineCoords = parseCoordinates(readText()) else skipTag()
                        }
                    }
                    "Polygon" -> {
                        // Only outer boundary for MVP
                        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                            when (parser.name) {
                                "outerBoundaryIs" -> {
                                    while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
                                        if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
                                        if (parser.name == "coordinates") polyOuter = parseCoordinates(readText()) else skipTag()
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
            val nm = name.ifBlank { "" }
            val p = pointCoords?.firstOrNull()
            if (p != null) {
                points.add(KmlPoint(id = id, name = nm, lat = p.first, lon = p.second))
            }
            val ln = lineCoords
            if (!ln.isNullOrEmpty()) {
                lines.add(KmlLine(id = id, name = nm, coords = ln))
            }
            val pg = polyOuter
            if (!pg.isNullOrEmpty()) {
                polys.add(KmlPolygon(id = id, name = nm, outer = pg))
            }
        }

        // Main parse loop
        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != org.xmlpull.v1.XmlPullParser.START_TAG) continue
            when (parser.name) {
                "Document" -> {
                    // read inside document for name (optional)
                }
                "name" -> {
                    // Only capture first document name if not set yet.
                    if (docName == null) {
                        docName = readText()
                    } else {
                        // skip nested names
                        readText()
                    }
                }
                "GroundOverlay" -> {
                    overlay = parseGroundOverlay()
                }
                "Placemark" -> {
                    parsePlacemark()
                }
            }
        }

        return ParsedKml(docName, overlay, points, lines, polys)
    }
}

private data class ParsedKml(
    val first: String?,
    val second: GroundOverlay?,
    val third: List<KmlPoint>,
    val fourth: List<KmlLine>,
    val fifth: List<KmlPolygon>,
)
