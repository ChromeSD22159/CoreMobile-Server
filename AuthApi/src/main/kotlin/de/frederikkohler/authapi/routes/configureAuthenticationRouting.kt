package de.frederikkohler.authapi.routes

import de.frederikkohler.authapi.SessionTokensDTO
import de.frederikkohler.authapi.model.UserLoginDTO
import de.frederikkohler.authapi.repository.AuthRepository
import de.frederikkohler.authapi.repository.UserProfileRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.accessTokenAccessedRoutes
import de.frederikkohler.shared.utils.publicRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.noContent
import de.frederikkohler.utils.ok
import de.frederikkohler.utils.unauthorized
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond

fun Application.configureAuthenticationRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("Authentication")
) {
    val authRepository = AuthRepository(jwtService)
    val userProfileRepository = UserProfileRepository(jwtService)

    publicRoutes {
        post("/signin", {
            tags = tag
            description = "Loggt den Benutzer ein"
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
                    body<UserLoginDTO> {
                        description = "Gibt den User, Access Token und Refresh Token zurück"
                    }
                }
            }
        }) {
            val email = call.parameters["email"] ?: return@post call.badRequest()
            val password = call.parameters["password"] ?: return@post call.badRequest()

            val userDTO = authRepository.login(email, password)
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
            val users: List<String> = authRepository.allUsers()
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
                queryParameter<String>("firstName") {
                    description = "Vorname"
                    required = true
                }
                queryParameter<String>("lastName") {
                    description = "Nachname"
                    required = true
                }
                queryParameter<String>("birthDate") {
                    description = "Dein Geburtsdatum (iso string)"
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
            val firstName = call.parameters["firstName"] ?: return@post call.badRequest()
            val lastName = call.parameters["lastName"] ?: return@post call.badRequest()
            val birthDate = call.parameters["birthDate"] ?: return@post call.badRequest()

            val user = authRepository.createUser(email, password)

            if (user != null) {
                val loginDto = authRepository.login(email, password)
                if (loginDto != null) {

                    userProfileRepository.createUserProfile(
                        loginDto.userDto.id,
                        firstName,
                        lastName,
                        birthDate
                    )

                    call.ok(loginDto)
                } else call.noContent()
            } else call.noContent()
        }
    }

    accessTokenAccessedRoutes {
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
            val principal = call.getAuthToken()
            if (principal != null) {
                val refreshTokenDTO = authRepository.generateSessionTokens(principal)
                if (refreshTokenDTO != null) {
                    call.ok(refreshTokenDTO)
                } else {
                    call.unauthorized()
                }
            } else {
                call.unauthorized()
            }
        }
    }
}





