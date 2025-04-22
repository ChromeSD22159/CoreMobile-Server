package de.frederikkohler.authapi.db.dao

import de.frederikkohler.authapi.db.table.UserProfiles
import de.frederikkohler.authapi.model.UserProfileDTO
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserProfileDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserProfileDAO>(UserProfiles)
    var userId by UserProfiles.userId
    var firstName by UserProfiles.firstName
    var lastName by UserProfiles.lastName
    var birthDate by UserProfiles.birthDate

    fun toModel() = UserProfileDTO(
        id = this.id.toString(),
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        birthDate = this.birthDate
    )
}