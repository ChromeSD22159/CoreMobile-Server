package de.frederikkohler.authapi.db.table

import org.jetbrains.exposed.dao.id.UUIDTable

object Projects : UUIDTable("projects") {
    val name = varchar("name", 255).uniqueIndex()
    val userId = reference("user_id", Users.id)
    val token = varchar("token", 350).nullable()
}