package de.frederikkohler.shared.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.frederikkohler.shared.SharedConfig
import io.ktor.server.config.ApplicationConfig
import java.util.Date

class JwtService() {

    private val secret = SharedConfig.JWT_SECRET // config.property("jwt.secret").getString()
    private val issuer = SharedConfig.JWT_ISSUER // config.property("jwt.issuer").getString()
    private val audience = SharedConfig.JWT_AUDIENCE  //config.property("jwt.audience").getString()
    private val accessTokenExpiration = SharedConfig.JWT_ACCESS_TOKEN_EXPIRATION // config.property("jwt.accessTokenExpiration").getString().toLong()
    private val refreshTokenExpiration = SharedConfig.JWT_REFRESH_TOKEN_EXPIRATION //config.property("jwt.refreshTokenExpiration").getString().toLong()
    private val projectToken = SharedConfig.JWT_PROJECT_TOKEN //config.property("jwt.projectToken").getString()

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
            .withClaim("access", "all")
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

            val userId = decodedJWT.getClaim("userId")?.asString()
            val accessClaim = decodedJWT.getClaim("access")?.asString()

            if (accessClaim.isNullOrBlank() && !userId.isNullOrBlank()) {
                userId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun verifyAccessTokenAndGenerateRefreshToken(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val userId = decodedJWT.getClaim("userId")?.asString()
            val accessClaim = decodedJWT.getClaim("access")?.asString()

            if (!accessClaim.isNullOrBlank() && accessClaim == "all" && !userId.isNullOrBlank()) {
                generateRefreshToken(userId)
            } else {
                null
            }
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

            val userId = decodedJWT.getClaim("userId")?.asString()
            val accessClaim = decodedJWT.getClaim("access")?.asString()

            if (!accessClaim.isNullOrBlank() && accessClaim == "all" && !userId.isNullOrBlank()) {
                userId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }



    // PROJECT
    fun generateProjectAccessToken(projectId: String, userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("projectId", projectId)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + projectToken * 1000L))
            .sign(algorithm)
    }

    fun generateProjectRefreshToken(projectId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("projectId", projectId)
            .withClaim("access", "sdk")
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
            .sign(algorithm)
    }

    fun verifyProjectAccessTokenAndGetUserId(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val projectId = decodedJWT.getClaim("projectId")?.asString()
            val userId = decodedJWT.getClaim("userId")?.asString()
            val accessClaim = decodedJWT.getClaim("access")?.asString()

            if (!accessClaim.isNullOrBlank() && accessClaim == "sdk" && !projectId.isNullOrBlank() && !userId.isNullOrBlank()) {
                userId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun verifyProjectAccessTokenAndGetProjectId(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val projectId = decodedJWT.getClaim("projectId")?.asString()
            val userId = decodedJWT.getClaim("userId")?.asString()

            if (!projectId.isNullOrBlank() && !userId.isNullOrBlank()) {
                projectId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun verifyProjectAccessTokenAndGenerateRefreshToken(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val projectId = decodedJWT.getClaim("projectId")?.asString()

            if (!projectId.isNullOrBlank()) {
                generateProjectRefreshToken(projectId)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun verifyProjectRefreshTokenAndGetProjectId(token: String): String? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val userId = decodedJWT.getClaim("projectId")?.asString()
            val accessClaim = decodedJWT.getClaim("access")?.asString()

            if (!accessClaim.isNullOrBlank() && accessClaim == "sdk" && !userId.isNullOrBlank()) {
                userId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}