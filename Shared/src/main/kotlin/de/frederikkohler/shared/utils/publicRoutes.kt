package de.frederikkohler.shared.utils

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing

fun Application.publicRoutes(
    routing: Routing.() -> Unit
) {
   routing {
       routing()
   }
}