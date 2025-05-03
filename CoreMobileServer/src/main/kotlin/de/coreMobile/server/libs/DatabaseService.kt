package de.coreMobile.server.libs

import com.charleskorn.kaml.Yaml
import de.coreMobile.server.model.databaseService.TablesConfig
import io.ktor.util.logging.KtorSimpleLogger
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path

class DatabaseService {
    var generatedTables: Map<String, Table> = emptyMap()
    var swaggerSchemas: Map<String, Any> = emptyMap()

    fun init(
        tablesConfigFile: String? = "tables.yaml"
    ) {
        if (tablesConfigFile == null) return
        val yamlTables = loadTablesConfig(tablesConfigFile)

        generatedTables = createExposedTables(yamlTables)
        swaggerSchemas = generateSwaggerSchemas(yamlTables)

        transaction {
            SchemaUtils.create(*generatedTables.values.toTypedArray())
        }
    }

    fun dropTables() {
        transaction {
            SchemaUtils.drop(*generatedTables.values.toTypedArray())
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

    private fun loadTablesConfig(filePath: String): TablesConfig {
        val logger = KtorSimpleLogger("com.example.RequestTracePlugin")
        val yaml = Path.of(filePath).toFile().readText()
        val parsedYaml = Yaml.default.decodeFromString(TablesConfig.serializer(), yaml)
        logger.trace(parsedYaml.toString())
        return parsedYaml
    }

    private fun generateSwaggerSchemas(tablesConfig: TablesConfig): Map<String, Any> {
        if(tablesConfig.tables.isEmpty()) return emptyMap()

        val schemas = generateSchemasOnly(tablesConfig)

        return mapOf(
            "openapi" to "3.0.0",
            "info" to mapOf(
                "title" to "Generated API",
                "version" to "1.0.0"
            ),
            "paths" to mapOf<String, Any>(),
            "components" to mapOf(
                "schemas" to schemas
            )
        )
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
}