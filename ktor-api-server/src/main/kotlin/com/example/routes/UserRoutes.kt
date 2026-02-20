package com.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.models.*
import com.example.services.UserService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * User routes: GET /users, POST /users, GET /users/{id}, PUT /users/{id}, DELETE /users/{id}
 */
fun Route.userRoutes(userService: UserService) {
    route("/users") {
        // Get all users
        get {
            try {
                val users = userService.getAllUsers()
                call.respond(
                    mapOf(
                        "success" to true,
                        "data" to users,
                        "count" to users.size
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("ERROR", "Failed to fetch users: ${e.message}")
                )
            }
        }

        // Create user
        post {
            try {
                val request = call.receive<CreateUserRequest>()

                // Validation
                val validationError = validateCreateUserRequest(request)
                if (validationError != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", validationError)
                    )
                    return@post
                }

                val user = userService.createUser(request)
                call.respond(
                    HttpStatusCode.Created,
                    mapOf("success" to true, "data" to user)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("PARSE_ERROR", "Invalid request body: ${e.message}")
                )
            }
        }

        // Get user by ID, Update user, Delete user
        route("/{id}") {
            get {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "Invalid user ID")
                        )
                        return@get
                    }

                    val user = userService.getUserById(id)
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "User with ID $id not found")
                        )
                    } else {
                        call.respond(mapOf("success" to true, "data" to user))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("ERROR", "Failed to fetch user: ${e.message}")
                    )
                }
            }

            put {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "Invalid user ID")
                        )
                        return@put
                    }

                    val request = call.receive<UpdateUserRequest>()
                    val user = userService.updateUser(id, request)
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "User with ID $id not found")
                        )
                    } else {
                        call.respond(mapOf("success" to true, "data" to user))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("PARSE_ERROR", "Invalid request body: ${e.message}")
                    )
                }
            }

            delete {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "Invalid user ID")
                        )
                        return@delete
                    }

                    val deleted = userService.deleteUser(id)
                    if (!deleted) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "User with ID $id not found")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NoContent,
                            mapOf("success" to true, "message" to "User deleted successfully")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("ERROR", "Failed to delete user: ${e.message}")
                    )
                }
            }
        }
    }
}

/**
 * Health check endpoint
 */
fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            mapOf(
                "status" to "UP",
                "service" to "Ktor Users API",
                "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            )
        )
    }
}

/**
 * Root endpoint with usage information
 */
fun Route.rootRoute() {
    get("/") {
        call.respondText(
            """
            Welcome to Ktor Users API!
            Available endpoints:
            - GET    /api/v1/users              - Get all users
            - GET    /api/v1/users/{id}        - Get specific user
            - POST   /api/v1/users             - Create user
            - PUT    /api/v1/users/{id}        - Update user
            - DELETE /api/v1/users/{id}        - Delete user
            - GET    /api/v1/health            - Health check
            """.trimIndent(),
            ContentType.Text.Plain
        )
    }
}

/**
 * Validation helper for CreateUserRequest
 */
private fun validateCreateUserRequest(request: CreateUserRequest): String? {
    return when {
        request.name.isBlank() -> "Name cannot be empty"
        request.email.isBlank() || !request.email.contains("@") -> "Invalid email format"
        request.age <= 0 || request.age > 150 -> "Age must be between 1 and 150"
        else -> null
    }
}
