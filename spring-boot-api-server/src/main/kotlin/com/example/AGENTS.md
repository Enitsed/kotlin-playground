# AGENTS.md — Spring Boot Layered Architecture

**Scope**: `src/main/kotlin/com/example/`  
**Generated**: 2026-02-20  
**Pattern**: Spring Boot 3.1.7 enterprise layered architecture  

---

## OVERVIEW

This subtree implements **Spring Boot's layered architecture** for the Users REST API. Each subdirectory represents a distinct layer with specific responsibilities and patterns. This is the reference implementation for enterprise Kotlin/Spring development in this project.

---

## STRUCTURE

```
com/example/
├── controller/        # HTTP layer (@RestController) - request routing
├── service/          # Business logic (@Service) - domain rules
├── repository/       # Data access (@Repository) - JPA queries
├── entity/          # Database models (@Entity) - persistence
├── dto/             # API contracts (Request/Response) - serialization
├── exception/       # Error handling (@RestControllerAdvice) - global errors
└── config/          # Spring configuration (@Configuration)
```

**Key**: Each layer has **ONE PURPOSE**. Violations break testability.

---

## WHERE TO LOOK

| Task | File | Role |
|------|------|------|
| Add REST endpoint | `controller/UserController.kt` | Handle HTTP requests, validation, routing |
| Implement business logic | `service/UserService.kt` | Apply domain rules, orchestrate layers |
| Add database query | `repository/UserRepository.kt` | Extend Spring Data JPA, access DB |
| Add database field | `entity/User.kt` | Extend @Entity, add columns |
| Create request/response shape | `dto/UserDTO.kt` | Define API contracts |
| Add custom exception | `exception/CustomExceptions.kt` | Extend AppException |
| Handle new error type | `exception/GlobalExceptionHandler.kt` | @RestControllerAdvice handler |

---

## LAYER PATTERNS

### Controller Layer
**File**: `controller/UserController.kt`  
**Annotation**: `@RestController`  
**Purpose**: HTTP endpoint handling only

**Pattern**:
```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {
    
    @GetMapping
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        // 1. Accept HTTP request
        // 2. Call service
        // 3. Return response
        // NO business logic here
    }
}
```

**Rules** ✅:
- Accept request, call service, return response
- Input validation via `@Valid` on request body
- HTTP status codes only (404, 409, 400, 500)
- Return wrapped in `ApiResponse<T>`

**Never** ❌:
- Database queries (use service)
- Business logic (use service)
- Catch exceptions (let GlobalExceptionHandler catch)

---

### Service Layer
**File**: `service/UserService.kt`  
**Annotation**: `@Service`  
**Purpose**: Business logic and orchestration

**Pattern**:
```kotlin
@Service
class UserService(private val userRepository: UserRepository) {
    
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        // 1. Validate business rules
        // 2. Check for conflicts
        // 3. Transform to entity
        // 4. Call repository
        // 5. Transform to response
    }
}
```

**Rules** ✅:
- Implement business rules (e.g., "email must be unique")
- Validate before database operations
- Throw custom exceptions on violations
- Use `@Transactional` for data consistency
- Orchestrate multiple repository calls if needed

**Never** ❌:
- Return HTTP status codes
- Return JSON/API Response directly
- Make HTTP calls (this is server-side logic)
- Mock in tests — mock repositories only

---

### Repository Layer
**File**: `repository/UserRepository.kt`  
**Annotation**: `@Repository` (implicit via Spring Data JPA)  
**Purpose**: Database access only

**Pattern**:
```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    // Extends JpaRepository — Spring generates CRUD automatically
    
    // Add custom queries only if needed
    fun findByEmail(email: String): User?
}
```

**Rules** ✅:
- Extend `JpaRepository<Entity, ID>` (auto-generates findAll, save, delete, etc.)
- Add custom methods for domain-specific queries
- Use Spring's query generation (`findByEmail` → SQL WHERE clause)
- Return entity types, never DTOs

**Never** ❌:
- Business logic in queries
- Manual SQL (use JPQL or query methods)
- HTTP concerns
- Direct `@Autowired` — use constructor injection

