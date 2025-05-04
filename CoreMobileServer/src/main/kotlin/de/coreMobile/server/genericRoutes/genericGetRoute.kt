package de.coreMobile.server.genericRoutes

import de.coreMobile.server.libs.Logging
import de.frederikkohler.utils.internalServerError
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.routing.Route
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.genericGetRoute(
    generatedTables: Map<String, Table>
) {
    val logging = Logging("genericGetRoute")

    for (tableItem in generatedTables) {
        val tableName = tableItem.key
        val table = tableItem.value

        get("/${tableName}", {
            description = "Get all entries from a table"
            tags = listOf(tableName)
        }) {
            try {
                val results = transaction {
                    table.selectAll().map { row ->
                        buildJsonObject {
                            for (column in table.columns) {
                                val value = row[column]
                                put(column.name, serializeValue(value))
                            }
                        }
                    }
                }

                call.ok(JsonArray(results))
            } catch (e: Exception) {
                logging.error("Failed to fetch entries from $tableName: ${e.message}")
                call.internalServerError("Failed to fetch entries from $tableName: ${e.message}")
            }
        }
    }
}