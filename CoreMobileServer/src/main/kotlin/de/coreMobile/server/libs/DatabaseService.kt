package de.coreMobile.server.libs

import de.coreMobile.server.model.coreMobileConfig.CoreMobilesConfig
import de.coreMobile.server.model.coreMobileConfig.DatabaseConnection
import de.coreMobile.server.model.databaseService.TablesConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class DatabaseService(
    private val environment: ApplicationEnvironment,
    private var yamlTables: TablesConfig? = null,
    private var yamlConfig: CoreMobilesConfig? = null
) {
    val logging = Logging("Database")
    var generatedTables: Map<String, Table> = emptyMap()
    var swaggerSchemas: Map<String, Any> = emptyMap()

    init {
        try {
            connectToPostgres()

            yamlTables?.let {
                generatedTables = createExposedTables(it)
                swaggerSchemas = generateSwaggerSchemas(it)
            }

            if(generatedTables.isNotEmpty()) {
                transaction {
                    SchemaUtils.create(*generatedTables.values.toTypedArray())
                }
            }
        } catch (e: Exception) {
            logging.error(e.message.toString())
        }
    }

    private fun createExposedTables(
        tablesConfig: TablesConfig
    ): Map<String, Table> {
        val createdTables = mutableMapOf<String, Table>()

        data class ForeignKeyInfo(val fromTable: String, val fromColumn: String, val toTable: String)

        val foreignKeys = mutableListOf<ForeignKeyInfo>()

        tablesConfig.tables.forEach { (tableName, tableConfig) ->

            /*
            if (tableName.equals("User", ignoreCase = true)) {
                return@forEach
            }
             */

            val pkColumns = mutableListOf<Column<*>>()
            val columnRefs = mutableMapOf<String, Column<*>>()

            val table = object : Table(tableName) {
                init {
                    tableConfig.columns.forEach { (columnName, columnTypeString) ->
                        val parts = columnTypeString.split(" ")
                        val columnType = parts[0].lowercase()
                        val constraints = parts.drop(1)

                        val column: Column<*> = when (columnType) {
                            "uuid" -> uuid(columnName)
                            "varchar(255)" -> varchar(columnName, 255)
                            "text" -> text(columnName)
                            "timestamp" -> datetime(columnName)
                            "integer", "int" -> integer(columnName)
                            "boolean" -> bool(columnName)
                            else -> throw IllegalArgumentException("Unsupported column type: $columnType in $tableName.$columnName")
                        }

                        constraints.forEach { constraint ->
                            when {
                                constraint.equals("pk", ignoreCase = true) -> pkColumns.add(column)
                                constraint.equals("unique", ignoreCase = true) -> column.uniqueIndex()
                                constraint.startsWith("fk(") -> {
                                    val refTable = constraint.removePrefix("fk(").removeSuffix(")")
                                    foreignKeys.add(ForeignKeyInfo(tableName, columnName, refTable))
                                }
                            }
                        }

                        columnRefs[columnName] = column
                    }
                }

                override val primaryKey: PrimaryKey? =
                    if (pkColumns.isNotEmpty()) PrimaryKey(pkColumns.first(), *pkColumns.drop(1).toTypedArray())
                    else super.primaryKey
            }

            createdTables[tableName] = table
        }

        return createdTables
    }

    private fun generateSwaggerSchemas(tablesConfig: TablesConfig): Map<String, Any> {
        if(tablesConfig.tables.isEmpty()) return emptyMap()

        val schemas = generateSchemasOnly(tablesConfig)
        val paths = generatePaths()
        return mapOf(
            "openapi" to "3.0.0",
            "info" to mapOf(
                "title" to "Generated API",
                "version" to "1.0.0"
            ),
            "paths" to paths,
            "components" to mapOf(
                "schemas" to schemas
            )
        )
    }

    private fun generatePaths(): Map<String, Any> {
        val tablesConfig: TablesConfig = TablesConfig(
            tables = emptyMap()
        )  //yamlTables
        val paths = mutableMapOf<String, Any>()

        paths["/database/export"] = mapOf(
            "get" to mapOf(
                "summary" to "Exportiert die gesamte Datenbank als ZIP-Datei", // Füge eine Beschreibung hinzu
                "tags" to listOf("Database"), // Übernehme die Tags
                "responses" to mapOf(
                    "200" to mapOf(
                        "description" to "Erfolgreiche Antwort. Liefert eine ZIP-Datei der Datenbank.",
                        "content" to mapOf(
                            "application/zip" to mapOf( // Korrekter Content-Typ
                                "schema" to mapOf(
                                    "type" to "string",
                                    "format" to "binary" // Gibt an, dass es sich um Binärdaten handelt
                                )
                            )
                        )
                    )
                )
            )
        )


        tablesConfig.tables.forEach { table ->
            val tableName = generatedTables// Beispiel: Tabellennamen als Teil des Pfades

            // Beispiel für einen GET-Endpunkt für alle Einträge der Tabelle
            paths["/$tableName"] = mapOf(
                "get" to mapOf(
                    "summary" to "Ruft alle Einträge von ${tableName} ab",
                    "responses" to mapOf(
                        "200" to mapOf(
                            "description" to "Erfolgreiche Antwort",
                            "content" to mapOf(
                                "application/json" to mapOf(
                                    "schema" to mapOf(
                                        "type" to "array",
                                        "items" to mapOf("\$ref" to "#/components/schemas/${tableName}")
                                    )
                                )
                            )
                        )
                    )
                )
            )

            // Beispiel für einen GET-Endpunkt für einen einzelnen Eintrag anhand der ID (angenommen, es gibt ein Feld namens 'id')
            paths["/$tableName/{id}"] = mapOf(
                "get" to mapOf(
                    "summary" to "Ruft einen Eintrag von ${tableName} anhand der ID ab",
                    "parameters" to listOf(
                        mapOf(
                            "name" to "id",
                            "in" to "path",
                            "required" to true,
                            "schema" to mapOf("type" to "integer") // Annahme: ID ist ein Integer
                        )
                    ),
                    "responses" to mapOf(
                        "200" to mapOf(
                            "description" to "Erfolgreiche Antwort",
                            "content" to mapOf(
                                "application/json" to mapOf(
                                    "schema" to mapOf("\$ref" to "#/components/schemas/${tableName}")
                                )
                            )
                        ),
                        "404" to mapOf("description" to "Eintrag nicht gefunden")
                    )
                )
            )

            // Hier könntest du weitere Endpunkte hinzufügen (POST, PUT, DELETE, etc.)
        }

        return paths
    }

    private fun generateSchemasOnly(tablesConfig: TablesConfig): Map<String, Any> {
        return tablesConfig.tables.mapValues { (_, tableConfig) ->
            mapOf(
                "type" to "object",
                "properties" to tableConfig.columns.mapValues { (_, columnTypeString) ->
                    val type = columnTypeString.split(" ").first().lowercase()
                    mapOf("type" to mapToSwaggerType(type))
                }
            )
        }
    }

    private fun mapToSwaggerType(type: String): String = when {
        type.startsWith("uuid") -> "string"
        type.startsWith("varchar") -> "string"
        type == "text" -> "string"
        type == "timestamp" -> "string"
        type == "integer" || type == "int" -> "integer"
        type == "boolean" -> "boolean"
        else -> "string"
    }

    private fun connectToPostgres() {
        val db = Database
        val dockerDB = DatabaseConnection(
            environment.config.property("storage.jdbcURL").getString(),
            environment.config.property("storage.user").getString(),
            environment.config.property("storage.password").getString()
        )

        val dbData: DatabaseConnection = if (yamlConfig?.externalDb != null) {
            if (yamlConfig?.externalDb?.url.isNullOrBlank() || yamlConfig?.externalDb?.user.isNullOrBlank() || yamlConfig?.externalDb?.password.isNullOrBlank()) {
                logging.info("Docker DB used")
                dockerDB
            } else {
                logging.info("External DB used")
                DatabaseConnection(
                    yamlConfig!!.externalDb!!.url!!,
                    yamlConfig!!.externalDb!!.user!!,
                    yamlConfig!!.externalDb!!.password!!
                )
            }
        } else {
            logging.info("Docker DB used")
            dockerDB
        }

        logging.info("Docker DB: ${dbData.url}")

        try {
            db.connect(
                dbData.url,
                user = dbData.user,
                password = dbData.password
            )
            logging.info("Connection successful")
        } catch (e: Exception) {
            logging.error("Connection failed")
            logging.error(e.message.toString())
        }
    }

    suspend fun exportDatabaseALL(): ByteArray {
        val tables = generatedTables.values.map { it.tableName }
        val tableData = withContext(Dispatchers.IO) {
            transaction {
                tables.associateWith { tableName ->
                    val table = generatedTables[tableName] ?: error("Table '$tableName' not found")
                    table.selectAll().toList().map { row ->
                        table.columns.associateWith { column ->
                            row[column]
                        }.mapKeys { it.key.name }.mapValues { it.value?.toString() ?: "" }
                    }
                }
            }
        }

        val csvDataMap = tableData.mapValues { (_, data) -> formatAsCsv(data) }
        return createZipFile(csvDataMap)
    }

    private suspend fun createZipFile(data: Map<String, String>): ByteArray = withContext(Dispatchers.IO) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
            data.forEach { (tableName, csvContent) ->
                val entry = ZipEntry("$tableName.csv")
                zipOutputStream.putNextEntry(entry)
                zipOutputStream.write(csvContent.toByteArray())
                zipOutputStream.closeEntry()
            }
        }
        return@withContext byteArrayOutputStream.toByteArray()
    }

    private fun formatAsCsv(data: List<Map<String, Any>>): String {
        if (data.isEmpty()) return ""
        val header = data.first().keys.joinToString(",") + "\n"
        val rows = data.joinToString("\n") { row ->
            row.values.joinToString(",") { it.toString().replace(",", "\\,") }
        }
        return header + rows
    }
}