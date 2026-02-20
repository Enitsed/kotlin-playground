package com.example.exception

import com.example.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(
        ex: DuplicateResourceException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.bindingResult.allErrors.map { error ->
            if (error is FieldError) {
                "${error.field}: ${error.defaultMessage}"
            } else {
                error.defaultMessage ?: "Validation error"
            }
        }

        val response = ApiResponse<Any>(
            success = false,
            message = "Validation failed",
            errors = errors,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            message = "Internal server error: ${ex.message}",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
