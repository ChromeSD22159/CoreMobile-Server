package de.frederikkohler.shared.configure

import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.RouteTypes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer

fun Application.configureSecurity(
    jwtService: JwtService
) {

    install(Authentication) {
        bearer(RouteTypes.ACCESSTOKEN.displayName) {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyAccessTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
        bearer(RouteTypes.AUTHENTICATED.displayName) {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyRefreshTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
        bearer(RouteTypes.SDK.displayName) {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyProjectRefreshTokenAndGetProjectId(tokenCredential.token)?.let { projectId ->
                    tokenCredential.token
                }
            }
        }
    }
}
