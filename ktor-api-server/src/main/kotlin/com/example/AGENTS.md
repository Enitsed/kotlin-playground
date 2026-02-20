# AGENTS.md — Ktor Modular Architecture

**Scope**: `src/main/kotlin/com/example/`  
**Generated**: 2026-02-20  
**Pattern**: Ktor 2.3.6 modular async-first architecture  

---

## OVERVIEW

This subtree implements **Ktor's modular plugin-based architecture** for the Users REST API. Unlike monolithic single-file setups, this structure separates concerns: models, services, routes, and plugins. Ideal for scalable Ktor applications.

---

## STRUCTURE

```
com/example/
├── models/         # @Serializable data classes (DTOs, requests, responses)
├── services/       # Business logic (UserService, data operations)
├── routes/         # Routing DSL, HTTP handlers, route composition
├── plugins/        # Ktor plugins (ContentNegotiation, Auth, etc.)
└── Application.kt  # Entry point (main function, plugin installation, routing setup)
```

**Key**: Each module has **ONE JOB**. Plugins handle infrastructure. Services handle logic. Routes handle HTTP. Models carry data.

---

## WHERE TO LOOK

| Task | File | Role |
|------|------|------|
| Add HTTP endpoint | `routes/UserRoutes.kt` | Define route handler with validation |
| Change response shape | `models/Models.kt` | Update @Serializable data class |
| Add business logic | `services/UserService.kt` | Implement CRUD or domain rules |
| Setup JSON serialization | `plugins/ContentNegotiation.kt` | Install plugin, configure JSON |
| Change port or logging | `Application.kt` → main() | Modify embeddedServer config |
| Add HTTP plugin | `plugins/{NewPlugin}.kt` | Create new plugin file, install in Application.kt |

---

## LAYER PATTERNS

### Models Layer
**File**: `models/Models.kt`  
**Annotation**: `@Serializable` (kotlinx.serialization)  
**Purpose**: Type-safe JSON serialization, data transfer objects

**Pattern**:
```kotlin
@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val createdAt: String
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)
```

**Rules** ✅:
- All request/response shapes as `@Serializable data class`
- Separate request DTO from response DTO
- Use nullable types (`String?`) for optional fields
- Timestamps as ISO-8601 strings

**Never** ❌:
- Request and response share same class (breaks contract)
- Non-serializable fields without `@Transient`
- Serialize mutable state (breaks predictability)

---

### Services Layer
**File**: `services/UserService.kt`  
**Purpose**: Business logic, CRUD operations, data management

**Pattern**:
```kotlin
class UserService {
    private val users = mutableMapOf<Long, User>()
    private var nextId = 1L

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
```

**Rules** ✅:
- No HTTP concerns (status codes, requests/responses handled by routes)
- Return domain models, not DTOs
- Exceptions for error cases (validation failures, not-found, etc.)
- Business logic independent of transport layer

**Never** ❌:
- Return `HttpStatusCode` from service
- HTTP-specific exceptions (let routes map errors)
- Routes call business logic directly (violates separation)

---

### Routes Layer
**File**: `routes/UserRoutes.kt`  
**Purpose**: HTTP endpoint definition, request/response mapping, validation

**Pattern**:
```kotlin
fun Route.userRoutes(userService: UserService) {
    route("/users") {
        get {
            try {
                val users = userService.getAllUsers()
                call.respond(mapOf("success" to true, "data" to users, "count" to users.size))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: "Unknown error"))
            }
        }

        post {
            try {
                val request = call.receive<CreateUserRequest>()
                
                val validationError = validateCreateUserRequest(request)
                if (validationError != null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("VALIDATION_ERROR", validationError))
                    return@post
                }

                val user = userService.createUser(request)
                call.respond(HttpStatusCode.Created, mapOf("success" to true, "data" to user))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("PARSE_ERROR", e.message ?: "Invalid request"))
            }
        }
    }
}

private fun validateCreateUserRequest(request: CreateUserRequest): String? {
    return when {
        request.name.isBlank() -> "Name cannot be empty"
        request.email.isBlank() || !request.email.contains("@") -> "Invalid email format"
        request.age <= 0 || request.age > 150 -> "Age must be between 1 and 150"
        else -> null
    }
}
```

**Rules** ✅:
- HTTP layer only: parse requests, call service, respond
- Validation at boundary (request → service)
- Map service errors to HTTP status codes
- Return wrapped responses (success/error envelope)
- Use `Route` extension functions for composability

**Never** ❌:
- Business logic in route handlers
- Database/repository calls directly (use service)
- Catch generic `Exception` and hide (always log/respond)
- Forget try-catch (validation or service may fail)

---

### Plugins Layer
**File**: `plugins/ContentNegotiation.kt` (and others)  
**Purpose**: Ktor plugin installation and configuration

**Pattern**:
```kotlin
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}
```

**Rules** ✅:
- One plugin per file (`ContentNegotiation.kt`, `Auth.kt`, `Compression.kt`, etc.)
- Extension function on `Application` (called from main)
- Configure and install the plugin inside
- Keep configuration in single place

**Never** ❌:
- Multiple plugins in one file (harder to find)
- Plugin configuration in main() (move to plugin file)
- Hardcoded config (move to properties/env)

---

### Entry Point Layer
**File**: `Application.kt`  
**Purpose**: Server startup, plugin installation, routing setup

**Pattern**:
```kotlin
val userService = UserService()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureContentNegotiation()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        rootRoute()
        
        route("/api/v1") {
            userRoutes(userService)
            healthRoutes()
        }
    }
}
```

**Rules** ✅:
- `main()` only creates server and calls configuration functions
- Global singletons (services) at package level
- `configureRouting()` calls route extension functions
- All plugins installed before routing

