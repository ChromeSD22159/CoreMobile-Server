package de.frederikkohler.authapi.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val password: String
)

fun String.toUUID() = UUID.fromString(this)