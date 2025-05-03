package de.coreMobile.server.model.firebaseMessage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
    data class FirebaseConfig(
    val type: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("private_key_id") val privateKeyId: String,
    @SerialName("private_key") val privateKey: String,
    @SerialName("client_email") val clientEmail: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("auth_uri") val authUri: String,
    @SerialName("token_uri") val tokenUri: String,
    @SerialName("auth_provider_x509_cert_url") val authProviderX509CertUrl: String,
    @SerialName("client_x509_cert_url") val clientX509CertUrl: String,
    @SerialName("universe_domain") val universeDomain: String,
    )