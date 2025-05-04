package de.frederikkohler.authapi.routes

import de.frederikkohler.authapi.SessionTokensDTO
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.sdkRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application

fun Application.configureSDKRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("SDK")
) {

    sdkRoutes {
        get("sdk/tokenByToken", {
            tags = listOf("Authentication", "SDK")
            protected = true
            description = """ 
                Generates an new ProjectRefresh token for a valid ProjectAccessToken
                
                **Authorization Header:** `Bearer <ProjectAccessToken>`
            """.trimIndent()
            response {
                code(HttpStatusCode.OK) {
                    body<SessionTokensDTO> {
                        description = "refreshToken"
                    }
                }
            }
        }) {
            val token = call.getAuthToken() ?: throw Exception("No token given")

            val refreshTokenOrNull = jwtService.verifyProjectAccessTokenAndGenerateRefreshToken(token)

            if (refreshTokenOrNull != null) {
                val response = SessionTokensDTO(refreshTokenOrNull)
                call.ok(response)
            } else {
                call.badRequest("Invalid token")
            }
        }
    }
}


