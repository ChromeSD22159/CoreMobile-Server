package de.frederikkohler.authapi.routes

import de.frederikkohler.authapi.SessionTokensDTO
import de.frederikkohler.authapi.errors.ProjectException
import de.frederikkohler.authapi.model.project.request.NewProjectDto
import de.frederikkohler.authapi.model.project.response.ProjectDto
import de.frederikkohler.authapi.repository.ProjectRepository
import de.frederikkohler.shared.services.JwtService
import de.frederikkohler.shared.utils.authenticatedRoutes
import de.frederikkohler.shared.utils.sdkRoutes
import de.frederikkohler.utils.badRequest
import de.frederikkohler.utils.getAuthToken
import de.frederikkohler.utils.ok
import de.frederikkohler.utils.unauthorized
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond




fun Application.configureProjectRouting(
    jwtService: JwtService,
    tag: List<String> = listOf("Project")
) {
    val projectRepository = ProjectRepository(jwtService)

    authenticatedRoutes {
        post("project", {
            tags = tag
            protected = true
            description = """
                Create project
                
                **Authorization Header:** `Bearer <UserRefreshToken>`
            """.trimIndent()
            request {
                body<NewProjectDto> {
                    description = "Project data"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<ProjectDto> {
                        description = "Returns the generated project"
                    }
                }
                code(HttpStatusCode.BadRequest) {
                    description = "failed or unauthorized Authorization Header"
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Invalid or expired access token"
                }
                code(HttpStatusCode.Forbidden) {
                    description = "Not authorized to create refresh tokens"
                }
            }
        }) {
            val token = call.getAuthToken()

            val newProject = call.receive<NewProjectDto>()

            if (token != null) {
                val userId = jwtService.verifyRefreshTokenAndGetUserId(token)


                if (userId != null) {
                    val result = projectRepository.createProject(
                        name = newProject.name,
                        userId = userId
                    )

                    result.onSuccess {
                        call.ok(it)
                    }
                    result.onFailure {
                        call.badRequest()
                    }
                } else call.unauthorized()
            } else {
                call.unauthorized()
            }
        }

        delete("project/{projectId}", {
            tags = tag
            protected = true
            description = """
                Delete project
                
                 **Authorization Header:** `Bearer <UserRefreshToken>`
            """.trimIndent()
            request {
                pathParameter<String>("projectId") {
                    description = "Project ID"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "Delete project"
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Invalid or missing authorization token"
                }
                code(HttpStatusCode.NotFound) {
                    description = "Project not found"
                }
                code(HttpStatusCode.InternalServerError) {
                    description = "Error when deleting the project"
                }
            }
        }) {
            val projectId = call.parameters["projectId"] ?: return@delete call.badRequest()

            try {
                val result = projectRepository.deleteProject(projectId)

                result.onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure { _ ->
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Unexpected error: ${e.message}")
            }
        }

        get("project/list", {
            tags = tag
            protected = true
            description = """
                List all projects from user
                
                **Authorization Header:** `Bearer <UserRefreshToken>`
            """.trimIndent()
            response {
                code(HttpStatusCode.OK) {
                    body<List<ProjectDto>> {
                        description = "List of all projects of the user"
                    }
                }
            }
        }) {
            val token = call.getAuthToken() ?: return@get call.unauthorized()
            val userId = jwtService.verifyRefreshTokenAndGetUserId(token) ?: return@get call.unauthorized()

            val result = projectRepository.getProjectsByUserId(userId)

            result.onSuccess {
                call.ok(it)
            }.onFailure {
                call.ok(listOf<ProjectDto>())
            }
        }

        put("project/{projectId}/generateAccessToken", {
            tags = tag
            protected = true
            description = """
                Generates an new ProjectAccessToken for a valid UserRefreshToken
                **Authorization Header:** `Bearer <UserRefreshToken>`
            """.trimIndent()
            request {
                pathParameter<String>("projectId") {
                    description = "The ID of the user"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<ProjectDto> {
                        description = "Project"
                    }
                }
            }
        }) {
            val token = call.getAuthToken() ?: return@put call.unauthorized()
            val userIdResult = jwtService.verifyRefreshTokenAndGetUserId(token) ?: return@put call.unauthorized()
            val projectId = call.parameters["projectId"] ?: return@put call.badRequest()

            val result = projectRepository.generateProjectToken(userIdResult, projectId)

            result.onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }.onFailure { exception ->
                when (exception) {
                    is ProjectException.ProjectNotFound -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.InternalServerError, "Error when creating the Access Token: ${exception.message}")
                }
            }
        }
    }
}