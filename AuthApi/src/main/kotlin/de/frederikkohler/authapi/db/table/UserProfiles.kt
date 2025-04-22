package de.frederikkohler.authapi.db.table

import org.jetbrains.exposed.dao.id.UUIDTable

object UserProfiles : UUIDTable("userProfile") {
    val userId = varchar("uid", 50)
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val birthDate = varchar("birthDate", 50)
}