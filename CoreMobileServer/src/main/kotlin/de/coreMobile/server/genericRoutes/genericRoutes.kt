package de.coreMobile.server.genericRoutes

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Table

fun Application.genericRoutes(
    generatedTables: Map<String, Table>
) {
    routing {
        genericGetRoute(generatedTables)
        genericGetByIdRoute(generatedTables)
        genericPostRoute(generatedTables)
        genericPutRoute(generatedTables)
        genericDeleteRoute(generatedTables)
    }
}

