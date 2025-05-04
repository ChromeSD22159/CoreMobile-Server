package de.coreMobile.server.genericRoutes

import de.coreMobile.server.libs.Logging
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.internalServerError
import de.frederikkohler.utils.noContent
import de.frederikkohler.utils.notFound
import io.github.smiley4.ktoropenapi.delete
import io.ktor.server.routing.Route
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.genericDeleteRoute(
    generatedTables: Map<String, Table>
) {
    val logging = Logging("genericDeleteRoute")

    for (tableItem in generatedTables) {
        val tableName = tableItem.key
        val table = tableItem.value

        delete("/{$tableName}/{id}", {
            description = "Delete an entry from a table by ID"
            tags = listOf(tableName)
            request {
                pathParameter<String>("id") {
                    description = "ID of the entry to delete"
                }
            }
        }) {
            val idParam = call.parameters["id"]

            val idColumn = table.columns.find { it.name == "id" }
            if (idColumn == null) {
                logging.error("Table '$tableName' has no 'id' column for deletion.")
                call.internalServerError()
                return@delete
            }

            var deleteCount: Int? = null
            var errorMessage: String? = null
            var invalidIdFormat = false
            var unsupportedIdType = false

            try {
                transaction {
                    val whereClause: Op<Boolean>? = when (idColumn.columnType) {
                        is UUIDColumnType -> {
                            val id: UUID? = try {
                                UUID.fromString(idParam)
                            } catch (e: IllegalArgumentException) {
                                invalidIdFormat = true
                                null
                            }
                            @Suppress("UNCHECKED_CAST")
                            (idColumn as Column<UUID>).let { col ->
                                id?.let { col eq it }
                            }
                        }
                        is IntegerColumnType -> {
                            val id = idParam?.toIntOrNull()
                            if (id == null) {
                                invalidIdFormat = true
                                null
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                (idColumn as Column<Int>) eq id
                            }
                        }
                        else -> {
                            logging.error("Unsupported ID column type (${idColumn.columnType}) for generic delete on table '$tableName'.")
                            unsupportedIdType = true
                            null
                        }
                    }

                    if (!invalidIdFormat && !unsupportedIdType && whereClause != null) {
                        deleteCount = table.deleteWhere { whereClause }
                    }
                }
            } catch (e: Exception) {
                logging.error("Error deleting from table '$tableName' with ID '$idParam': ${e.message}")
                errorMessage = "Database error"
            }

            when {
                invalidIdFormat -> call.badRequest("Invalid ID format")
                unsupportedIdType -> call.badRequest("Unsupported ID type")
                errorMessage != null -> call.internalServerError(errorMessage)
                deleteCount != null && deleteCount!! > 0 -> call.noContent()
                else -> call.notFound("No entry found with ID '$idParam' in table '$tableName'")
            }
        }
    }
}