package com.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.models.*
import com.example.services.UserService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = LoggerFactory.getLogger("UserRoutes")

/**
 * User routes: GET /users, POST /users, GET /users/{id}, PUT /users/{id}, DELETE /users/{id}
 */
fun Route.userRoutes(userService: UserService) {
    route("/users") {
        // Get all users
        get {
            try {
                logger.debug("GET /users - fetching all users")
                val users = userService.getAllUsers()
                call.respond(
                    HttpStatusCode.OK,
                    ResponseWrapper(
                        success = true,
                        data = users,
                        message = "Users retrieved successfully"
                    )
                )
            } catch (e: Exception) {
                logger.error("Error fetching users", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ResponseWrapper<Any>(
                        success = false,
                        message = "Failed to retrieve users"
                    )
                )
            }
        }

        // Create user
        post {
            try {
                logger.debug("POST /users - creating new user")
                val request = call.receive<CreateUserRequest>()

                // Validation
                val validationErrors = validateCreateUserRequest(request, userService)
                if (validationErrors.isNotEmpty()) {
                    logger.warn("Validation failed for create user request: $validationErrors")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseWrapper<Any>(
                            success = false,
                            message = "Validation failed",
                            errors = validationErrors
                        )
                    )
                    return@post
                }

                val user = userService.createUser(request)
                logger.info("User created successfully with ID: ${user.id}")
                call.respond(
                    HttpStatusCode.Created,
                    ResponseWrapper(
                        success = true,
                        data = user,
                        message = "User created successfully"
                    )
                )
            } catch (e: Exception) {
                logger.error("Error creating user", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    ResponseWrapper<Any>(
                        success = false,
                        message = "Invalid request body"
                    )
                )
            }
        }

        // Get user by ID, Update user, Delete user
        route("/{id}") {
            get {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        logger.warn("Invalid user ID parameter")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "Invalid user ID"
                            )
                        )
                        return@get
                    }

                    logger.debug("GET /users/$id - fetching user")
                    val user = userService.getUserById(id)
                    if (user == null) {
                        logger.warn("User not found with ID: $id")
                        call.respond(
                            HttpStatusCode.NotFound,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "User not found"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            ResponseWrapper(
                                success = true,
                                data = user,
                                message = "User retrieved successfully"
                            )
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error fetching user", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ResponseWrapper<Any>(
                            success = false,
                            message = "Failed to retrieve user"
                        )
                    )
                }
            }

            put {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        logger.warn("Invalid user ID parameter for PUT")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "Invalid user ID"
                            )
                        )
                        return@put
                    }

                    logger.debug("PUT /users/$id - updating user")
                    val request = call.receive<UpdateUserRequest>()
                    
                    // Validate update request
                    val validationErrors = validateUpdateUserRequest(request, userService, id)
                    if (validationErrors.isNotEmpty()) {
                        logger.warn("Validation failed for update user request: $validationErrors")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "Validation failed",
                                errors = validationErrors
                            )
                        )
                        return@put
                    }
                    
                    val user = userService.updateUser(id, request)
                    if (user == null) {
                        logger.warn("User not found for update with ID: $id")
                        call.respond(
                            HttpStatusCode.NotFound,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "User not found"
                            )
                        )
                    } else {
                        logger.info("User updated successfully with ID: $id")
                        call.respond(
                            HttpStatusCode.OK,
                            ResponseWrapper(
                                success = true,
                                data = user,
                                message = "User updated successfully"
                            )
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error updating user", e)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseWrapper<Any>(
                            success = false,
                            message = "Invalid request body"
                        )
                    )
                }
            }

            delete {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        logger.warn("Invalid user ID parameter for DELETE")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "Invalid user ID"
                            )
                        )
                        return@delete
                    }

                    logger.debug("DELETE /users/$id - deleting user")
                    val deleted = userService.deleteUser(id)
                    if (!deleted) {
                        logger.warn("User not found for deletion with ID: $id")
                        call.respond(
                            HttpStatusCode.NotFound,
                            ResponseWrapper<Any>(
                                success = false,
                                message = "User not found"
                            )
                        )
                    } else {
                        logger.info("User deleted successfully with ID: $id")
                        call.respond(HttpStatusCode.NoContent)
                    }
                } catch (e: Exception) {
                    logger.error("Error deleting user", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ResponseWrapper<Any>(
                            success = false,
                            message = "Failed to delete user"
                        )
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
        logger.debug("GET /health - health check")
        call.respond(
            HttpStatusCode.OK,
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
        logger.debug("GET / - root endpoint")
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
private fun validateCreateUserRequest(request: CreateUserRequest, userService: UserService): List<String> {
    val errors = mutableListOf<String>()
    
    if (request.name.isBlank()) {
        errors.add("Name cannot be empty")
    }
    
    if (request.email.isBlank()) {
        errors.add("Email cannot be empty")
    } else if (!isValidEmail(request.email)) {
        errors.add("Email format is invalid")
    } else if (userService.emailExists(request.email)) {
        errors.add("Email already exists")
    }
    
    if (request.age < 1 || request.age > 150) {
        errors.add("Age must be between 1 and 150")
    }
    
    return errors
}

/**
 * Validation helper for UpdateUserRequest
 */
private fun validateUpdateUserRequest(request: UpdateUserRequest, userService: UserService, userId: Long): List<String> {
    val errors = mutableListOf<String>()
    
    if (request.name != null && request.name.isBlank()) {
        errors.add("Name cannot be empty")
    }
    
    if (request.email != null) {
        if (request.email.isBlank()) {
            errors.add("Email cannot be empty")
        } else if (!isValidEmail(request.email)) {
            errors.add("Email format is invalid")
        } else if (userService.emailExists(request.email)) {
            val existingUser = userService.getUserById(userId)
            if (existingUser?.email != request.email) {
                errors.add("Email already exists")
            }
        }
    }
    
    if (request.age != null && (request.age < 1 || request.age > 150)) {
        errors.add("Age must be between 1 and 150")
    }
    
    return errors
}

/**
 * Email validation helper
 */
private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".") && email.length > 5
}
