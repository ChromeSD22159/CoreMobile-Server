package de.coreMobile.server.model.databaseService

import kotlinx.serialization.Serializable

@Serializable
data class TableConfig(
    val columns: Map<String, String>
)