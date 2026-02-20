package com.example.exception

import com.example.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Resource not found: {}", ex.message)
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message ?: "Resource not found",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Validation error: {}", ex.message)
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message ?: "Validation failed",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(
        ex: DuplicateResourceException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Duplicate resource: {}", ex.message)
        val response = ApiResponse<Any>(
            success = false,
            message = ex.message ?: "Resource already exists",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Data integrity violation occurred", ex)
        val response = ApiResponse<Any>(
            success = false,
            message = "Operation failed due to data constraint violation",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Validation failed for request")
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
        logger.error("Unexpected error occurred", ex)
        val response = ApiResponse<Any>(
            success = false,
            message = "Internal server error",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
