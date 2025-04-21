package de.frederikkohler.shared.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class JwtService(private val config: ApplicationConfig) {

    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val accessTokenExpiration = config.property("jwt.accessTokenExpiration").getString().toLong()
    private val refreshTokenExpiration = config.property("jwt.refreshTokenExpiration").getString().toLong()

    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
            .sign(algorithm)
    }

    fun verifyAccessTokenAndGetUserId(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
            decodedJWT.getClaim("userId").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun verifyRefreshTokenAndGetUserId(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
            decodedJWT.getClaim("userId").asString()
        } catch (e: Exception) {
            null
        }
    }
}