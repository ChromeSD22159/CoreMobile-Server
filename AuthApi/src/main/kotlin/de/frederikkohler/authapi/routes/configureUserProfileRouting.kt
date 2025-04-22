package de.frederikkohler.authapi.routes

import de.frederikkohler.authapi.model.UserProfileDTO
import de.frederikkohler.authapi.repository.UserProfileRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.authenticatedRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.ok
import de.frederikkohler.utils.unauthorized
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application

fun Application.configureUserProfileRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("UserProfile")
) {
    val userProfileRepository = UserProfileRepository(jwtService)

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
                        call.badRequest()
                    }
                } else {
                    call.badRequest()
                }
            } else {
                call.unauthorized()
            }
        }
    }
}