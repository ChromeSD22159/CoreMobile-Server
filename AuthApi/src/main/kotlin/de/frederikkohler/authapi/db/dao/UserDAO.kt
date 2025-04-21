package de.frederikkohler.authapi.db.dao

import de.frederikkohler.authapi.db.table.UserTable
import de.frederikkohler.authapi.model.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)
    var uid by UserTable.uid
    var email by UserTable.email
    var password by UserTable.password

    fun toModel() = User(
        uid = this.uid,
        email = this.email,
        password = this.password
    )
}