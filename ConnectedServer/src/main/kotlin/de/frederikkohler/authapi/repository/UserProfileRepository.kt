package de.frederikkohler.authapi.repository

import de.frederikkohler.authapi.db.dao.UserProfileDAO
import de.frederikkohler.authapi.db.table.Projects
import de.frederikkohler.authapi.db.table.UserProfiles
import de.frederikkohler.authapi.model.UserProfileDTO
import de.frederikkohler.authapi.model.UserUpdateDto
import de.frederikkohler.authapi.model.toUUID
import de.frederikkohler.shared.suspendTransaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction



class UserProfileRepository {
    init {
        transaction {
            SchemaUtils.create(UserProfiles)
        }
    }

    suspend fun createUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        birthDate: String
    ) = suspendTransaction {
        UserProfileDAO.new {
            this.userId = userId
            this.firstName = firstName
            this.lastName = lastName
            this.birthDate = birthDate
        }
    }

    suspend fun getUserProfileByUserId(id: String) = suspendTransaction {
        UserProfileDAO.find { UserProfiles.userId eq id }.firstOrNull()?.toModel()
    }

    suspend fun updateUserProfile(userId: String, profile: UserUpdateDto): UserProfileDTO? = suspendTransaction {
        val userProfileDAO = UserProfileDAO.findById(userId.toUUID())
        return@suspendTransaction userProfileDAO?.apply {
            firstName = profile.firstName
            lastName = profile.lastName
            birthDate = profile.birthDate
        }?.toModel()
    }

    suspend fun deleteUserProfile(id: String): Boolean = suspendTransaction {
        val userProfileDAO = UserProfileDAO.findById(id.toUUID())
        return@suspendTransaction if (userProfileDAO != null) {
            userProfileDAO.delete()
            true
        } else {
            false
        }
    }
}