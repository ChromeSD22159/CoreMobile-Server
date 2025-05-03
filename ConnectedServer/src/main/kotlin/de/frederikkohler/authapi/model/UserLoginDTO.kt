package de.frederikkohler.authapi.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDTO(
    val userDto: UserDto,
    val accessToken: String,
    val shortLivedRefreshToken: String
)