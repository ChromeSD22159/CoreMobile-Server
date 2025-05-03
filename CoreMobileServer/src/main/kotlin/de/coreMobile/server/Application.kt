package de.coreMobile.server

import de.coreMobile.server.libs.DatabaseService
import de.frederikkohler.shared.configure.configureMonitoring
import de.frederikkohler.shared.configure.configureSecurity
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.utils.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jwtService = JwtService()
    val databaseService = DatabaseService()
    databaseService.init()

    configureSwagger(databaseService.swaggerSchemas)
    configureSerialization()
    configureMonitoring()
    configureSecurity(jwtService)
    configureSDKRouting(jwtService)
}