package de.coreMobile.server.genericRoutes

import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.internalServerError
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Route.genericPutRoute(
    generatedTables: Map<String, Table>
) {
    for (tableItem in generatedTables) {
        val tableName = tableItem.key
        val table = tableItem.value

        put("/$tableName/{id}", {
            description = "Get an entry from a table by ID"
            tags = listOf(tableName)
            request {
                pathParameter<String>("id") {
                    description = "ID of the entry to get"
                    required = true
                }
                body<JsonObject> {
                    description = "JSON Object to insert"
                    required = true
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<JsonObject> {
                        description = "JSON Object to update"
                    }
                }
                code(HttpStatusCode.BadRequest) {
                    description = "Fehlender oder ungültiger Authorization Header"
                }
            }
        }) {
            var updatedRowCount = 0
            val json = call.receive<JsonObject>()
            val idParameter = call.parameters["id"]
            try {
                val idColumn = table.columns.find { it.name == "id" } ?: throw Exception("Table '$tableName' has no 'id' column.")
                val idParam = idParameter ?: throw Exception("Missing 'id' parameter")
                transaction {
                    updatedRowCount = table.update({
                        val idValue = parseIdParam(idParam, idColumn.columnType) ?: throw IllegalArgumentException("Invalid ID format")
                        (idColumn as Column<Any>) eq idValue
                    }) { stmt ->
                        for (column in table.columns) {
                            if (column.name == "id") continue

                            val value = json[column.name]
                            if (value != null) {
                                try {
                                    val parsedValue = parseJsonValue(value, column.columnType)
                                    @Suppress("UNCHECKED_CAST")
                                    stmt[column as Column<Any?>] = parsedValue
                                } catch (e: Exception) {
                                    throw BadRequestException("Invalid value for column '${column.name}'")
                                }
                            }
                        }
                    }
                }
                if (updatedRowCount > 0) {
                    call.ok( "Updated into $tableName")
                } else {
                    throw NotFoundException("No entry found with ID '$idParam' in table '$tableName'")
                }
            } catch (e: BadRequestException) {
                call.badRequest(e.message)
            } catch (e: IllegalArgumentException) {
                call.badRequest("Invalid ID format")
            } catch (e: Exception) {
                call.internalServerError("Error updating table '$tableName' with ID '$idParameter': ${e.message}")
            }
        }
    }
}