---

### Entity Layer
**File**: `entity/User.kt`  
**Annotation**: `@Entity`  
**Purpose**: Database model persistence

**Pattern**:
```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val name: String,
    val email: String,
    val age: Int,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

**Rules** ✅:
- `@Entity` marks as JPA entity
- `@Id` for primary key, `@GeneratedValue` for auto-increment
- `@Column` for custom column names
- Use nullable types (`?`) for optional fields
- Use `data class` for auto-generated equals/hashCode

**Never** ❌:
- business logic in entities
- transient fields without `@Transient`
- Circular references (causes N+1 queries)
- Logic in getters/setters

---

### DTO Layer
**File**: `dto/UserDTO.kt`  
**Purpose**: API contracts for serialization

**Pattern**:
```kotlin
// Request DTO
data class CreateUserRequest(
    @NotBlank(message = "Name cannot be empty")
    val name: String,
    
    @Email(message = "Email should be valid")
    val email: String,
    
    @Min(1)
    @Max(150)
    val age: Int
)

// Response DTO (matches entity, adds id + timestamps)
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// Mapper (in service or extension function)
fun User.toResponse() = UserResponse(
    id = this.id,
    name = this.name,
    email = this.email,
    age = this.age,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
```

**Rules** ✅:
- Separate request and response DTOs (different shapes)
- Request DTOs include validation (`@NotBlank`, `@Email`, `@Min`, `@Max`)
- Response DTOs match entity fields (may be subset)
- Create mappers (`toResponse()`, `toEntity()`) as extension functions
- DTOs go to controller only — service/repo use entities

**Never** ❌:
- Use entity directly as response (bypasses DTO contract)
- Validation in entity (entity for persistence only)
- Bidirectional relationships in DTOs (JSON cycles)

---

### Exception Layer
**File**: `exception/CustomExceptions.kt` + `exception/GlobalExceptionHandler.kt`

**Pattern**:
```kotlin
// CustomExceptions.kt
open class AppException(message: String) : Exception(message)
class ResourceNotFoundException(message: String) : AppException(message)
class ValidationException(message: String) : AppException(message)
class DuplicateResourceException(message: String) : AppException(message)

// GlobalExceptionHandler.kt
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException):
        ResponseEntity<ApiResponse<Any>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(success = false, message = ex.message ?: "Not found"))
    }
}
```

**Rules** ✅:
- Extend `AppException` for custom exceptions
- Throw from service layer
- `@RestControllerAdvice` catches globally, converts to HTTP response
- Map exception type → HTTP status code (404, 409, 400, 500)
- Always wrap response in `ApiResponse<T>`

**Never** ❌:
- Catch and ignore exceptions
- Mix exception handling in controller (use global handler)
- Generic Exception — use custom types for clarity
- Return exceptions in response body directly

---

## CODE MAP

| Symbol | Type | File | LOC | Role |
|--------|------|------|-----|------|
| `UserController` | class | controller/UserController.kt | 164 | REST endpoints |
| `UserService` | class | service/UserService.kt | 81 | Business logic |
| `UserRepository` | interface | repository/UserRepository.kt | 12 | DB access |
| `User` | entity | entity/User.kt | 32 | Persistence model |
| `CreateUserRequest` | data class | dto/UserDTO.kt | 24 | Request DTO |
| `UserResponse` | data class | dto/UserDTO.kt | 25 | Response DTO |
| `AppException` | sealed class | exception/CustomExceptions.kt | 7 | Error base |
| `GlobalExceptionHandler` | class | exception/GlobalExceptionHandler.kt | 89 | Global error handler |

---

## DEPENDENCY INJECTION PATTERN

Spring **auto-wires** all `@Service`, `@Repository`, `@Controller` beans. Never use `@Autowired` — use constructor injection:

```kotlin
// ✅ CORRECT
@RestController
class UserController(private val userService: UserService) {
    // Service auto-injected via constructor
}

// ❌ WRONG
@RestController
class UserController {
    @Autowired  // Avoid this
    private lateinit var userService: UserService
}
```

---

## TESTING PATTERN

**Test structure**: `src/test/kotlin/com/example/`

```kotlin
@SpringBootTest
class UserServiceTest {
    
