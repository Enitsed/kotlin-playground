package com.example.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateUserRequest(
    @field:NotBlank(message = "Name cannot be empty")
    val name: String,

    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email cannot be empty")
    val email: String,

    @field:Min(value = 1, message = "Age must be greater than 0")
    @field:Max(value = 150, message = "Age must be less than or equal to 150")
    val age: Int
)

data class UpdateUserRequest(
    @field:NotBlank(message = "Name cannot be empty")
    val name: String? = null,

    @field:Email(message = "Email should be valid")
    val email: String? = null,

    @field:Min(value = 1, message = "Age must be greater than 0")
    @field:Max(value = 150, message = "Age must be less than or equal to 150")
    val age: Int? = null
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
