package com.example.services

import com.example.models.CreateUserRequest
import com.example.models.UpdateUserRequest
import com.example.models.User
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory user service for managing users.
 * In production, this would interact with a database via a repository.
 * Uses AtomicLong for thread-safe ID generation.
 */
class UserService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val users = mutableMapOf<Long, User>()
    private val nextId = AtomicLong(1L)
    private val emailIndex = mutableSetOf<String>()

    init {
        // Add sample data
        users[1L] = User(1, "John Doe", "john@example.com", 30)
        users[2L] = User(2, "Jane Smith", "jane@example.com", 28)
        emailIndex.add("john@example.com")
        emailIndex.add("jane@example.com")
        nextId.set(3L)
        logger.info("UserService initialized with 2 sample users")
    }

    fun getAllUsers(): List<User> {
        logger.debug("Fetching all users")
        return users.values.toList().sortedBy { it.id }
    }

    fun getUserById(id: Long): User? {
        logger.debug("Fetching user with ID: $id")
        return users[id]
    }

    fun createUser(request: CreateUserRequest): User {
        logger.info("Creating user with email: ${request.email}")
        val id = nextId.getAndIncrement()
        val user = User(id, request.name, request.email, request.age)
        users[id] = user
        emailIndex.add(request.email)
        logger.info("User created successfully with ID: $id")
        return user
    }

    fun updateUser(id: Long, request: UpdateUserRequest): User? {
        logger.info("Updating user with ID: $id")
        val existing = users[id] ?: return null
        
        // If email is being changed, update the email index
        if (request.email != null && request.email != existing.email) {
            emailIndex.remove(existing.email)
            emailIndex.add(request.email)
        }
        
        val updated = existing.copy(
            name = request.name ?: existing.name,
            email = request.email ?: existing.email,
            age = request.age ?: existing.age
        )
        users[id] = updated
        logger.info("User with ID: $id updated successfully")
        return updated
    }

    fun deleteUser(id: Long): Boolean {
        logger.info("Deleting user with ID: $id")
        val user = users[id]
        if (user != null) {
            emailIndex.remove(user.email)
            users.remove(id)
            logger.info("User with ID: $id deleted successfully")
            return true
        }
        logger.warn("Attempted to delete non-existent user with ID: $id")
        return false
    }

    fun emailExists(email: String): Boolean {
        return emailIndex.contains(email)
    }
}
