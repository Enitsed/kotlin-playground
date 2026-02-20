package com.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val count: Int? = null
)

/**
 * Standardized response wrapper for all API responses.
 * Ensures consistent response format across all endpoints.
 */
@Serializable
data class ResponseWrapper<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val errors: List<String>? = null,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
)
