package de.frederikkohler.postsapi

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
            title = "Post API"
            description = "Post API"
        }
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
            swaggerUI("/swagger") {
                openApi()
            }
        }
    }
}