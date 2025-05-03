import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "de.frederikkohler"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Shared"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.ktor.server.call.logging)
    implementation("io.github.smiley4:ktor-swagger-ui:5.0.2")
    implementation("io.github.smiley4:ktor-openapi:5.0.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.+")
    implementation("io.ktor:ktor-server-auth-jwt:3.1.2")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdhc)
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.60.0")
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("io.ktor:ktor-client-cio:3.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("com.charleskorn.kaml:kaml:0.77.1")

    // Google API Client Library für OAuth 2.0
    implementation("com.google.api-client:google-api-client:2.4.0") // Überprüfe die aktuelle Version
    implementation("com.google.oauth-client:google-oauth-client-java6:1.34.1") // Überprüfe die aktuelle Version
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1") // Optional, aber oft für I/O-Operationen benötigt

    // Google Auth Library (die modernere Alternative, die im vorherigen Beispiel verwendet wurde)
    implementation("com.google.auth:google-auth-library-oauth2-http:1.24.1") // Überprüfe die aktuelle Version
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
}