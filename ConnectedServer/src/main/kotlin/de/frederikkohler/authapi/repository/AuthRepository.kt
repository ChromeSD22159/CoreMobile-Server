package de.frederikkohler.authapi.repository

import de.frederikkohler.authapi.SessionTokensDTO
import de.frederikkohler.authapi.db.dao.UserDAO
import de.frederikkohler.authapi.db.table.Users
import de.frederikkohler.authapi.model.UserDto
import de.frederikkohler.authapi.model.UserLoginDTO
import de.frederikkohler.authapi.model.toUUID
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.suspendTransaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

interface UserRepository {
    suspend fun allUsers(): List<String>
    suspend fun userByEmail(name: String): UserDto?
    suspend fun userById(id: String): UserDto
    suspend fun insertUser(userDto: UserDto): Boolean
    suspend fun createUser(mail: String, password: String): UserDto?
    suspend fun login(username: String, password: String): UserLoginDTO?
    suspend fun generateSessionTokens(accessToken: String): SessionTokensDTO?
    suspend fun generateSessionTokensByProjectId(projectToken: String): SessionTokensDTO?
}

class AuthRepository(
    private val jwtService: JwtService
): UserRepository {

    init {
        transaction {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun allUsers(): List<String> = suspendTransaction {
       UserDAO.all().map { it.email }
    }

    override suspend fun userByEmail(name: String): UserDto? = suspendTransaction {
        UserDAO.find { Users.email eq name }.firstOrNull()?.toModel()
    }

    override suspend fun userById(id: String): UserDto = suspendTransaction {
        val result = UserDAO.find { Users.id eq id.toUUID() }.firstOrNull()

        result?.toModel() ?: throw NoSuchElementException("User with id $id not found")
    }

    override suspend fun insertUser(userDto: UserDto): Boolean = suspendTransaction {
        UserDAO.new {
            email = userDto.email
            password = userDto.password
        }
        true
    }

    override suspend fun createUser(mail: String, password: String): UserDto? {
        val userDto = UserDto(UUID.randomUUID().toString(), mail, password)
        val result = insertUser(userDto)

        return if (result) userDto else null
    }

    override suspend fun login(username: String, password: String): UserLoginDTO? = suspendTransaction {
        val userDao = UserDAO.find { (Users.email eq username) and (Users.password eq password) }.firstOrNull()
        if (userDao != null) {
            val user = userDao.toModel()
            val accessToken = jwtService.generateAccessToken(user.id)
            val refreshToken = jwtService.generateRefreshToken(user.id)
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

    override suspend fun generateSessionTokensByProjectId(projectToken: String): SessionTokensDTO? {
        val userId = jwtService.verifyAccessTokenAndGenerateRefreshToken(projectToken)

        if (userId != null) {
            val refreshToken = jwtService.generateRefreshToken(userId)

            return SessionTokensDTO(refreshToken)
        } else return null
    }
}