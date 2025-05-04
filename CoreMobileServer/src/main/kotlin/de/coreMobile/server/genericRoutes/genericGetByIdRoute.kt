package de.coreMobile.server.genericRoutes

import de.coreMobile.server.libs.Logging
import de.frederikkohler.utils.internalServerError
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.genericGetByIdRoute(
    generatedTables: Map<String, Table>
) {
    val logging = Logging("genericGetByIdRoute")

    for (tableItem in generatedTables) {
        val tableName = tableItem.key
        val table = tableItem.value

        get("/$tableName/{id}", {
            description = "Get an entry from a table by ID"
            tags = listOf(tableName)
            request {
                pathParameter<String>("id") {
                    description = "ID of the entry to get"
                }
            }
        }) {
            try {
                val idParam = call.parameters["id"] ?: throw Exception("Missing 'id' parameter")
                val idColumn = table.columns.find { it.name == "id" } ?: throw Exception("Table '$tableName' has no 'id' column.")
                var foundItem: JsonObject? = null

                transaction {
                    findInTable(idColumn, idParam)?.let {
                        val column = table.selectAll()
                        val query = column.where { it }
                        val result = query.map {
                            buildJsonObject {
                                for (tableColumns in table.columns) {
                                    val value = it[tableColumns]
                                    logging.info("value: $value")
                                    put(tableColumns.name, serializeValue(value))
                                }
                            }
                        }
                        foundItem = result.firstOrNull()
                    }
                }


                if (foundItem != null) {
                    call.ok(foundItem!!)
                } else {
                    throw Exception("No entry found with ID '$idParam' in table '$tableName'")
                }
            } catch (e: Exception) {
                call.internalServerError(e.message)
            }
        }
    }
}

// REFACTOR
fun RoutingContext.findInTable(column: Column<*>, idParam: String): Op<Boolean>? {
    val whereClause: Op<Boolean>? = when (column.columnType) {
        is UUIDColumnType -> {
            val id: UUID? = try {
                UUID.fromString(idParam)
            } catch (e: IllegalArgumentException) {
                null
            }
            @Suppress("UNCHECKED_CAST")
            (column as Column<UUID>).let { col ->
                id?.let { col eq it }
            }
        }
        is IntegerColumnType -> {
            val id = idParam.toIntOrNull()
            if (id == null) {
                null
            } else {
                @Suppress("UNCHECKED_CAST")
                (column as Column<Int>) eq id
            }
        }
        else -> {
            null
        }
    }

    return whereClause
}

fun RoutingContext.serializeValue(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is String -> JsonPrimitive(value)
    is Int -> JsonPrimitive(value)
    is Long -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Float -> JsonPrimitive(value)
    is Double -> JsonPrimitive(value)
    is UUID -> JsonPrimitive(value.toString())
    is java.time.Instant -> JsonPrimitive(value.toString())
    else -> JsonPrimitive(value.toString())
}

fun RoutingContext.parseJsonValue(value: JsonElement, type: IColumnType<*>): Any? {
    return when (type) {
        is IntegerColumnType -> value.jsonPrimitive.int
        is VarCharColumnType -> value.jsonPrimitive.content
        is UUIDColumnType -> UUID.fromString(value.jsonPrimitive.content)
        is BooleanColumnType -> value.jsonPrimitive.boolean
        is TextColumnType -> value.jsonPrimitive.content
        is DecimalColumnType -> value.jsonPrimitive.double.toBigDecimal()
        else -> throw IllegalArgumentException("Unsupported column type: $type")
    }
}

fun RoutingContext.parseIdParam(idParam: String?, columnType: IColumnType<*>): Any? {
    return when (columnType) {
        is UUIDColumnType -> try { UUID.fromString(idParam) } catch (e: IllegalArgumentException) { null }
        is IntegerColumnType -> idParam?.toIntOrNull()
        else -> idParam
    }
}

var defaultUser = """
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "firstname": "Max",
  "lastname": "Mustermann",
  "email": "max.mustermann@example.com",
  "verified": true,
  "counter": 42,
  "profile_id": "f9e8d7c6-b5a4-3210-fedc-ba9876543210"
}
"""