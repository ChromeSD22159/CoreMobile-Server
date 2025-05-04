package de.coreMobile.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.coreMobile.server.model.databaseService.TablesConfig
import de.frederikkohler.shared.model.AuthRequired
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.descriptors.type
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder

fun Application.configureSwagger(
    generatedSchema: Map<String, Any>,
    generatedTables:  Map<String, Table>
) {
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
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApi()
        }

        route("model") {
            swaggerUI("http://0.0.0.0:8080/openapi")
        }
        get("/openapi") {
            val mapper = jacksonObjectMapper()
            val openApiJson: String = mapper.writeValueAsString(generatedSchema)

            openApi()

            call.respondText(
                openApiJson,
                contentType = ContentType.Application.Json
            )
        }
    }
}