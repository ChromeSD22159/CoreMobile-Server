package de.coreMobile.server.genericRoutes

import de.coreMobile.server.libs.Logging
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.genericPostRoute(
    generatedTables: Map<String, Table>
) {
    val logging = Logging("genericPostRoute")

    for (tableItem in generatedTables) {
        val tableName = tableItem.key
        val table = tableItem.value

        post("/$tableName", {
            description = "Post an entry to a table"
            tags = listOf(tableName)
            request {
                body<JsonObject> {
                    description = "JSON Object to insert"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<JsonObject> {
                        description = "JSON Object to insert"
                    }
                }
                code(HttpStatusCode.BadRequest) {
                    description = "Fehlender oder ungültiger Authorization Header"
                }
            }
        }) {
            val json = call.receive<JsonObject>()

            try {
                transaction {
                    table.insert { stmt ->
                        for (column in table.columns) {
                            val value = json[column.name]
                            if (value != null) {
                                val anyColumn = column as Column<Any?>
                                stmt[anyColumn] = parseJsonValue(value, column.columnType)
                            }
                        }
                    }
                }
                call.ok( "Inserted into $tableName")
            } catch (e: Exception) {
                logging.error("Insert failed for $tableName: ${e.message}")
                call.badRequest("Insert failed: ${e.message}")
            }
        }
    }
}