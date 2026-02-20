package com.example.service

import com.example.dto.CreateUserRequest
import com.example.dto.UpdateUserRequest
import com.example.dto.UserResponse
import com.example.entity.User
import com.example.exception.DuplicateResourceException
import com.example.exception.ResourceNotFoundException
import com.example.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAllUsers(): List<UserResponse> {
        logger.debug("Fetching all users")
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        logger.debug("Fetching user with ID: {}", id)
        return userRepository.findById(id)
            .orElseThrow {
                logger.warn("User not found with ID: {}", id)
                ResourceNotFoundException("User not found")
            }
            .toResponse()
    }

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        logger.debug("Creating user with email: {}", request.email)

        // Sanitize input
        val sanitizedName = request.name.trim()
        val sanitizedEmail = request.email.trim().lowercase()

        // Check if email already exists
        if (userRepository.existsByEmail(sanitizedEmail)) {
            logger.warn("Duplicate email attempt: {}", sanitizedEmail)
            throw DuplicateResourceException("Email already in use")
        }

        val user = User(
            name = sanitizedName,
            email = sanitizedEmail,
            age = request.age,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        logger.info("User created successfully with ID: {}", savedUser.id)
        return savedUser.toResponse()
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        logger.debug("Updating user with ID: {}", id)

        val user = userRepository.findById(id)
            .orElseThrow {
                logger.warn("User not found for update with ID: {}", id)
                ResourceNotFoundException("User not found")
            }

        // Check if email is being updated and if it already exists (but not for the same user)
        if (request.email != null) {
            val sanitizedEmail = request.email.trim().lowercase()
            if (sanitizedEmail != user.email && userRepository.existsByEmail(sanitizedEmail)) {
                logger.warn("Duplicate email attempt during update: {}", sanitizedEmail)
                throw DuplicateResourceException("Email already in use")
            }
        }

        val updatedUser = user.copy(
            name = request.name?.trim() ?: user.name,
            email = request.email?.trim()?.lowercase() ?: user.email,
            age = request.age ?: user.age,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)
        logger.info("User updated successfully with ID: {}", id)
        return savedUser.toResponse()
    }

    @Transactional
    fun deleteUser(id: Long) {
        logger.debug("Deleting user with ID: {}", id)

        if (!userRepository.existsById(id)) {
            logger.warn("User not found for deletion with ID: {}", id)
            throw ResourceNotFoundException("User not found")
        }

        userRepository.deleteById(id)
        logger.info("User deleted successfully with ID: {}", id)
    }

    private fun User.toResponse() = UserResponse(
        id = this.id ?: 0,
        name = this.name,
        email = this.email,
        age = this.age,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
