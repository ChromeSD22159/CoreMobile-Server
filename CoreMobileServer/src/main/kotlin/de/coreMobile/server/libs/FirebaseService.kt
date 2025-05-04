package de.coreMobile.server.libs

import com.google.auth.oauth2.GoogleCredentials
import de.coreMobile.server.model.firebaseMessage.FirebaseConfig
import de.coreMobile.server.model.firebaseMessage.request.Notification
import de.coreMobile.server.model.firebaseMessage.response.NotificationResponse
import de.coreMobile.server.utils.loadMountedJson
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
import java.io.FileInputStream

class FirebaseService {
    private val logging = Logging("FirebaseService")
    var serviceAccount: FirebaseConfig? = null

    init {
        loadServiceAccount()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getAccessToken(): String? {
        try {
            val scopes = listOf("https://www.googleapis.com/auth/firebase.messaging")
            val googleCredentials =
                withContext(Dispatchers.IO) {
                    GoogleCredentials
                        .fromStream(FileInputStream("/app/CoreMobileServer/CustomerFiles/serviceAccountKey.json"))
                }
                .createScoped(scopes)
            googleCredentials.refresh()


            logging.info("Error getting access token: ${googleCredentials.accessToken.tokenValue}")

            return googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            logging.error("Error getting access token: ${e.message}")
            return null
        }
    }

    suspend fun sendMessage(
        deviceToken: String,
        request: Notification
    ): Result<NotificationResponse> {
        return try {
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

            if(serviceAccount != null) {
                val response: NotificationResponse = client.post("https://fcm.googleapis.com/v1/projects/${serviceAccount!!.projectId}/messages:send") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(apiToken)
                    setBody(payload)
                }.body()

                Result.success(response)
            } else {
                throw Exception("No firebase project id found")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadServiceAccount() {
        try {
            serviceAccount = loadMountedJson("serviceAccountKey.json")
        } catch (e: Exception) {
            logging.error("loadTablesConfigYaml: ${e.message.toString()}")
        }
    }
}