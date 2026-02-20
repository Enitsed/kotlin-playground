package com.example.service

import com.example.dto.CreateUserRequest
import com.example.dto.UpdateUserRequest
import com.example.entity.User
import com.example.exception.DuplicateResourceException
import com.example.exception.ResourceNotFoundException
import com.example.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    @Test
    fun testDuplicateEmailRejection() {
        MockitoAnnotations.openMocks(this)
        userService = UserService(userRepository)

        val request = CreateUserRequest(name = "John", email = "test@example.com", age = 25)
        whenever(userRepository.existsByEmail("test@example.com")).thenReturn(true)

        assertThrows<DuplicateResourceException> {
            userService.createUser(request)
        }
    }

    @Test
    fun testEmailSanitization() {
        MockitoAnnotations.openMocks(this)
        userService = UserService(userRepository)

        val request = CreateUserRequest(name = "John", email = "TEST@EXAMPLE.COM", age = 25)
        val now = LocalDateTime.now()
        val savedUser = User(id = 1L, name = "John", email = "test@example.com", age = 25, createdAt = now, updatedAt = now)

        whenever(userRepository.existsByEmail("test@example.com")).thenReturn(false)
        whenever(userRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(savedUser)

        val result = userService.createUser(request)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun testUserNotFound() {
        MockitoAnnotations.openMocks(this)
        userService = UserService(userRepository)

        whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.getUserById(999L)
        }
    }
}
