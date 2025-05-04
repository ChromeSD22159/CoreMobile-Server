package de.coreMobile.server.libs

import com.charleskorn.kaml.Yaml
import de.coreMobile.server.model.coreMobileConfig.CoreMobilesConfig
import de.coreMobile.server.model.databaseService.TablesConfig
import de.coreMobile.server.utils.loadMountedFiles
import io.ktor.server.application.ApplicationEnvironment
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.nio.file.Path

class ConfigService(
    private var environment: ApplicationEnvironment
) {
    private val logging = Logging("ConfigService")

    var yamlTables: TablesConfig? = null
    var yamlConfig: CoreMobilesConfig? = null
    
    init {
        yamlTables = loadTablesConfigYaml()
        yamlConfig = loadConfigYaml()
    }
    
    private fun loadTablesConfigYaml(): TablesConfig? {
        return try {
            return loadMountedFiles("tables.yaml")
        } catch (e: Exception) {
            logging.error("loadTablesConfigYaml: ${e.message.toString()}")
            null
        }
    }

    private fun loadConfigYaml(): CoreMobilesConfig? {
        return try {
            return loadMountedFiles("config.yaml")
        } catch (e: Exception) {
            logging.error("loadConfigYaml: ${e.message.toString()}")
            null
        }
    }
}

