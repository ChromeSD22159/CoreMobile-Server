package de.frederikkohler.shared.configure

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"
    val host = environment.config.propertyOrNull("ktor.deployment.host")?.getString() ?: "0.0.0.0"

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    logStart(host, port)
}

fun logStart(
    host: String,
    port: String,
    logger: org.slf4j.Logger = LoggerFactory.getLogger(Application::class.java)
) {
    logger.info("Swagger UI available: http://${host}:${port}/swagger/index.html#/")
}
