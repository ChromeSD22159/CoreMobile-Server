package de.frederikkohler.authapi.db.table

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("user") {
    val uid = varchar("uid", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
}