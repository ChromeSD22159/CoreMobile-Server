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
    val userProfileRepository = UserProfileRepository()

    publicRoutes {
        post("/signIn", {
            tags = tag
            description = """
                User signIn 
            """.trimIndent()
            request {
                queryParameter<String>("email") {
                    description = "The user's e-mail address"
                    required = true
                }
                queryParameter<String>("password") {
                    description = "The password"
                    required = true
                }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<UserLoginDTO> {
                        description = "Returns the user, access token and refresh token"
                    }
                }
            }
        }) {
            val email = call.parameters["email"] ?: return@post call.badRequest("Missing email")
            val password = call.parameters["password"] ?: return@post call.badRequest("Missing password")

            val userDTO = authRepository.login(email, password)
            if (userDTO != null) {
                call.respond(HttpStatusCode.OK, userDTO)
            } else {
                call.badRequest("Invalid email or password")
            }
        }

        get("/users", {
            tags = tag
            description = """
                List all registered Users (E-Mail)
            """.trimIndent()
            response {
                code(HttpStatusCode.OK) {
                    body<List<String>> {
                        description = "Returns a list of all users"
                    }
                }
            }
        }) {
            val users: List<String> = authRepository.allUsers()
            call.respond(HttpStatusCode.OK, users)
        }

        post("/signUp", {
            tags = tag
            description = """
                SignUp for new User
            """.trimIndent()
            request {
                queryParameter<String>("email") {
                    description = "The user's e-mail address"
                    required = true
                }
                queryParameter<String>("password") {
                    description = "The password"
                    required = true
                }
                queryParameter<String>("firstName") {
                    description = "Firstname"
                    required = true
                }
                queryParameter<String>("lastName") {
                    description = "Lastname"
                    required = true
                }
                queryParameter<String>("birthDate") {
                    description = "Your date of birth (dd.mm.yyyy)"
                    required = true
                }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<SessionTokensDTO> {
                        description = "The user has been successfully created"
                    }
                }
            }
        }) {
            val email = call.parameters["email"] ?: return@post call.badRequest("Missing email")
            val password = call.parameters["password"] ?: return@post call.badRequest("Missing password")
            val firstName = call.parameters["firstName"] ?: return@post call.badRequest("Missing firstName")
            val lastName = call.parameters["lastName"] ?: return@post call.badRequest("Missing lastName")
            val birthDate = call.parameters["birthDate"] ?: return@post call.badRequest("Missing birthDate")

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
            description = """
                Generates an new refresh token for a valid UserAccessToken
                
                **Authorization Header:** `Bearer <UserAccessToken>`
            """.trimIndent()
            response {
                code(HttpStatusCode.OK) {
                    body<SessionTokensDTO> {
                        description = "Returns a new refresh token"
                    }
                }
                code(HttpStatusCode.BadRequest) {
                    description = "Missing or invalid Authorization Header"
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Invalid or expired access token"
                }
                code(HttpStatusCode.Forbidden) {
                    description = "Not authorized to create refresh tokens"
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
