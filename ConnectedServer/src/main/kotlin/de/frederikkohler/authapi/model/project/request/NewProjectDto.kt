package de.frederikkohler.authapi.model.project.request

import kotlinx.serialization.Serializable

@Serializable
data class NewProjectDto(
    val name: String, 
    val token: String? = null
)