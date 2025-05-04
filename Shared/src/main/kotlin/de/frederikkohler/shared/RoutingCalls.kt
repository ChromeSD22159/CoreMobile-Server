package de.frederikkohler.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend fun RoutingCall.badRequest(msg: String?) {
    this.respond(HttpStatusCode.BadRequest, msg ?: "")
}

suspend fun RoutingCall.unauthorized(msg: String? = null) {
    this.respond(HttpStatusCode.Unauthorized, msg ?: "")
}

suspend fun RoutingCall.noContent(msg: String? = null) {
    this.respond(HttpStatusCode.NoContent, msg ?: "")
}

suspend fun RoutingCall.internalServerError(msg: String? = null) {
    this.respond(HttpStatusCode.InternalServerError, msg ?: "")
}

suspend fun RoutingCall.notFound(msg: String) {
    this.respond(HttpStatusCode.InternalServerError, msg ?: "")
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