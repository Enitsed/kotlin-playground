package com.example.controller

import com.example.dto.ApiResponse
import com.example.dto.CreateUserRequest
import com.example.dto.UpdateUserRequest
import com.example.dto.UserResponse
import com.example.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "List of users retrieved successfully"
            )
        ]
    )
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        logger.info("GET /api/v1/users - Fetching all users")
        val users = userService.getAllUsers()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = users,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "User retrieved successfully"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        ]
    )
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<UserResponse>> {
        logger.info("GET /api/v1/users/{} - Fetching user by ID", id)
        val user = userService.getUserById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = user,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user with provided details")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "User created successfully"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "User with this email already exists"
            )
        ]
    )
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<ApiResponse<UserResponse>> {
        logger.info("POST /api/v1/users - Creating user with email: {}", request.email)
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = user,
                message = "User created successfully",
                timestamp = LocalDateTime.now()
            )
        )
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Update an existing user with new details")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "User updated successfully"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "User not found"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Email already exists"
            )
        ]
    )
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        logger.info("PUT /api/v1/users/{} - Updating user", id)
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = user,
                message = "User updated successfully",
                timestamp = LocalDateTime.now()
            )
        )
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Delete a user by their ID")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "User deleted successfully"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        ]
    )
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info("DELETE /api/v1/users/{} - Deleting user", id)
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is up and running")
    fun healthCheck(): ResponseEntity<ApiResponse<String>> {
        logger.debug("GET /api/v1/users/health - Health check")
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = "Service is running",
                message = "Spring Boot Users API is healthy",
                timestamp = LocalDateTime.now()
            )
        )
    }
}
