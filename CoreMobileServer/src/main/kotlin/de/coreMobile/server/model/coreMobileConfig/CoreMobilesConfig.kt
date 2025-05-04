package de.coreMobile.server.model.coreMobileConfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoreMobilesConfig(
    @SerialName("version") val version: Double,
    @SerialName("author") val author: String? = null,
    @SerialName("external-db") val externalDb: ExternalDatabaseConfig? = null
)

@Serializable
data class ExternalDatabaseConfig(
    val url: String? = null,
    val user: String? = null,
    val password: String? = null
)

@Serializable
data class DatabaseConnection(
    val url: String,
    val user: String,
    val password: String
)