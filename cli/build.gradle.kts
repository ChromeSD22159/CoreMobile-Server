plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

application {
    mainClass = "de.coreMobile.cli.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("com.github.ajalt.clikt:clikt-markdown:5.0.3")
}
tasks {
    shadowJar {
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass.get()))
        }
    }
}