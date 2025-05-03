package de.coreMobile.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.io.File

/**
 * ./gradlew cli:run --args="stop"
 * java -jar cli-all.jar stop
 */
class StopCommand : CliktCommand(name = "stop") {
    override fun run() {
        try {
            val process = ProcessBuilder("docker", "compose", "down")
                .directory(File(".")) // Optional: Setze das Arbeitsverzeichnis, falls deine docker-compose.yml nicht im aktuellen Verzeichnis liegt
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                println("Docker Compose wurde erfolgreich heruntergefahren.")
            } else {
                println("Fehler beim Herunterfahren von Docker Compose. Exit-Code: $exitCode")
            }

        } catch (e: Exception) {
            println("Eine Ausnahme ist beim Ausführen von Docker Compose aufgetreten: ${e.localizedMessage}")
        }
    }
}