package com.example.services

import com.example.models.CreateUserRequest
import com.example.models.UpdateUserRequest
import com.example.models.User

/**
 * In-memory user service for managing users.
 * In production, this would interact with a database via a repository.
 */
class UserService {
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
