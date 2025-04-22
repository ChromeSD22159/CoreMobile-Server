package de.frederikkohler.authapi.repository

import de.frederikkohler.authapi.db.dao.UserProfileDAO
import de.frederikkohler.authapi.db.table.UserProfiles
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.suspendTransaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class UserProfileRepository(
    private val jwtService: JwtService
) {
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
}