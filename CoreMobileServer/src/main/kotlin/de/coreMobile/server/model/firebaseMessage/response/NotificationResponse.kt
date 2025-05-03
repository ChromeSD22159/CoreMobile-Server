package de.coreMobile.server.model.firebaseMessage.response

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val name: String
)