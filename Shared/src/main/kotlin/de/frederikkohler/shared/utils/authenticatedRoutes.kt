package de.frederikkohler.shared.utils

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing

fun Application.authenticatedRoutes(
    routes: Routing.() -> Unit
) {
    routing {
        authenticate("authenticated") {
            routes()
        }
    }
}