    @MockBean
    private lateinit var userRepository: UserRepository
    
    @InjectMocks
    private lateinit var userService: UserService
    
    @Test
    fun testCreateUser() {
        // 1. Setup mock
        whenever(userRepository.save(any())).thenReturn(User(...))
        
        // 2. Execute
        val response = userService.createUser(CreateUserRequest(...))
        
        // 3. Verify
        assertNotNull(response.id)
    }
}
```

**Rules**:
- Mock repositories, not services (service = what we test)
- Use `@MockBean` for repository mocks
- Use `@InjectMocks` to auto-wire service with mocks
- Test service layer (business logic)
- Controller tests use `MockMvc` for HTTP layer testing

---

## KEY ANNOTATIONS REFERENCE

| Annotation | Layer | Usage |
|------------|-------|-------|
| `@RestController` | Controller | Mark as REST endpoint handler |
| `@Service` | Service | Mark as business logic bean |
| `@Repository` | Repository | Mark as data access bean |
| `@Entity` | Entity | Mark as JPA persistent class |
| `@Transactional` | Service | Manage transaction boundaries |
| `@Valid` | Controller | Trigger validation on request body |
| `@RestControllerAdvice` | Exception | Global exception handler |
| `@ExceptionHandler` | Exception | Handle specific exception type |
| `@GetMapping` | Controller | Map GET request to method |
| `@PostMapping` | Controller | Map POST request to method |
| `@RequestMapping` | Controller | Map path prefix to controller |

---

## CONVENTIONS

1. **One responsibility per class**: Controller handles HTTP. Service handles logic. Repo handles queries.
2. **No business logic in entities**: Keep entities thin — they're persistence objects.
3. **Validation at boundaries**: Validate requests in controller (via `@Valid`), business rules in service.
4. **Always return ApiResponse**: Every endpoint response wrapped in `ApiResponse<T>`.
5. **Errors are exceptions**: Throw custom exceptions, let `@RestControllerAdvice` handle.

---

## WHERE TO FIND PATTERNS

- **API Endpoint Example**: `controller/UserController.kt` → `getAllUsers()` method
- **Business Logic Example**: `service/UserService.kt` → `createUser()` method  
- **Database Query Example**: `repository/UserRepository.kt` → inherits from `JpaRepository`
- **Entity Mapping**: `entity/User.kt` → fields match database columns
- **Request Validation**: `dto/UserDTO.kt` → `CreateUserRequest` with `@NotBlank`, `@Email` etc.
- **Global Error Handling**: `exception/GlobalExceptionHandler.kt` → catches all exceptions, converts to HTTP responses

---

## ANTI-PATTERNS (THIS LAYER)

❌ **DO NOT**:
1. Put business logic in controller
2. Call repository directly from controller (breaks separation)
3. Catch exceptions in controller (use global handler)
4. Use entity directly as API response (breaks DTO contract)
5. Add HTTP concerns to service/repo (breaks testability)
6. Use `@Autowired` field injection (use constructor)
7. Return raw exceptions to client (wrap in ApiResponse)
8. Skip `@Valid` on request bodies (leave validation to Spring)
9. Forget `@Transactional` on service methods that modify data
10. Create circular entity relationships (causes N+1 queries)

---

## NEXT STEPS

To extend this layer:
1. **Add new endpoint**: Create method in `UserController`, add service method in `UserService`
2. **Add database field**: Extend `User` entity, create migration SQL
3. **Add custom query**: Add method to `UserRepository` interface (Spring generates query)
4. **Add error handling**: Create custom exception in `CustomExceptions.kt`, add handler in `GlobalExceptionHandler.kt`
5. **Run tests**: `./gradlew test` (from `spring-boot-api-server/` root)

---

**See Also**: 
- Root `AGENTS.md` — project overview, frameworks, conventions
- `spring-boot-api-server/README.md` — setup instructions, configuration
- `spring-boot-api-server/build.gradle.kts` — dependencies and versions