**Never** ❌:
- Business logic in main()
- Create new service instances per request (use singleton)
- Forget to install plugin before using it in routes

---

## CODE MAP

| Symbol | Type | File | LOC | Role |
|--------|------|------|-----|------|
| `User` | data class | models/Models.kt | 8 | Domain model |
| `CreateUserRequest` | data class | models/Models.kt | 5 | Request DTO |
| `UpdateUserRequest` | data class | models/Models.kt | 5 | Update DTO |
| `ErrorResponse` | data class | models/Models.kt | 5 | Error DTO |
| `UserService` | class | services/UserService.kt | 40 | Business logic |
| `userRoutes()` | function | routes/UserRoutes.kt | 135 | Route group |
| `healthRoutes()` | function | routes/UserRoutes.kt | 15 | Health check |
| `rootRoute()` | function | routes/UserRoutes.kt | 20 | Welcome page |
| `configureContentNegotiation()` | function | plugins/ContentNegotiation.kt | 11 | JSON plugin |

---

## DEPENDENCY INJECTION PATTERN

Ktor doesn't have built-in DI like Spring. Manual singleton pattern used:

```kotlin
// In Application.kt (package level)
val userService = UserService()

// Inject into route handlers
fun Route.userRoutes(userService: UserService) {
    // Use injected service
}

// Call from configureRouting()
fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            userRoutes(userService)  // Pass singleton
        }
    }
}
```

**For larger apps**, consider Koin dependency injection:
```kotlin
val koinModule = module {
    single { UserService() }
}
```

---

## ROUTING COMPOSITION

Ktor routes are composable via extension functions on `Route`:

```kotlin
fun Application.configureRouting() {
    routing {
        // Root routes
        rootRoute()
        
        // Versioned API routes
        route("/api/v1") {
            userRoutes(userService)
            productRoutes(productService)  // Easy to add
            healthRoutes()
        }
        
        // Admin routes
        route("/admin") {
            adminRoutes(adminService)
        }
    }
}
```

Each `fun Route.xxxRoutes()` is self-contained and reusable.

---

## COROUTINES & ASYNC

All Ktor handlers are **suspend functions** by default (coroutine-based):

```kotlin
get {
    // This is a suspend function
    val users = userService.getAllUsers()  // May be async internally
    call.respond(users)
}

post {
    val request = call.receive<CreateUserRequest>()  // Suspends during I/O
    val user = userService.createUser(request)
    call.respond(HttpStatusCode.Created, user)
}
```

**Benefits**: Non-blocking, thousands of concurrent connections without threads.

---

## CONVENTIONS

1. **One file per logical concern**: models, services, routes, plugins
2. **Models only for serialization**: No business logic in data classes
3. **Services independent of HTTP**: Return models, throw exceptions
4. **Routes for HTTP only**: Parse requests, call service, respond
5. **Plugins for infrastructure**: Install once in main()
6. **Validation at boundaries**: Request comes in → validate → service
7. **Errors are exceptions**: Service throws, route catches, responds with HTTP status

---

## WHERE TO FIND PATTERNS

- **Models**: `models/Models.kt` — @Serializable shapes
- **Service logic**: `services/UserService.kt` → CRUD operations  
- **Route composition**: `routes/UserRoutes.kt` → userRoutes(), healthRoutes()
- **Plugin setup**: `plugins/ContentNegotiation.kt` → JSON configuration
- **Application setup**: `Application.kt` → main(), configureRouting()
- **Validation helpers**: `routes/UserRoutes.kt` → validateCreateUserRequest()

---

## ANTI-PATTERNS (THIS LAYER)

❌ **DO NOT**:
1. Put business logic in route handlers
2. Call services directly without route handlers (skip HTTP layer)
3. Add HTTP concerns to services (status codes, responses)
4. Use `@Serializable` for internal models (only for API contracts)
5. Install plugins in route handlers (install in main or configureRouting)
6. Mix multiple plugins in one file (one plugin per file)
7. Create new service instances per request (use singletons)
8. Forget try-catch in routes (always handle errors)
9. Return exceptions to client (wrap in ErrorResponse)
10. Validate in service (validate in route before calling service)

---

## TESTING PATTERN

```kotlin
@Test
fun testGetAllUsers() {
    val userService = UserService()
    val users = userService.getAllUsers()
    
    assertEquals(2, users.size)
    assertEquals("John Doe", users[0].name)
}

@Test
fun testCreateUser() {
    val userService = UserService()
    val request = CreateUserRequest("Alice", "alice@example.com", 25)
    val user = userService.createUser(request)
    
    assertNotNull(user.id)
    assertEquals("Alice", user.name)
}

// For HTTP layer, use Ktor test client
@Test
fun testGetUsersEndpoint() = testApplication {
    val response = client.get("/api/v1/users")
    assertEquals(HttpStatusCode.OK, response.status)
}
```

**Strategy**:
- Test services independently (mock nothing, use real objects)
- Test routes with Ktor test client
- Validation helpers can be unit tested directly

---

## NEXT STEPS

To extend this architecture:
1. **Add new endpoint**: Create new function in `routes/UserRoutes.kt`
2. **Add new service method**: Add to `services/UserService.kt`
3. **Add response model**: Create `@Serializable` in `models/Models.kt`
4. **Add plugin**: Create `plugins/NewPlugin.kt`, install in `Application.kt`
5. **Scale storage**: Replace `mutableMapOf` with database repository
6. **Add dependency injection**: Integrate Koin for larger projects
7. **Run tests**: `./gradlew test` (from `ktor-api-server/` root)

---

**See Also**: 
- Root `AGENTS.md` — project overview, frameworks, conventions
- `ktor-api-server/README.md` — setup instructions
- `ktor-api-server/build.gradle.kts` — dependencies and versions
