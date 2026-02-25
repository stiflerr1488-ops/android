package com.example.teamcompass.ui

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.math.abs

object KmzMapSaver {

    suspend fun saveToUri(
        application: Application,
        map: TacticalMap,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint> = emptyList(),
        destinationUri: Uri,
    ) = withContext(Dispatchers.IO) {
        if (newPoints.isEmpty() && deletedPoints.isEmpty()) return@withContext

        val mapDir = File(map.dirPath)
        require(mapDir.exists() && mapDir.isDirectory) { "Map files not found." }

        val mainKml = safeJoinInside(mapDir, map.mainKmlRelativePath)
        require(mainKml.exists() && mainKml.isFile) { "Main KML file not found." }

        reconcilePointsInKmlAtomic(mainKml, newPoints, deletedPoints)

        val tempKmz = File.createTempFile("kmz_export_", ".kmz", application.cacheDir)
        try {
            packDirectoryToKmz(mapDir = mapDir, outKmz = tempKmz)
            writeOutput(application, tempKmz, destinationUri)
        } finally {
            tempKmz.delete()
        }
    }

    private fun reconcilePointsInKmlAtomic(
        kmlFile: File,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint>,
    ) {
        val doc = try {
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
            }.newDocumentBuilder().parse(kmlFile)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to read KML.", e)
        }

        if (deletedPoints.isNotEmpty()) {
            removeDeletedPlacemarks(doc, deletedPoints)
        }

        val container = findPlacemarkContainer(doc)
        val ns = doc.documentElement?.namespaceURI
        newPoints.forEach { point ->
            container.appendChild(buildPlacemarkNode(doc, ns, point))
        }

        val tempFile = File(kmlFile.parentFile, "${kmlFile.name}.tmp")
        val backupFile = File(kmlFile.parentFile, "${kmlFile.name}.bak")

