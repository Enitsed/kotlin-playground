# Spring Boot with Kotlin - Quick Reference Guide

## Project Setup (2 minutes)

### Using Spring Initializr
```bash
curl https://start.spring.io/starter.zip \
  -d language=kotlin \
  -d type=gradle-project-kotlin \
  -d dependencies=web,data-jpa,h2,devtools \
  -d packageName=com.example.api \
  -o my-api.zip && unzip my-api.zip
```

### Essential Dependencies (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

## Controllers Cheat Sheet

### Basic REST Controller
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {
    
    @GetMapping                         // GET /api/users
    fun getAll(): List<UserDto>
    
    @GetMapping("/{id}")                // GET /api/users/1
    fun getById(@PathVariable id: Long): UserDto
    
    @PostMapping                        // POST /api/users
    fun create(@RequestBody dto: CreateUserDto): UserDto
    
    @PutMapping("/{id}")                // PUT /api/users/1
    fun update(@PathVariable id: Long, @RequestBody dto: UpdateUserDto): UserDto
    
    @DeleteMapping("/{id}")             // DELETE /api/users/1
    fun delete(@PathVariable id: Long): ResponseEntity<Void>
    
    @PatchMapping("/{id}")              // PATCH /api/users/1
    fun patch(@PathVariable id: Long, @RequestBody dto: UpdateUserDto): UserDto
}
```

### Request Parameters
```kotlin
@GetMapping
fun search(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(required = false) name: String?,
    @RequestParam name: String = "defaultValue"
): List<UserDto>

// GET /api/users?page=2&name=john
```

### Path Variables
```kotlin
@GetMapping("/{userId}/posts/{postId}")
fun getUserPost(
    @PathVariable userId: Long,
    @PathVariable postId: Long
): PostDto

// GET /api/users/1/posts/5
```

### Request Body
```kotlin
@PostMapping
fun create(@Valid @RequestBody request: CreateUserRequest): UserDto

// Automatically validates and deserializes JSON
```

### Response Status
```kotlin
@PostMapping
fun create(@RequestBody request: CreateUserDto): ResponseEntity<UserDto> {
    val user = service.create(request)
    return ResponseEntity(user, HttpStatus.CREATED)  // 201
}

@DeleteMapping("/{id}")
fun delete(@PathVariable id: Long): ResponseEntity<Void> {
    service.delete(id)
    return ResponseEntity.noContent().build()  // 204
}
```

---

## DTOs and Data Classes

### Simple DTO
```kotlin
data class UserDto(
    val id: Long,
    val name: String,
    val email: String
)
```

### With Validation
```kotlin
import jakarta.validation.constraints.*

data class CreateUserRequest(
    @field:NotBlank(message = "Name required")
    val name: String,
    
    @field:Email(message = "Invalid email")
    val email: String,
    
    @field:Size(min = 8, max = 20)
    val password: String,
    
    @field:Min(18)
    val age: Int
)
```

### With JSON Customization
```kotlin
import com.fasterxml.jackson.annotation.*

data class Event(
    val id: Long,
    
    @JsonProperty("event_name")      // Custom JSON name
    val name: String,
    
    @JsonIgnore                      // Hide from JSON
    val internalId: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    
    @JsonInclude(NON_NULL)           // Include only if not null
    val description: String?
)
```

---

## Service and Repository Layer

### Service (Business Logic)
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    
    fun getUser(id: Long): UserDto {
        val entity = userRepository.findById(id)
            .orElseThrow { NotFoundException("User not found") }
        return entity.toDto()
    }
    
    fun createUser(request: CreateUserRequest): UserDto {
        val entity = UserEntity(
            name = request.name,
            email = request.email
        )
        val saved = userRepository.save(entity)
        emailService.sendWelcomeEmail(saved.email)
        return saved.toDto()
    }
}
```

### Repository
```kotlin
@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun findAllByNameContainingIgnoreCase(name: String): List<UserEntity>
}
```

### JPA Entity
```kotlin
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(columnDefinition = "TEXT")
    val bio: String? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

---

## Error Handling

### Global Exception Handler
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(404, "Not Found", e.message ?: "Resource not found"),
            HttpStatus.NOT_FOUND
        )
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(500, "Internal Error", "An error occurred"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### Custom Exception
```kotlin
class NotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
```

---

## Configuration (application.yml)

```yaml
spring:
  application:
    name: my-api
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  
  jackson:
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    root: INFO
    com.example.api: DEBUG
```

---

## JSON Examples

### Request
```bash
POST /api/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Response
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Collection with Pagination
```json
{
  "content": [
    { "id": 1, "name": "User 1" },
    { "id": 2, "name": "User 2" }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0
}
```

