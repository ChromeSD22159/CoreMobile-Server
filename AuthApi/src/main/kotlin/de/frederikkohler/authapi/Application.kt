package de.frederikkohler.authapi

import de.frederikkohler.authapi.routes.configureAuthenticationRouting
import de.frederikkohler.authapi.routes.configureUserProfileRouting
import de.frederikkohler.shared.configure.configureMonitoring
import de.frederikkohler.shared.configure.configureSecurity
import de.frederikkohler.shared.configure.connectToPostgres
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.utils.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jwtService = JwtService(environment.config)

    configureSwagger()
    configureSerialization()
    configureMonitoring()
    configureSecurity(jwtService)
    connectToPostgres()
    configureUserProfileRouting(jwtService)
    configureAuthenticationRouting(jwtService)
}