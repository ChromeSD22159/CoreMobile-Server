package de.frederikkohler.authapi.repository

import de.frederikkohler.authapi.db.dao.ProjectDao
import de.frederikkohler.authapi.db.dao.UserDAO
import de.frederikkohler.authapi.db.table.Projects
import de.frederikkohler.authapi.errors.ProjectException
import de.frederikkohler.authapi.model.project.response.ProjectDto
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.suspendTransaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ProjectRepository(
    private val jwtService: JwtService
) {
    init {
        transaction {
            SchemaUtils.create(Projects)
        }
    }
    
    suspend fun createProject(name: String, userId: String): Result<ProjectDto> = suspendTransaction {
        val parsedUserId = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Ungültiges UUID-Format für userId: $userId"))
        }

        if (name.isBlank()) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Der Projektname darf nicht leer sein."))
        }

        val user = UserDAO.findById(parsedUserId)
        if (user != null) {
            val newProject = ProjectDao.new {
                this.name = name
                this.userId = user.id
                this.token = token
            }
            Result.success(newProject.toDto())
        } else {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Benutzer mit ID $userId nicht gefunden."))
        }
    }

    suspend fun generateProjectToken(userId: String, projectId: String): Result<ProjectDto> = suspendTransaction {
        val parsedProjectId = try {
            UUID.fromString(projectId)
        } catch (e: IllegalArgumentException) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Ungültiges Projekt-ID Format: $projectId"))
        }

        val project = ProjectDao.findById(parsedProjectId) ?: return@suspendTransaction Result.failure(ProjectException.ProjectNotFound)

        val token = jwtService.generateProjectAccessToken(projectId, userId)
        project.token = token
        Result.success(project.toDto())
    }

    suspend fun getProjectsByUserId(userId: String): Result<List<ProjectDto>> = suspendTransaction {
        val parsedUserId = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Ungültiges UUID-Format für userId: $userId"))
        }

        val projects = ProjectDao.find { Projects.userId eq parsedUserId }

        if (projects.empty()) {
            return@suspendTransaction Result.failure(ProjectException.ProjectNotFound)
        } else {
            return@suspendTransaction Result.success(projects.map { it.toDto() })
        }
    }

    suspend fun deleteProject(projectId: String): Result<Boolean> = suspendTransaction {
        val parsedProjectId = try {
            UUID.fromString(projectId)
        } catch (e: IllegalArgumentException) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Ungültiges Projekt-ID Format: $projectId"))
        }
        val project = ProjectDao.findById(parsedProjectId)

        if (project != null) {
            project.delete()
            Result.success(true)
        } else {
            Result.failure(ProjectException.ProjectNotFound)
        }
    }

    suspend fun revokeProjectToken(projectId: String): Result<Boolean> = suspendTransaction {
        val parsedProjectId = try {
            UUID.fromString(projectId)
        } catch (e: IllegalArgumentException) {
            return@suspendTransaction Result.failure(ProjectException.InvalidProjectData("Ungültiges Projekt-ID Format: $projectId"))
        }
        val project = ProjectDao.findById(parsedProjectId)
        if (project != null) {
            project.token = null
            Result.success(true)
        } else {
            Result.failure(ProjectException.ProjectNotFound)
        }
    }

    private fun ProjectDao.toDto() = ProjectDto(id.value.toString(), name, userId.value.toString(), token)
}