        try {
            writeXml(doc, tempFile)
            if (backupFile.exists()) {
                backupFile.delete()
            }
            if (kmlFile.exists() && !kmlFile.renameTo(backupFile)) {
                throw IllegalStateException("Failed to write file.")
            }
            if (!tempFile.renameTo(kmlFile)) {
                if (backupFile.exists()) {
                    backupFile.renameTo(kmlFile)
                }
                throw IllegalStateException("Failed to write file.")
            }
            if (backupFile.exists()) {
                backupFile.delete()
            }
        } catch (e: IllegalStateException) {
            tempFile.delete()
            throw e
        } catch (e: Exception) {
            tempFile.delete()
            throw IllegalStateException("Failed to write file.", e)
        }
    }

    private fun removeDeletedPlacemarks(doc: Document, deletedPoints: List<KmlPoint>) {
        val placemarkNodes = mutableListOf<Element>()
        val nodeList = doc.getElementsByTagNameNS("*", "Placemark")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node is Element) {
                placemarkNodes += node
            }
        }
        if (placemarkNodes.isEmpty()) return

        val remainingToDelete = deletedPoints.toMutableList()
        placemarkNodes.forEach { placemark ->
            if (remainingToDelete.isEmpty()) return@forEach
            val parsed = parsePlacemarkPoint(placemark) ?: return@forEach
            val index = remainingToDelete.indexOfFirst { pointsEquivalent(it, parsed) }
            if (index < 0) return@forEach

            placemark.parentNode?.removeChild(placemark)
            remainingToDelete.removeAt(index)
        }
    }

    private fun parsePlacemarkPoint(placemark: Element): KmlPoint? {
        val coordsText = firstDescendantText(placemark, "Point", "coordinates") ?: return null
        val (lat, lon) = parseCoordinates(coordsText) ?: return null

        val name = firstChildText(placemark, "name").orEmpty().trim()
        val description = firstChildText(placemark, "description").orEmpty().trim()
        val iconRaw = findExtendedDataValue(placemark, "teamcompass_icon")
            ?.trim()
            .orEmpty()
            .ifBlank { null }
        val colorArgb = findExtendedDataValue(placemark, "teamcompass_color")?.let(::parseArgbColor)

        return KmlPoint(
            id = "",
            name = name,
            description = description,
            lat = lat,
            lon = lon,
            iconRaw = iconRaw,
            colorArgb = colorArgb,
        )
    }

    private fun firstChildText(parent: Element, localName: String): String? {
        val childNodes = parent.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i) as? Element ?: continue
            if (node.localName == localName || node.nodeName == localName) {
                return node.textContent
            }
        }
        return null
    }

    private fun firstDescendantText(parent: Element, firstTag: String, secondTag: String): String? {
        val firstLevel = parent.getElementsByTagNameNS("*", firstTag)
        if (firstLevel.length <= 0) return null
        val first = firstLevel.item(0) as? Element ?: return null
        val secondLevel = first.getElementsByTagNameNS("*", secondTag)
        if (secondLevel.length <= 0) return null
        return secondLevel.item(0)?.textContent
    }

    private fun findExtendedDataValue(placemark: Element, key: String): String? {
        val dataNodes = placemark.getElementsByTagNameNS("*", "Data")
        for (i in 0 until dataNodes.length) {
            val data = dataNodes.item(i) as? Element ?: continue
            if (data.getAttribute("name") != key) continue
            val valueNodes = data.getElementsByTagNameNS("*", "value")
            if (valueNodes.length <= 0) return null
            return valueNodes.item(0)?.textContent
        }
        return null
    }

    private fun parseCoordinates(raw: String): Pair<Double, Double>? {
        val token = raw.trim().split(Regex("\\s+")).firstOrNull() ?: return null
        val parts = token.split(',')
        if (parts.size < 2) return null
        val lon = parts[0].toDoubleOrNull() ?: return null
        val lat = parts[1].toDoubleOrNull() ?: return null
        return Pair(lat, lon)
    }

    private fun parseArgbColor(raw: String): Long? {
        val normalized = raw.trim().removePrefix("#")
        val hex = when (normalized.length) {
            8 -> normalized
            6 -> "FF$normalized"
            else -> return null
        }
        return hex.toLongOrNull(16)
    }

    private fun pointsEquivalent(a: KmlPoint, b: KmlPoint): Boolean {
        val sameName = a.name.trim() == b.name.trim()
        val sameDescription = a.description.trim() == b.description.trim()
        val sameIcon = (a.iconRaw?.trim().orEmpty()) == (b.iconRaw?.trim().orEmpty())
        val sameColor = (a.colorArgb ?: -1L) == (b.colorArgb ?: -1L)
        val sameLat = abs(a.lat - b.lat) <= 1e-6
        val sameLon = abs(a.lon - b.lon) <= 1e-6
        return sameName && sameDescription && sameIcon && sameColor && sameLat && sameLon
    }

    private fun buildPlacemarkNode(
        doc: Document,
        namespaceUri: String?,
        point: KmlPoint,
    ): Element {
        fun create(name: String): Element {
            return if (namespaceUri.isNullOrBlank()) {
                doc.createElement(name)
            } else {
                doc.createElementNS(namespaceUri, name)
            }
        }

        val placemark = create("Placemark")
        if (point.name.isNotBlank()) {
            val name = create("name")
            name.textContent = point.name
            placemark.appendChild(name)
        }
        if (point.description.isNotBlank()) {
            val description = create("description")
            description.textContent = point.description
            placemark.appendChild(description)
        }
        if (!point.iconRaw.isNullOrBlank() || point.colorArgb != null) {
            val extended = create("ExtendedData")
            point.iconRaw?.takeIf { it.isNotBlank() }?.let { icon ->
                val data = create("Data")
                data.setAttribute("name", "teamcompass_icon")
                val value = create("value")
                value.textContent = icon
                data.appendChild(value)
                extended.appendChild(data)
            }
            point.colorArgb?.let { color ->
                val data = create("Data")
                data.setAttribute("name", "teamcompass_color")
                val value = create("value")
                value.textContent = String.format(Locale.US, "#%08X", color and 0xFFFFFFFFL)
                data.appendChild(value)
                extended.appendChild(data)
            }
            if (extended.hasChildNodes()) {
                placemark.appendChild(extended)
            }
        }
        val pointNode = create("Point")
        val coordinates = create("coordinates")
        coordinates.textContent = String.format(
            Locale.US,
            "%.7f,%.7f,0",
            point.lon,
            point.lat,
        )
        pointNode.appendChild(coordinates)
        placemark.appendChild(pointNode)
        return placemark
    }

    private fun findPlacemarkContainer(doc: Document): Element {
        val byDocument = doc.getElementsByTagNameNS("*", "Document")
        if (byDocument.length > 0) return byDocument.item(0) as Element

        val byFolder = doc.getElementsByTagNameNS("*", "Folder")
        if (byFolder.length > 0) return byFolder.item(0) as Element

        return doc.documentElement ?: throw IllegalArgumentException("Failed to read KML.")
    }

    private fun writeXml(doc: Document, outFile: File) {
        FileOutputStream(outFile).use { out ->
            val transformer = TransformerFactory.newInstance().newTransformer().apply {
                setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty(OutputKeys.METHOD, "xml")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            }
            transformer.transform(DOMSource(doc), StreamResult(out))
        }
    }

    private fun packDirectoryToKmz(mapDir: File, outKmz: File) {
        try {
            ZipOutputStream(outKmz.outputStream().buffered()).use { zip ->
                mapDir.walkTopDown()
                    .filter { it.isFile }
                    .sortedBy { it.relativeTo(mapDir).path }
                    .forEach { file ->
                        val rel = file.relativeTo(mapDir).path.replace('\\', '/')
                        if (rel == "source" || rel.endsWith(".bak") || rel.endsWith(".tmp")) return@forEach
                        zip.putNextEntry(ZipEntry(rel))
                        file.inputStream().buffered().use { input -> input.copyTo(zip) }
                        zip.closeEntry()
                    }
            }
        } catch (e: Exception) {
            throw IllegalStateException("KMZ packaging failed.", e)
        }
    }

    private fun writeOutput(application: Application, file: File, uri: Uri) {
        try {
            if (uri.scheme.equals("file", ignoreCase = true)) {
                val target = uri.path?.let(::File) ?: throw IllegalStateException("Failed to write file.")
                target.parentFile?.mkdirs()
                file.inputStream().buffered().use { input ->
                    target.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }
                return
            }

            application.contentResolver.openOutputStream(uri).use { out ->
                requireNotNull(out) { "Failed to write file." }
                file.inputStream().buffered().use { input ->
                    input.copyTo(out)
                }
            }
        } catch (e: SecurityException) {
            throw IllegalStateException("No access to source file for overwrite.", e)
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("Failed to write file.", e)
        }
    }

    private fun safeJoinInside(root: File, relativePath: String): File {
        val cleaned = relativePath.replace('\\', '/').trimStart('/')
        val out = File(root, cleaned)
        val canonRoot = root.canonicalPath
        val canonOut = out.canonicalPath
        val prefix = "$canonRoot${File.separator}"
        require(canonOut == canonRoot || canonOut.startsWith(prefix)) {
            "Invalid KML path."
        }
        return out
    }
}
