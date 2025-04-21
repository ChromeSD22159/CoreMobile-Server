package de.frederikkohler.authapi.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDTO(
    val user: User,
    val accessToken: String,
    val shortLivedRefreshToken: String
)