package de.coreMobile.server

import de.coreMobile.server.model.firebaseMessage.request.Notification
import de.coreMobile.server.repository.FirebaseRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.publicRoutes
import de.frederikkohler.shared.utils.sdkRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.ok
import io.github.smiley4.ktoropenapi.post
import io.ktor.server.application.Application
import io.ktor.server.routing.get

fun Application.configureSDKRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("SDK")
) {
    val firebaseRepository = FirebaseRepository()

    publicRoutes {
        get("sdk/getAccessToken") {
            val token = firebaseRepository.getAccessToken()
            call.ok(token ?: "No Token")
        }
        get("sdk/serviceAccount") {
            val token = firebaseRepository.loadServiceAccount()
            call.ok(token ?: listOf<String>())
        }
    }

    sdkRoutes {
        post("sdk/sendNotification", {
            request {
                queryParameter<String>("deviceToken") {
                    description = "Device token"
                }
                queryParameter<String>("title") {
                    description = "Notification Title"
                }
                queryParameter<String>("body") {
                    description = "Notification body"
                }
            }
        }) {
            val title = call.request.queryParameters["title"]
            val body = call.request.queryParameters["body"]
            val deviceToken = call.request.queryParameters["deviceToken"]

            if(title.isNullOrBlank()) return@post call.badRequest()
            if(body.isNullOrBlank()) return@post call.badRequest()
            if(deviceToken.isNullOrBlank()) return@post call.badRequest()

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