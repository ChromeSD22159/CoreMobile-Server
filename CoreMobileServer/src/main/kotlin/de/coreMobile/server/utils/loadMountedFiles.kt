package de.coreMobile.server.utils

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.file.Path

inline fun <reified T> loadMountedFiles(fileName: String): T? {
    try {
        val filePath = "CoreMobileServer/CustomerFiles/$fileName"
        val prefix = System.getenv("CONFIG_PREFIX") ?: ""
        val generatedFilePath = Path.of(prefix + filePath).toFile().readText()
        return Yaml.default.decodeFromString(serializer(), generatedFilePath)
    } catch (e: Exception) {
        throw RuntimeException("Error loading $fileName", e)
    }
}

inline fun <reified T> loadMountedJson(fileName: String): T? {
    try {
        val filePath = "CoreMobileServer/CustomerFiles/$fileName"
        val prefix = System.getenv("CONFIG_PREFIX") ?: ""
        val generatedFilePath = Path.of(prefix + filePath).toFile().readText()
        return Json.decodeFromString(serializer(), generatedFilePath)
    } catch (e: Exception) {
        throw RuntimeException("Error loading $fileName", e)
    }
}