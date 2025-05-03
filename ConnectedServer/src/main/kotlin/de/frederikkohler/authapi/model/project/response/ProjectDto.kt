package de.frederikkohler.authapi.model.project.response

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val userId: String,
    val token: String? = null
)