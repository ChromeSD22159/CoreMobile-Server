package de.coreMobile.server

import de.coreMobile.server.genericRoutes.genericRoutes
import de.coreMobile.server.libs.ConfigService
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
    val configService = ConfigService(environment)
    val databaseService = DatabaseService(environment, configService.yamlTables, configService.yamlConfig)

    configureSwagger(databaseService.swaggerSchemas, databaseService.generatedTables)
    genericRoutes(databaseService.generatedTables)
    configureSerialization()
    configureMonitoring()
    configureSecurity(jwtService)
    configureSDKRouting(jwtService, databaseService)
}


