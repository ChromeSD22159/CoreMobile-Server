package de.coreMobile.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

/**
 * ./gradlew cli:run --args="init --name TestProject"
 * java -jar cli-all.jar init --name TestProject
 */
class InitCommand : CliktCommand(name = "init") {
    private val name by option("-n", "--name", help = "Name deines neuen Projektes.")

    override fun run() {

        if (name.isNullOrBlank()) {
            println("Bitte geben Sie einen gültigen Projektnamen ein.")
        } else {
            val projectDir = File(name!!)

            if (projectDir.exists()) {
                println("Das Verzeichnis '$name' wird überschrieben.")
                projectDir.deleteRecursively()
                buildFiles(projectDir)
            } else {
                buildFiles(projectDir)
            }
        }
    }

    private fun buildFiles(projectDir: File) {
        val dockerComposeContent = """
            version: '3.8'
            services:
              core-mobile-server: 
                image: chromesd22159/core-mobile-server
                ports:
                  - "8080:8080"
                volumes: 
                  - ./tables.yaml:/app/CoreMobileServer/tables.yaml:ro
                  - ./serviceAccountKey.json:/app/CoreMobileServer/serviceAccountKey.json:ro
                  - ./config.yaml:/app/CoreMobileServer/config.yaml:ro
                depends_on:
                  core-mobile-db:
                    condition: service_healthy
              core-mobile-nginx:
                image: nginx:latest
                ports:
                  - "80:80"
                volumes:
                  - ./nginx.conf:/etc/nginx/nginx.conf
                depends_on:
                  - core-mobile-server
                  - core-mobile-db
              core-mobile-db:
                image: postgres
                volumes:
                  - ./tmp/db:/var/lib/postgresql/data
                environment:
                  POSTGRES_DB: "ktor_tutorial_db"
                  POSTGRES_HOST_AUTH_METHOD: trust
                ports:
                  - "5432:5432"
                healthcheck:
                  test: [ "CMD-SHELL", "pg_isready -U postgres" ]
                  interval: 1s
        """.trimIndent()

        val nginxContent = """
            events {}

            http {
                server {
                    listen 80;
                    server_name localhost;

                     location / {
                         proxy_pass http://core-mobile-server:8080/;
                     }
                }
            }
        """.trimIndent()

        try {
            if (projectDir.mkdirs()) {
                val dockerComposeFileName = ".docker-compose.yaml"
                val serviceAccountFileName = "serviceAccountKey.json"
                val tableFileName = "table.yaml"
                val configFileName = "config.yaml"
                val nginxFileName = "nginx.conf"

                val dockerComposeFile = File(projectDir, dockerComposeFileName)
                val serviceAccountFile = File(projectDir, serviceAccountFileName)
                val tableFile = File(projectDir, tableFileName)
                val configFile = File(projectDir, configFileName)
                val nginxFile = File(projectDir, nginxFileName)

                dockerComposeFile.writeText(dockerComposeContent)
                serviceAccountFile.writeText("")
                tableFile.writeText("")
                configFile.writeText("")
                nginxFile.writeText(nginxContent)
                println("Docker Compose Datei '$dockerComposeFileName' erfolgreich erstellt.")
                println("Table Datei '$tableFileName' erfolgreich erstellt.")
            } else {
                throw Exception("Fehler beim Erstellen des Verzeichnisses '$name'.")
            }
        } catch (e: Exception) {
            println("Fehler beim Erstellen der Docker Compose Datei: ${e.localizedMessage}")
        }
    }
}