package com.example.service

import com.example.dto.CreateUserRequest
import com.example.dto.UpdateUserRequest
import com.example.dto.UserResponse
import com.example.entity.User
import com.example.exception.DuplicateResourceException
import com.example.exception.ResourceNotFoundException
import com.example.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with ID $id not found") }
            .toResponse()
    }

    fun createUser(request: CreateUserRequest): UserResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("User with email ${request.email} already exists")
        }

        val user = User(
            name = request.name,
            email = request.email,
            age = request.age,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with ID $id not found") }

        // Check if email is being updated and if it already exists (but not for the same user)
        if (request.email != null && request.email != user.email) {
            if (userRepository.existsByEmail(request.email)) {
                throw DuplicateResourceException("User with email ${request.email} already exists")
            }
        }

        val updatedUser = user.copy(
            name = request.name ?: user.name,
            email = request.email ?: user.email,
            age = request.age ?: user.age,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse()
    }

    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User with ID $id not found")
        }
        userRepository.deleteById(id)
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
