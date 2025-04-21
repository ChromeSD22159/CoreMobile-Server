package de.frederikkohler.postsapi

import de.frederikkohler.shared.services.JwtService
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.configurePostsRouting() {
    val tag = listOf("Test")
    val config = environment.config
    val jwtService = JwtService(config)

    install(Authentication) {
        bearer("authenticated") {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyAccessTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
    }

    routing {
        authenticate("authenticated") {
            get("/test", {
                tags = tag
                description = "Test"
            }) {
                call.respond(HttpStatusCode.OK, "Hello world?")
            }
        }
    }
}