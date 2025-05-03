package de.coreMobile.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.frederikkohler.shared.model.AuthRequired
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureSwagger(generatedSchema: Map<String, Any>) {
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
            swaggerUI("http://0.0.0.0:1337/openapi")
        }

        get("/openapi") {
            val mapper = jacksonObjectMapper()
            val openApiJson: String = mapper.writeValueAsString(generatedSchema)
            call.respondText(
                openApiJson,
                contentType = ContentType.Application.Json
            )
        }
    }
}