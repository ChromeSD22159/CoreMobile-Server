package de.coreMobile.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

/**
 * Mit Logging (Standard)
 * ```bash
 * ./gradlew cli:run --args="start"
 * java -jar cli-all.jar start
 * ```
 *
 * Ohne Logging
 * ```bash
 * ./gradlew cli:run --args="start --no-log"
 * java -jar cli-all.jar start --no-log
 * ```
 *
 * Mit Detach und Logging
 * ```bash
 * ./gradlew cli:run --args="start -d"
 * java -jar cli-all.jar start -d
 * ```
 *
 * Mit Detach und ohne Logging
 * ```bash
 * ./gradlew cli:run --args="start -d --no-log"
 * java -jar cli-all.jar start -d --no-log
 * ```
 */
class RunCommand : CliktCommand(name = "start") {
    private val detach by option("-d", "--detach", help = "Starte die Container im Hintergrund.").flag()
    private val noLog by option("--no-log", help = "Deaktiviere die Ausgabe der Container-Logs in der Konsole.").flag()

    override fun run() {
        try {

            val commandList = mutableListOf("docker", "compose", "up")
            if (detach) {
                commandList.add("-d")
            }

            val processBuilder = ProcessBuilder(commandList)
                .directory(File("."))

            if (!noLog) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
            }

            val process = processBuilder.start()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                println("Docker Compose wurde erfolgreich gestartet.")
            } else {
                println("Fehler beim Starten von Docker Compose. Exit-Code: $exitCode")
            }
        } catch (e: Exception) {
            println("Eine Ausnahme ist beim Ausführen von Docker Compose aufgetreten: ${e.localizedMessage}")
        }
    }
}