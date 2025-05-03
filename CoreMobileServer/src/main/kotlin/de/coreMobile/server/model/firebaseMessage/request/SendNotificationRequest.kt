package de.coreMobile.server.model.firebaseMessage.request

import kotlinx.serialization.Serializable

@Serializable
data class SendNotificationRequest(
    val body: SendNotificationMessage,
)
@Serializable
data class SendNotificationMessage(
    val token: String,
    val notification: Notification,
)
@Serializable
data class Notification(
    val title: String,
    val body: String,
)