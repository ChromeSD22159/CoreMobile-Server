package de.frederikkohler.authapi

import de.frederikkohler.shared.model.AuthRequired
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.url

fun Application.configureSwagger() {
    install(OpenApi) {
        info {
            version = "1.0.0"
            title = "CoreMobile Connected Server"
            description = "RestApi for Mananging the CoreMobile Connected Server"
            termsOfService = "https://www.frederikkohler.de/datenschutz"
            contact {
                name = "Frederik Kohler"
                url = "https://www.frederikkohler.de"
                email = "info@frederikkohler.de"
            }
        }
        /*
        server {
            url = "http://localhost"
            description = "local dev-server"
            variable("version") {
                default = "1.0"
                enum = setOf("1.0")
                description = "the version of the server api"
            }
        }
        server {
            url = "https://frederikkohler.de"
            description = "productive server"
            variable("version") {
                default = "1.0"
                enum = setOf("1.0")
                description = "the version of the server api"
            }
        }
         */
        security {
            securityScheme("MySecurityScheme") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
            }
            defaultSecuritySchemeNames("MySecurityScheme")
            defaultUnauthorizedResponse {
                description = "AccessToken is invalid"
                body<AuthRequired>()
            }
        }
    }

    routing {
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApi()
        }
    }
}