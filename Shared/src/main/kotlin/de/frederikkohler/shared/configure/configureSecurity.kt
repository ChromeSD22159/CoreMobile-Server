package de.frederikkohler.shared.configure

import de.frederikkohler.shared.services.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer

fun Application.configureSecurity(
    jwtService: JwtService
) {
    install(Authentication) {
        bearer("accessToken") {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyAccessTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
        bearer("authenticated") {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                jwtService.verifyRefreshTokenAndGetUserId(tokenCredential.token)?.let { userId ->
                    tokenCredential.token
                }
            }
        }
    }
}
