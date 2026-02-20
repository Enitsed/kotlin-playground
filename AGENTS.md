# AGENTS.md — Kotlin Learning Project

**Project**: Kotlin API Servers (learning guide with Ktor and Spring Boot examples)  
**Updated**: 2026-02-20  
**Active Agents**: Developers, linters, test runners

---

## PROJECT STRUCTURE

```
kotlin-example/
├── ktor-api-server/              # Lightweight async framework (Ktor 2.3.6)
│   ├── src/main/kotlin/
│   │   ├── Application.kt                    # Entry point (main, routing, plugin setup)
│   │   └── com/example/
│   │       ├── models/Models.kt              # @Serializable data classes (User, DTOs)
│   │       ├── services/UserService.kt       # Business logic (CRUD operations)
│   │       ├── routes/UserRoutes.kt          # HTTP route handlers (GET, POST, PUT, DELETE)
│   │       └── plugins/ContentNegotiation.kt # JSON serialization plugin
│   ├── build.gradle.kts
│   └── Dockerfile
│
├── spring-boot-api-server/       # Enterprise framework (Spring Boot 3.1.7)
│   ├── src/main/kotlin/com/example/
│   │   ├── Application.kt                    # Spring Boot main class
│   │   ├── controller/UserController.kt      # REST endpoints (@RestController)
│   │   ├── service/UserService.kt            # Business logic (@Service)
│   │   ├── repository/UserRepository.kt      # Data access (Spring Data JPA)
│   │   ├── entity/User.kt                    # JPA entity (@Entity)
│   │   ├── dto/UserDTO.kt                    # Request/response DTOs
│   │   ├── exception/CustomExceptions.kt     # Custom exception classes
│   │   ├── exception/GlobalExceptionHandler.kt # Global error handler (@RestControllerAdvice)
│   │   └── config/                           # Spring configuration (optional)
│   ├── src/main/resources/
│   │   ├── application.yaml                  # Default config
│   │   ├── application-dev.yaml              # Dev with SQL logging
│   │   └── application-prod.yaml             # Prod with minimal logging
│   ├── build.gradle.kts
│   ├── docker-compose.yml
│   └── Dockerfile
│
└── Documentation
    ├── AGENTS.md                 # Root project knowledge base
    ├── README.md                 # Project overview + learning path
    ├── GETTING_STARTED.md
    ├── SPRING_BOOT_KOTLIN_GUIDE.md
    └── FRAMEWORK_COMPARISON.md
```

Both projects implement **identical Users REST API** for direct framework comparison. Choose your learning path:

---

## BUILD & RUN COMMANDS

### Ktor (Lightweight, No Database)

```bash
cd ktor-api-server

# Run directly (development)
./gradlew run                    # Starts on localhost:8080

# Build
./gradlew build                  # Creates JAR in build/libs/

# Clean
./gradlew clean
```

### Spring Boot (Enterprise, MySQL Required)

```bash
cd spring-boot-api-server

# Start MySQL first
docker-compose up -d             # Starts MySQL on localhost:3306

# Run (default profile uses application.yaml)
./gradlew bootRun                # Starts on localhost:8080

# Run with dev profile (shows SQL queries, uses users_db_dev)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run with prod profile (minimal logging, requires env vars)
./gradlew bootRun --args='--spring.profiles.active=prod'

# Build
./gradlew build                  # Creates JAR in build/libs/

# Clean
./gradlew clean

# Stop MySQL
docker-compose down
```

### Testing

```bash
# Run all tests
./gradlew test

# Run single test file
./gradlew test --tests UserControllerTest

# Run single test method
./gradlew test --tests UserControllerTest.testCreateUser

# Show test output
./gradlew test --info
```

---

## CODE STYLE GUIDELINES

### Imports Organization

**Order**:
1. Kotlin stdlib
2. Java stdlib
3. Third-party libraries (alphabetical)
4. Local project imports
5. Blank line between groups

