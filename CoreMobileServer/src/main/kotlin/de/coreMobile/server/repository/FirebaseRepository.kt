package de.coreMobile.server.repository

import com.google.auth.oauth2.GoogleCredentials
import de.coreMobile.server.model.firebaseMessage.FirebaseConfig
import de.coreMobile.server.model.firebaseMessage.request.Notification
import de.coreMobile.server.model.firebaseMessage.response.NotificationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.FileInputStream

class FirebaseRepository {
    private val serviceAccountFile = "/app/CoreMobileServer/serviceAccountKey.json"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getAccessToken(): String? {
        return try {
            val scopes = listOf("https://www.googleapis.com/auth/firebase.messaging")
            val googleCredentials =
                withContext(Dispatchers.IO) {
                    GoogleCredentials
                        .fromStream(FileInputStream(serviceAccountFile))
                }
                .createScoped(scopes)
            googleCredentials.refresh()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            println("Error getting access token: ${e.message}")
            return null
        }
    }

    suspend fun sendMessage(
        deviceToken: String,
        request: Notification
    ): Result<NotificationResponse> {
        return try {
            val serviceAccount = loadServiceAccount()
            val apiToken = getAccessToken()

            if(deviceToken.isBlank()) throw Exception("No device token provided")
            if(request.body.isBlank()) throw Exception("No token given")
            if(apiToken.isNullOrBlank()) throw Exception("No API access token retrieved")
            if(serviceAccount == null) throw Exception("No firebase project id found")

            val payload = mapOf(
                "message" to mapOf(
                    "token" to deviceToken,
                    "notification" to mapOf(
                        "title" to request.title,
                        "body" to request.body
                    )
                )
            )

            val response: NotificationResponse = client.post("https://fcm.googleapis.com/v1/projects/${serviceAccount.projectId}/messages:send") {
                contentType(ContentType.Application.Json)
                bearerAuth(apiToken)
                setBody(payload)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure<NotificationResponse>(e)
        }
    }

    fun loadServiceAccount(): FirebaseConfig? {
        return try {
            Json.decodeFromString<FirebaseConfig>(FileInputStream(serviceAccountFile).bufferedReader().use { it.readText() })
        } catch (e: Exception) { null }
    }
}