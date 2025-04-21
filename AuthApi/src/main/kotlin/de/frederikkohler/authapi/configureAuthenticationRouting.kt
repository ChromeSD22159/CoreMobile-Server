package de.frederikkohler.authapi

import de.frederikkohler.authapi.model.UserLoginDTO
import de.frederikkohler.authapi.repository.AuthRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.noContent
import de.frederikkohler.utils.ok
import de.frederikkohler.utils.unauthorized
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.configureAuthenticationRouting(database: Database) {
    val tag = listOf("Authentication")
    val config = environment.config
    val jwtService = JwtService(config)
    val repository = AuthRepository(database, jwtService)

    install(Authentication) {
        bearer("accessToken") {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyAccessTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
    }

    routing {
        post("/signin", {
            tags = tag
            description = "Loggt den Benutzer ein"
            response {
                code(HttpStatusCode.Created) {
                    body<UserLoginDTO> {
                        description = "Gibt den User, Access Token und Refresh Token zurück"
                    }
                }
            }
        }) {
            val email = call.parameters["email"] ?: return@post call.badRequest()
            val password = call.parameters["password"] ?: return@post call.badRequest()

            val userDTO = repository.login(email, password)
            if (userDTO != null) {
                call.respond(HttpStatusCode.OK, userDTO)
            } else {
                call.badRequest()
            }
        }

        get("/users", {
            tags = tag
            response {
                code(HttpStatusCode.OK) {
                    body<List<String>> {
                        description = "Gibt eine Liste aller Benutzer zurück"
                    }
                }
            }
        }) {
            val users: List<String> = repository.allUsers()
            call.respond(HttpStatusCode.OK, users)
        }

        post("/signup", {
            tags = tag
            description = "erstellt einen neuen Benutzer"
            request {
                queryParameter<String>("email") {
                    description = "Die E-Mail-Adresse des Benutzers"
                    required = true
                }
                queryParameter<String>("password") {
                    description = "Das Passwort"
                    required = true
                }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<SessionTokensDTO> {
                        description = "Der Benutzer wurde erfolgreich erstellt"
                    }
                }
            }
        }) {
            val email = call.parameters["email"] ?: return@post call.badRequest()
            val password = call.parameters["password"] ?: return@post call.badRequest()

            val user = repository.createUser(email, password)

            if (user != null) {
                val loginDto = repository.login(email, password)
                if (loginDto != null) {
                    call.ok(loginDto)
                } else call.noContent()
            } else call.noContent()
        }

        authenticate("accessToken") {
            post("/tokenByToken", {
                protected = true
                tags = tag
                description = "Erstellt einen neuen Refresh Token mit einem gültigen Access Token"
                response {
                    code(HttpStatusCode.OK) {
                        body<SessionTokensDTO> {
                            description = "Gibt einen neuen Refresh Token zurück"
                        }
                    }
                    code(HttpStatusCode.BadRequest) {
                        description = "Fehlender oder ungültiger Authorization Header"
                    }
                    code(HttpStatusCode.Unauthorized) {
                        description = "Ungültiges oder abgelaufenes Access Token"
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Nicht autorisiert, Refresh Token zu erstellen"
                    }
                }
            }) {
                val principal = call.principal<String>()
                if (principal != null) {
                    val refreshTokenDTO = repository.generateSessionTokens(principal)
                    if (refreshTokenDTO != null) {
                        call.ok(refreshTokenDTO)
                    } else {
                        call.unauthorized()
                    }
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
    }
}