**Example**:
```kotlin
package com.example.controller

// Kotlin
import kotlin.io.*

// Java
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Spring/Third-party
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// Project
import com.example.dto.ApiResponse
import com.example.service.UserService
```

### Naming Conventions

| Item | Style | Example |
|------|-------|---------|
| **Classes** | PascalCase | `UserController`, `UserService`, `User` |
| **Interfaces** | PascalCase (no `I` prefix) | `UserRepository` |
| **Functions** | camelCase | `getAllUsers()`, `createUser()` |
| **Variables** | camelCase | `userName`, `userId`, `userList` |
| **Constants** | UPPER_SNAKE_CASE | `MAX_AGE = 150` |
| **Package names** | lowercase.dot.separated | `com.example.controller` |
| **DTOs** | Suffix with `Request`, `Response`, or `DTO` | `CreateUserRequest`, `UserResponse` |

### Type Safety & Null Handling

**Rules**:
- **No type suppression**: Never use `as Any`, `@Suppress`, or `@SuppressLint`
- **Explicit nullability**: Use `?` for nullable, never default to nullable
- **Null checks before use**: Use `?.let {}`, `?.also {}`, or safe cast `as?`
- **Elvis operator**: `val name = request.name ?: "Unknown"`

**Example**:
```kotlin
// ❌ WRONG
val user: User = userService.findById(id) as User  // Unsafe cast

// ✅ CORRECT
val user: User? = userService.findById(id)
val userName = user?.name ?: "Unknown"
```

### Data Classes & DTOs

**Pattern**:
- Use `data class` for DTOs, entities, and immutable models
- Separate requests (`CreateUserRequest`) from responses (`UserResponse`)
- Mark mandatory fields without defaults
- Optional fields use nullable types

**Example**:
```kotlin
// Request DTOs
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val age: Int? = null
)

// Response DTOs
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

### Formatting & Indentation

- **Indentation**: 4 spaces (never tabs)
- **Line length**: Max 120 characters (break after)
- **Spacing**: 1 blank line between functions/methods, 2 blank lines between classes
- **Braces**: Opening brace on same line (Kotlin convention)

**Example**:
```kotlin
class UserService(private val userRepository: UserRepository) {

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll()
            .map { user -> user.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            ?: throw ResourceNotFoundException("User not found")
        return user.toResponse()
    }
}
```

### Error Handling

**Pattern**:
- Custom exceptions in `exception/` package
- Global exception handler using `@RestControllerAdvice` (Spring Boot)
- Consistent `ApiResponse<T>` wrapper with `success`, `message`, `data`, `timestamp`
- Proper HTTP status codes (404 for not found, 409 for conflict, 400 for validation)

**Custom Exceptions**:
```kotlin
// In exception/CustomExceptions.kt
open class AppException(message: String) : Exception(message)

class ResourceNotFoundException(message: String) : AppException(message)
class ValidationException(message: String) : AppException(message)
class DuplicateResourceException(message: String) : AppException(message)
```

**Response Wrapper**:
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

**Global Handler Example**:
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException):
        ResponseEntity<ApiResponse<Any>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(success = false, message = ex.message))
    }
}
```

### Extension Functions & Scope Functions

**Use scope functions for clean object initialization**:
```kotlin
user.apply { name = "Updated" }  // Modify object
    .also { println(it) }        // Execute side effect
    .let { it.toResponse() }     // Transform to other type
```

**Extension functions for reusable logic**:
```kotlin
// Domain-specific helpers
fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    name = this.name,
    email = this.email,
    age = this.age
)
```

### Spring Boot Specific

**Annotations**:
- `@RestController` on controllers (returns JSON by default)
- `@Service` on service classes (business logic)
- `@Repository` on Spring Data JPA repositories (auto-wired)
- `@Entity` on JPA model classes
- `@Autowired` for dependency injection (can omit with constructor injection)
- `@Valid` on request bodies for validation
- `@Transactional` for transaction management (default on service methods)

