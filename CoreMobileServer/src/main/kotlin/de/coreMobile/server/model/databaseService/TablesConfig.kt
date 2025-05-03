package de.coreMobile.server.model.databaseService

import kotlinx.serialization.Serializable

@Serializable
data class TablesConfig(
    val tables: Map<String, TableConfig>
)