package de.frederikkohler.authapi

import de.frederikkohler.shared.configure.configureMonitoring
import de.frederikkohler.shared.configure.configureSecurity
import de.frederikkohler.shared.configure.connectToPostgres
import de.frederikkohler.utils.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSwagger()
    configureSerialization()
    configureMonitoring()
    configureSecurity()

    val database = connectToPostgres()
    configureAuthenticationRouting(database)
}