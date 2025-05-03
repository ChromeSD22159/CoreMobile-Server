package de.frederikkohler.authapi.model

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateDto(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
)