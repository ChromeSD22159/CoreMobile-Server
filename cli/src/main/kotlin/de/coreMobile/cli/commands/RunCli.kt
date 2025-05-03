package de.coreMobile.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

/**
 * ./gradlew cli:run
 * java -jar cli-all.jar
 */
class RunCli : CliktCommand() {
    override fun run() = Unit
}