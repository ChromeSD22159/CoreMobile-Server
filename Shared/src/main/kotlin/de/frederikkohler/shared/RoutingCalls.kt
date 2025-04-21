package de.frederikkohler.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend fun RoutingCall.badRequest() {
    this.respond(HttpStatusCode.BadRequest, null)
}

suspend fun RoutingCall.unauthorized() {
    this.respond(HttpStatusCode.Unauthorized, null)
}

suspend fun RoutingCall.noContent() {
    this.respond(HttpStatusCode.NoContent, null)
}

suspend inline fun <reified T : Any> RoutingCall.ok(response: T) {
    this.respond(HttpStatusCode.OK, response)
}

fun RoutingCall.getAuthToken(): String? {
    val headerValue = request.headers["Authorization"]
    return if (!headerValue.isNullOrBlank() && headerValue.startsWith("Bearer ")) {
        headerValue.substringAfter("Bearer ").trim()
    } else {
        null
    }
}