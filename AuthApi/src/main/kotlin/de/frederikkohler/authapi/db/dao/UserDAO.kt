package de.frederikkohler.authapi.db.dao

import de.frederikkohler.authapi.db.table.Users
import de.frederikkohler.authapi.model.UserDto
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(Users)

    var email by Users.email
    var password by Users.password

    fun toModel() = UserDto(
        id = this.id.toString(),
        email = this.email,
        password = this.password
    )
}