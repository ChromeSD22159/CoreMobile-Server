package de.frederikkohler.postsapi

import de.frederikkohler.shared.configure.configureMonitoring
import de.frederikkohler.shared.configure.configureSecurity
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
    configurePostsRouting()
}