package de.frederikkohler.authapi

import kotlinx.serialization.Serializable

@Serializable
data class SessionTokensDTO(
    val shortLivedRefreshToken: String
)