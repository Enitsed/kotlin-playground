package com.example.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ValidationException(message: String) : RuntimeException(message)

class DuplicateResourceException(message: String) : RuntimeException(message)
