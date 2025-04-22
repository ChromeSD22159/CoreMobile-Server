package de.frederikkohler.authapi.db.table

import org.jetbrains.exposed.dao.id.UUIDTable

object Users : UUIDTable("user") {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
}