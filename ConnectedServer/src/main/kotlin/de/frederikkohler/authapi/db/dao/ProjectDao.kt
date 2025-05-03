package de.frederikkohler.authapi.db.dao

import de.frederikkohler.authapi.db.table.Projects
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ProjectDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectDao>(Projects)

    var name by Projects.name
    var userId by Projects.userId
    var token by Projects.token
}