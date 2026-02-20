package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Data Models
@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val age: Int? = null
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
)

// In-memory user store
class UserStore {
    private val users = mutableMapOf<Long, User>()
    private var nextId = 1L

    init {
        // Add sample data
        users[1L] = User(1, "John Doe", "john@example.com", 30)
        users[2L] = User(2, "Jane Smith", "jane@example.com", 28)
        nextId = 3L
    }

    fun getAllUsers(): List<User> = users.values.toList().sortedBy { it.id }

    fun getUserById(id: Long): User? = users[id]

    fun createUser(request: CreateUserRequest): User {
        val user = User(nextId, request.name, request.email, request.age)
        users[nextId] = user
        nextId++
        return user
    }

    fun updateUser(id: Long, request: UpdateUserRequest): User? {
        val existing = users[id] ?: return null
        val updated = existing.copy(
            name = request.name ?: existing.name,
            email = request.email ?: existing.email,
            age = request.age ?: existing.age
        )
        users[id] = updated
        return updated
    }

    fun deleteUser(id: Long): Boolean = users.remove(id) != null
}

// Global user store instance
val userStore = UserStore()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureContentNegotiation()
    }.start(wait = true)
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}

fun Application.configureRouting() {
    routing {
        // API Routes
        route("/api/v1") {
            // Get all users
            route("/users") {
                get {
                    try {
                        val users = userStore.getAllUsers()
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
                        if (request.name.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", "Name cannot be empty")
                            )
                            return@post
                        }
                        if (request.email.isBlank() || !request.email.contains("@")) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", "Invalid email format")
                            )
                            return@post
                        }
                        if (request.age <= 0 || request.age > 150) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", "Age must be between 1 and 150")
                            )
                            return@post
                        }

                        val user = userStore.createUser(request)
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

                // Get user by ID
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

                            val user = userStore.getUserById(id)
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

                    // Update user
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
                            val user = userStore.updateUser(id, request)
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

                    // Delete user
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

                            val deleted = userStore.deleteUser(id)
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

            // Health check endpoint
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

        // Root endpoint
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
}