### Error Response
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User with ID 999 not found",
  "timestamp": "2024-02-20T15:30:00"
}
```

---

## Useful Annotations

| Annotation | Purpose |
|-----------|---------|
| `@RestController` | REST endpoint class |
| `@RequestMapping` | Base URL for controller |
| `@GetMapping`, `@PostMapping`, etc. | HTTP method mappings |
| `@PathVariable` | Extract from URL path |
| `@RequestParam` | Query string parameter |
| `@RequestBody` | JSON request body |
| `@ResponseBody` | JSON response |
| `@Service` | Business logic bean |
| `@Repository` | Data access bean |
| `@Configuration` | Configuration bean |
| `@Autowired` | Dependency injection |
| `@Valid` | Validate input |
| `@Transactional` | Database transaction |
| `@Entity` | JPA entity |
| `@Table`, `@Column` | Database mapping |
| `@Id`, `@GeneratedValue` | Primary key |
| `@ExceptionHandler` | Handle exceptions |
| `@RestControllerAdvice` | Global exception handler |
| `@CrossOrigin` | Allow CORS |

---

## Common Patterns

### Create and Return
```kotlin
@PostMapping
fun create(@Valid @RequestBody request: CreateDto): ResponseEntity<ResultDto> {
    val result = service.create(request)
    return ResponseEntity(result, HttpStatus.CREATED)
}
```

### Get with ID
```kotlin
@GetMapping("/{id}")
fun getById(@PathVariable id: Long): ResponseEntity<ResultDto> {
    val result = service.getById(id)
        ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(result)
}
```

### Update
```kotlin
@PutMapping("/{id}")
fun update(
    @PathVariable id: Long,
    @Valid @RequestBody request: UpdateDto
): ResponseEntity<ResultDto> {
    val result = service.update(id, request)
    return ResponseEntity.ok(result)
}
```

### Delete
```kotlin
@DeleteMapping("/{id}")
fun delete(@PathVariable id: Long): ResponseEntity<Void> {
    service.delete(id)
    return ResponseEntity.noContent().build()
}
```

### List with Pagination
```kotlin
@GetMapping
fun list(pageable: Pageable): ResponseEntity<Page<ResultDto>> {
    val results = service.list(pageable)
    return ResponseEntity.ok(results)
}

// Usage: GET /api/items?page=0&size=20&sort=name,asc
```

---

## Testing Snippet

```kotlin
@WebMvcTest(UserController::class)
class UserControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {
    
    @MockBean
    lateinit var service: UserService
    
    @Test
    fun `get user returns 200 ok`() {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }
    
    @Test
    fun `create user returns 201`() {
        val request = CreateUserRequest("John", "john@example.com")
        
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
    }
}
```

---

## Environment Profiles

### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
  jpa:
    show-sql: true

logging:
  level:
    root: DEBUG
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/mydb
    username: ${DB_USER}
    password: ${DB_PASSWORD}

logging:
  level:
    root: WARN
```

Run with: `java -jar app.jar --spring.profiles.active=prod`

---

## Kotlin Advantages in Spring Boot

| Feature | Benefit |
|---------|---------|
| Null safety | Compile-time null checks |
| Data classes | Automatic equals, hashCode, toString |
| Default parameters | Cleaner API design |
| Extension functions | Add methods to existing classes |
| Scope functions | `.apply`, `.let`, `.run` for functional style |
| String templates | `"Hello $name"` interpolation |
| Type inference | Less verbose than Java |
| No checked exceptions | Cleaner error handling |

---

## Running Your App

```bash
# Development
./gradlew bootRun

# Build JAR
./gradlew build

# Run JAR
java -jar build/libs/app-0.0.1-SNAPSHOT.jar

# Run with profile
java -jar build/libs/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Debug mode
java -jar build/libs/app-0.0.1-SNAPSHOT.jar --debug
```

---

## IDE Tips (IntelliJ)

- `Cmd+N` (Mac) or `Ctrl+N` (Windows/Linux) - New class/file
- `Cmd+Shift+T` (Mac) - Create test
- `Cmd+P` (Mac) - Show function parameters
- `Cmd+B` (Mac) - Go to definition
- `Cmd+U` (Mac) - Go to super method
- Enable "Kotlin Compiler" plugin for Spring support

---

## Resources

- Official: https://spring.io/projects/spring-boot
- Kotlin: https://kotlinlang.org/docs/spring.html
- Reference: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Initializr: https://start.spring.io
- Community: https://stackoverflow.com/questions/tagged/spring-boot+kotlin

