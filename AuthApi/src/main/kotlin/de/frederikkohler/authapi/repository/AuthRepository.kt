package de.frederikkohler.authapi.repository

import de.frederikkohler.authapi.SessionTokensDTO
import de.frederikkohler.authapi.db.dao.UserDAO
import de.frederikkohler.authapi.db.table.UserTable
import de.frederikkohler.authapi.model.User
import de.frederikkohler.authapi.model.UserLoginDTO
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.suspendTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

interface UserRepository {
    suspend fun allUsers(): List<String>
    suspend fun userByEmail(name: String): User?
    suspend fun userById(id: String): User
    suspend fun insertUser(user: User): Boolean
    suspend fun createUser(username: String, password: String): User?
    suspend fun login(username: String, password: String): UserLoginDTO?
    suspend fun generateSessionTokens(accessToken: String): SessionTokensDTO?
}

class AuthRepository(
    private val database: Database,
    private val jwtService: JwtService
): UserRepository {

    init {
        transaction {
            SchemaUtils.create(UserTable)
        }
    }

    override suspend fun allUsers(): List<String> = suspendTransaction {
       UserDAO.all().map { it.email }
    }

    override suspend fun userByEmail(name: String): User? = suspendTransaction {
        UserDAO.find { UserTable.email eq name }.firstOrNull()?.toModel()
    }

    override suspend fun userById(id: String): User = suspendTransaction {
        val result = UserDAO.find { UserTable.uid eq id }.firstOrNull()

        result?.toModel() ?: throw NoSuchElementException("User with id $id not found")
    }

    override suspend fun insertUser(user: User): Boolean = suspendTransaction {
        UserDAO.new {
            uid = user.uid
            email = user.email
            password = user.password
        }
        true
    }

    override suspend fun createUser(mail: String, password: String): User? {
        val user = User(UUID.randomUUID().toString(), mail, password)
        val result = insertUser(user)

        return if (result) user else null
    }

    override suspend fun login(username: String, password: String): UserLoginDTO? = suspendTransaction {
        val userDao = UserDAO.find { (UserTable.email eq username) and (UserTable.password eq password) }.firstOrNull()
        if (userDao != null) {
            val user = userDao.toModel()
            val accessToken = jwtService.generateAccessToken(user.uid)
            val refreshToken = jwtService.generateRefreshToken(user.uid)
            UserLoginDTO(user, accessToken = accessToken, refreshToken)
        } else {
            null
        }
    }

    override suspend fun generateSessionTokens(accessToken: String): SessionTokensDTO? {
        val userId = jwtService.verifyAccessTokenAndGetUserId(accessToken)

        if (userId != null) {
            val refreshToken = jwtService.generateRefreshToken(userId)

            return SessionTokensDTO(refreshToken)
        } else return null
    }
}