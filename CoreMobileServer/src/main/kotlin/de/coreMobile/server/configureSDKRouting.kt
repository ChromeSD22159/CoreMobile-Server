package de.coreMobile.server

import de.coreMobile.server.libs.DatabaseService
import de.coreMobile.server.model.firebaseMessage.request.Notification
import de.coreMobile.server.libs.FirebaseService
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.publicRoutes
import de.frederikkohler.shared.utils.sdkRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondBytes

fun Application.configureSDKRouting(
    jwtService: JwtService,
    databaseService: DatabaseService,
    tag: List<String> = listOf("SDK")
) {
    val firebaseRepository = FirebaseService()

    publicRoutes {
        get("sdk/getAccessToken", {
            tags = tag
            description = "Get Firebase Access Token"
            response {
                code(HttpStatusCode.OK) {
                    description = "Calculation was performed successfully."
                    body<String> {
                        description = "the result of an operation together with the original request"
                    }
                }
            }
        }) {
            try {
                val token = firebaseRepository.getAccessToken()
                call.ok(token ?: throw Exception("No Token"))
            } catch (e: Exception) {
                call.badRequest(e.message ?: "Unknown Error")
            }
        }

        get("sdk/serviceAccount", {
            tags = tag
            description = "Get Firebase Project iD"
            response {
                code(HttpStatusCode.OK) {
                    description = "Project iD successfully."
                    // specify the schema of the response body and some additional information
                    body<String> {
                        description = "the result of an operation together with the original request"
                    }
                }
            }
        }) {
            try {
                val token = firebaseRepository.serviceAccount
                call.ok(token?.projectId ?: throw Exception("No Token"))
            } catch (e: Exception) {
                call.badRequest(e.message ?: "Unknown Error")
            }
        }

        get("database/export") {
            val dbByteArray = databaseService.exportDatabaseALL()
            call.response.headers.append(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    "database_export.zip"
                ).toString()
            )
            call.respondBytes(dbByteArray, io.ktor.http.ContentType.Application.Zip)
        }
    }

    sdkRoutes {
        post("sdk/sendNotification", {
            tags = tag
            description = "Send a Firebase Notification"
            protected = true
            request {
                queryParameter<String>("title") {
                    description = "Title of the notification"
                }
                queryParameter<String>("body") {
                    description = "Body of the notification"
                }

            }
        }) {
            val title = call.request.queryParameters["title"]
            val body = call.request.queryParameters["body"]
            val deviceToken = call.getAuthToken()

            if(title.isNullOrBlank()) return@post  call.badRequest("title is blank")
            if(body.isNullOrBlank()) return@post  call.badRequest("body is blank")
            if(deviceToken.isNullOrBlank()) return@post  call.badRequest("deviceToken is blank")

            firebaseRepository.sendMessage(
                deviceToken = deviceToken,
                request = Notification(
                    title = title,
                    body = body
                )
            )
        }
    }
}