**Validation**:
```kotlin
data class CreateUserRequest(
    @NotBlank(message = "Name cannot be empty")
    val name: String,
    
    @Email(message = "Email should be valid")
    val email: String,
    
    @Min(1, message = "Age must be >= 1")
    @Max(150, message = "Age must be <= 150")
    val age: Int
)
```

### Ktor Specific

**Architecture Pattern**: Modular plugin-based with separate concerns
- **Models** (`models/Models.kt`): `@Serializable` DTOs
- **Services** (`services/UserService.kt`): Business logic (CRUD, data management)
- **Routes** (`routes/UserRoutes.kt`): HTTP handlers, validation, route composition
- **Plugins** (`plugins/ContentNegotiation.kt`, etc.): Infrastructure setup

**Request/Response** with `call.receive<T>()` and `call.respond()`:
```kotlin
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

// In route handler
post("/users") {
    val request = call.receive<CreateUserRequest>()
    val user = userService.createUser(request)
    call.respond(HttpStatusCode.Created, mapOf("success" to true, "data" to user))
}
```

**Routing Composition** with extension functions:
```kotlin
fun Application.configureRouting() {
    routing {
        rootRoute()
        route("/api/v1") {
            userRoutes(userService)
            healthRoutes()
        }
    }
}

fun Route.userRoutes(userService: UserService) {
    route("/users") {
        get { /* implementation */ }
        post { /* implementation */ }
    }
}
```

**Error Handling** (Ktor):
```kotlin
try {
    val user = userStore.getUserById(id)
        ?: return@get call.respond(HttpStatusCode.NotFound,
            ErrorResponse("Not Found", "User $id not found"))
    call.respond(HttpStatusCode.OK, user)
} catch (e: Exception) {
    call.respond(HttpStatusCode.InternalServerError,
        ErrorResponse("Internal Error", e.message ?: "Unknown error"))
}
```

---

## CONVENTIONS NOT TO BREAK

1. **DTO separation**: Always use separate request/response DTOs
2. **Exception handling**: Centralized in global handler (Spring) or try-catch blocks (Ktor)
3. **No silent failures**: Always log or throw exceptions
4. **Type safety first**: Never suppress type errors
5. **Package-by-feature**: Organize by domain (controller, service, repository)
6. **Immutability**: Prefer `val` over `var`

---

## PROJECT TECH STACK

| Framework | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 1.9.21 | Language |
| Gradle | Latest | Build tool |
| Ktor | 2.3.6 | Lightweight server (async) |
| Spring Boot | 3.1.7 | Enterprise server |
| Spring Data JPA | 3.1.x | Database access |
| MySQL | 8.0 | Database (Spring Boot only) |
| kotlinx.serialization | 1.6.0 | JSON serialization (Ktor) |
| Jackson | Latest | JSON serialization (Spring Boot) |
| JUnit 5 | Latest | Testing framework |

---

## VALIDATION & TESTING

- **Unit tests**: `src/test/kotlin/`
- **Framework**: JUnit 5 + Kotlin Test
- **Spring Boot validation**: Uses Jakarta Bean Validation + custom validators
- **Ktor validation**: Manual validation in request handlers
- **No `.gradle` files in repo**: Use `./gradlew` (wrapper always available)

---

## USEFUL LINKS IN THIS REPO

- **Ktor README**: `ktor-api-server/README.md`
- **Spring Boot README**: `spring-boot-api-server/README.md`
- **Main README**: `README.md` (overview + learning path)
- **Spring Boot Guide**: `SPRING_BOOT_KOTLIN_GUIDE.md`
- **Framework Comparison**: `FRAMEWORK_COMPARISON.md`

---

## FIRST-TIME SETUP

```bash
# Clone and navigate
git clone <repo>
cd kotlin-example

# For Ktor
cd ktor-api-server
./gradlew build && ./gradlew run

# For Spring Boot (separate terminal)
cd spring-boot-api-server
docker-compose up -d
./gradlew build && ./gradlew bootRun

# Test endpoints
curl http://localhost:8080/api/v1/users
curl http://localhost:8080/api/v1/health
```
