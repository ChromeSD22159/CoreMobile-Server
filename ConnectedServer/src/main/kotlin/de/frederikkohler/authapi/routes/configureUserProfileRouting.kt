package de.frederikkohler.authapi.routes

import de.frederikkohler.authapi.model.UserProfileDTO
import de.frederikkohler.authapi.model.UserUpdateDto
import de.frederikkohler.authapi.repository.UserProfileRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.authenticatedRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.ok
import de.frederikkohler.utils.unauthorized
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive

fun Application.configureUserProfileRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("UserProfile")
) {
    val userProfileRepository = UserProfileRepository()

    authenticatedRoutes {
        get("/me", {
            protected = true
            tags = tag
            response {
                code(HttpStatusCode.OK) {
                    body<UserProfileDTO> {
                        description = "Gibt den UserProfile zurück"
                    }
                }
                code(HttpStatusCode.NoContent) {
                    description = "Der Benutzer wurde nicht gefunden"
                }
                code(HttpStatusCode.BadRequest) {
                    description = "Fehlender oder ungültiger Authorization Header"
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Ungültiges oder abgelaufenes Access Token"
                }
            }
        }) {
            val token = call.getAuthToken()
            if (token != null) {
                val userId = jwtService.verifyRefreshTokenAndGetUserId(token)
                if (userId != null) {
                    val userDto = userProfileRepository.getUserProfileByUserId(userId)
                    if (userDto != null) {
                        call.ok(userDto)
                    } else {
                        call.badRequest("User not found")
                    }
                } else {
                    call.badRequest("Invalid token")
                }
            } else {
                call.unauthorized()
            }
        }

        put("user/me/{userId}", {
            protected = true
            tags = tag
            request {
                pathParameter<String>("userId") {
                    description = "Die ID des Benutzers"
                }
                body<UserUpdateDto> {
                    description = "request body"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<UserProfileDTO> {
                        description = "Gibt den UserProfile zurück"
                    }
                }
                code(HttpStatusCode.NoContent) {
                    description = "Der Benutzer wurde nicht gefunden"
                }
                code(HttpStatusCode.BadRequest) {
                    description = "Fehlender oder ungültiger Authorization Header"
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Ungültiges oder abgelaufenes Access Token"
                }
            }
        }) {
            try {
                val userId = call.parameters["userId"] ?: throw Exception("No UserId given")
                val receivedUserProfile = call.receive<UserUpdateDto>()
                userProfileRepository.updateUserProfile(userId, receivedUserProfile)?.let {
                    call.ok(it)
                }
            } catch (e: Exception) {
                call.badRequest(e.message)
            }
        }

        delete("/me/{userId}", {
            protected = true
            tags = tag
            request {
                pathParameter<String>("userId") {
                    description = "Die ID des Benutzers"
                }
            }
        }) {
            val userId = call.parameters["userId"] ?: return@delete call.badRequest("No UserId given")
            if (userProfileRepository.deleteUserProfile(userId)) {
                call.ok(true)
            } else {
                call.ok(false)
            }
        }
    }
}