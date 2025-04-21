package de.frederikkohler.shared.configure

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.connectToPostgres(): Database {
    val config = environment.config
    val url = config.property("storage.jdbcURL").getString()
    val user = config.property("storage.user").getString()
    val password = config.property("storage.password").getString()

    return Database.connect(
        url,
        user = user,
        password = password
    )
}
