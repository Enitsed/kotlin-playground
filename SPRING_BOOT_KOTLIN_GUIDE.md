# Spring Boot with Kotlin for REST API Development - Complete Research Guide

## Table of Contents
1. [Setting Up Spring Boot with Kotlin](#1-setting-up-spring-boot-with-kotlin)
2. [REST Controller Patterns](#2-rest-controller-patterns)
3. [JSON Handling](#3-json-handling)
4. [Project Structure Best Practices](#4-project-structure-best-practices)
5. [Spring Boot vs Express.js/NestJS](#5-spring-boot-vs-expressjs-nestjs)
6. [Complete Code Examples](#6-complete-code-examples)

---

## 1. Setting Up Spring Boot with Kotlin

### 1.1 Methods to Create a Project

#### Option A: Using Spring Initializr Web UI
Visit `https://start.spring.io` and:
1. Select Gradle (Kotlin DSL) or Maven
2. Choose Language: **Kotlin**
3. Select dependencies:
   - Spring Web
   - Spring Data JPA (for database access)
   - H2 Database (development)
   - Spring Boot DevTools
   - Any other needed libraries (Spring Security, etc.)

#### Option B: Using Command Line (curl)
```bash
$ mkdir my-api && cd my-api
$ curl https://start.spring.io/starter.zip \
  -d language=kotlin \
  -d type=gradle-project-kotlin \
  -d dependencies=web,data-jpa,h2,devtools \
  -d packageName=com.example.api \
  -d name=MyApi \
  -o my-api.zip
$ unzip my-api.zip
```

#### Option C: Using IntelliJ IDEA
1. File → New → Project
2. Select Spring Initializr
3. Configure with Kotlin, Gradle, and desired dependencies

### 1.2 Build Configuration (Gradle - Kotlin DSL)

**build.gradle.kts**
```kotlin
plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web (includes Spring MVC)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Kotlin Stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Jackson Kotlin support (JSON serialization)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Development tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 1.3 Key Kotlin-Specific Build Considerations

1. **kotlin-spring Plugin**: Automatically opens classes/methods so Spring proxies work
   - Kotlin classes are `final` by default, which conflicts with Spring's CGLIB proxies
   - This plugin makes classes/methods `open` where needed

2. **kotlin-reflect**: Needed for Kotlin reflection, required by Spring

3. **jackson-module-kotlin**: Essential for JSON serialization/deserialization
   - Handles Kotlin data classes
   - Works with single constructors automatically
   - Supports default parameters and named arguments

4. **JSR305 Annotations**: Strict null-safety enforcement

---

## 2. REST Controller Patterns

### 2.1 Basic @RestController Pattern

```kotlin
package com.example.api.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return listOf(
            UserDto(1L, "John Doe", "john@example.com"),
            UserDto(2L, "Jane Smith", "jane@example.com")
        )
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserDto {
        return UserDto(id, "John Doe", "john@example.com")
    }

    @PostMapping
    fun createUser(@RequestBody dto: CreateUserDto): UserDto {
        return UserDto(1L, dto.name, dto.email)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody dto: UpdateUserDto
    ): UserDto {
        return UserDto(id, dto.name, dto.email)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): Map<String, String> {
        return mapOf("message" to "User deleted successfully")
    }
}

// Data Transfer Objects (DTOs)
data class UserDto(
    val id: Long,
    val name: String,
    val email: String
)

data class CreateUserDto(
    val name: String,
    val email: String
)

data class UpdateUserDto(
    val name: String,
    val email: String
)
```

### 2.2 Query Parameters and Validation

```kotlin
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.*

@RestController
@RequestMapping("/api/products")
class ProductController {

    // Query parameters with @RequestParam
    @GetMapping
    fun searchProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) category: String? = null,
        @RequestParam(required = false) minPrice: Double? = null,
        @RequestParam(required = false) maxPrice: Double? = null
    ): ProductPage {
        return ProductPage(
            page = page,
            size = size,
            category = category,
            minPrice = minPrice,
            maxPrice = maxPrice
        )
    }

    // Request body with validation
    @PostMapping
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ProductDto {
        return ProductDto(
            id = 1L,
            name = request.name,
            price = request.price,
            category = request.category
        )
    }
}

data class ProductPage(
    val page: Int,
    val size: Int,
    val category: String?,
    val minPrice: Double?,
    val maxPrice: Double?
)

data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    val name: String,

    @field:DecimalMin("0.01", message = "Price must be greater than 0")
    val price: Double,

    @field:NotBlank(message = "Category is required")
    val category: String
)

data class ProductDto(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String
)
```

### 2.3 Path Variables and Advanced Routing

```kotlin
@RestController
@RequestMapping("/api/v1")
class AdvancedRoutingController {

    // Single path variable
    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: Long): UserDto {
        return UserDto(userId, "User $userId", "user$userId@example.com")
    }

    // Multiple path variables
    @GetMapping("/users/{userId}/posts/{postId}")
    fun getUserPost(
        @PathVariable userId: Long,
        @PathVariable postId: Long
    ): PostDto {
        return PostDto(postId, "Post Title", "Content here", userId)
    }

    // Optional path variable (using optional or default)
    @GetMapping("/files/{fileId}")
    fun getFile(@PathVariable fileId: String): FileResponse {
        return FileResponse(fileId, "file-name.txt", 1024L)
    }

    // Regex in path variable
    @GetMapping("/items/{itemId:[0-9]+}")
    fun getItemByNumericId(@PathVariable itemId: Long): ItemDto {
        return ItemDto(itemId, "Item Name")
    }
}

data class PostDto(
    val id: Long,
    val title: String,
    val content: String,
    val userId: Long
)

data class FileResponse(
    val id: String,
    val filename: String,
    val size: Long
)

data class ItemDto(
    val id: Long,
    val name: String
)
```

### 2.4 Dependency Injection in Controllers

```kotlin
import org.springframework.stereotype.Service
import org.springframework.stereotype.Repository

// Service layer
@Service
class UserService(private val userRepository: UserRepository) {
    fun getUserById(id: Long): UserDto? {
        return userRepository.findById(id)?.let { 
            UserDto(it.id, it.name, it.email) 
        }
    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { 
            UserDto(it.id, it.name, it.email) 
        }
    }

    fun createUser(name: String, email: String): UserDto {
        val user = UserEntity(name = name, email = email)
        val saved = userRepository.save(user)
        return UserDto(saved.id, saved.name, saved.email)
    }
}

// Repository layer
@Repository
interface UserRepository : JpaRepository<UserEntity, Long>

// JPA Entity
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String
)

// Controller with injected service
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers()
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserDto? {
        return userService.getUserById(id)
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserDto {
        return userService.createUser(request.name, request.email)
    }
}

data class CreateUserRequest(
    val name: String,
    val email: String
)
```

---

## 3. JSON Handling

### 3.1 Automatic JSON Serialization/Deserialization

Spring Boot with Jackson automatically handles JSON conversion:

```kotlin
// These objects are automatically converted to/from JSON

data class Article(
    val id: Long,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: LocalDateTime
)

@RestController
@RequestMapping("/api/articles")
class ArticleController {

    @GetMapping("/{id}")
    fun getArticle(@PathVariable id: Long): Article {
        // Automatically serialized to JSON
        return Article(
            id = id,
            title = "Spring Boot Guide",
            content = "Complete guide to Spring Boot",
            author = "John Doe",
            publishedAt = LocalDateTime.now()
        )
    }

    @PostMapping
    fun createArticle(@RequestBody article: Article): Article {
        // JSON automatically deserialized to Article object
        return article.copy(id = 1L)
    }
}
```

**Output:**
```json
{
  "id": 1,
  "title": "Spring Boot Guide",
  "content": "Complete guide to Spring Boot",
  "author": "John Doe",
  "publishedAt": "2024-02-20T15:30:00"
}
```

### 3.2 Custom JSON Serialization

```kotlin
import com.fasterxml.jackson.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Event(
    val id: Long,

    // Custom JSON property name
    @JsonProperty("event_name")
    val name: String,

    // Ignore during serialization
    @JsonIgnore
    val internalNotes: String = "",

    // Custom date format
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,

    // Custom serializer
    @JsonSerialize(using = PriceSerializer::class)
    val price: Double,

    // Only include if non-null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val description: String? = null
)

// Custom serializer implementation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class PriceSerializer : JsonSerializer<Double>() {
    override fun serialize(
        value: Double,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeString(String.format("$%.2f", value))
    }
}

@RestController
@RequestMapping("/api/events")
class EventController {

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): Event {
        return Event(
            id = id,
            name = "Conference 2024",
            internalNotes = "VIP event",  // Won't appear in JSON
            createdAt = LocalDateTime.now(),
            price = 299.99,
            description = "Annual conference"
        )
    }
}
```

**Output:**
```json
{
  "id": 1,
  "event_name": "Conference 2024",
  "createdAt": "2024-02-20T15:30:00",
  "price": "$299.99",
  "description": "Annual conference"
}
```

### 3.3 Handling Collections and Nested Objects

```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String
)

data class Post(
    val id: Long,
    val title: String,
    val content: String,
    val author: User,  // Nested object
    val tags: List<String>,
    val metadata: Map<String, String>
)

@RestController
@RequestMapping("/api/posts")
class PostController {

    @GetMapping
    fun getAllPosts(): List<Post> {
        return listOf(
            Post(
                id = 1,
                title = "Spring Boot Best Practices",
                content = "...",
                author = User(1, "John Doe", "john@example.com"),
                tags = listOf("spring", "kotlin", "backend"),
                metadata = mapOf(
                    "views" to "1500",
                    "likes" to "250"
                )
            )
        )
    }
}
```

**Output:**
```json
[
  {
    "id": 1,
    "title": "Spring Boot Best Practices",
    "content": "...",
    "author": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "tags": ["spring", "kotlin", "backend"],
    "metadata": {
      "views": "1500",
      "likes": "250"
    }
  }
]
```

### 3.4 Error Response Handling

```kotlin
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)

data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: List<FieldError>
)

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any?
)

@RestController
@RequestMapping("/api/items")
class ItemController(private val itemService: ItemService) {

    @GetMapping("/{id}")
    fun getItem(@PathVariable id: Long): ResponseEntity<ItemDto> {
        return try {
            val item = itemService.getItem(id)
            ResponseEntity.ok(item)
        } catch (e: ItemNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @ExceptionHandler(ItemNotFoundException::class)
    fun handleItemNotFound(
        e: ItemNotFoundException,
        request: jakarta.servlet.http.HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Item Not Found",
            message = e.message ?: "The requested item does not exist",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleValidationError(
        e: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = e.message ?: "Invalid input provided"
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }
}

class ItemNotFoundException(message: String) : Exception(message)
class ItemService {
    fun getItem(id: Long): ItemDto {
        if (id <= 0) throw ItemNotFoundException("Item with ID $id not found")
        return ItemDto(id, "Item $id")
    }
}

data class ItemDto(val id: Long, val name: String)
```

---

## 4. Project Structure Best Practices

### 4.1 Standard Spring Boot Project Layout

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/example/api/
│   │       ├── ApiApplication.kt          # Main application class
│   │       ├── config/
│   │       │   ├── WebConfig.kt           # Web configuration
│   │       │   └── SecurityConfig.kt      # Security configuration
│   │       ├── controller/
│   │       │   ├── UserController.kt
│   │       │   ├── ProductController.kt
│   │       │   └── GlobalExceptionHandler.kt
│   │       ├── service/
│   │       │   ├── UserService.kt
│   │       │   ├── ProductService.kt
│   │       │   └── interface/
│   │       │       └── IUserService.kt
│   │       ├── repository/
│   │       │   ├── UserRepository.kt
│   │       │   └── ProductRepository.kt
│   │       ├── entity/
│   │       │   ├── UserEntity.kt
│   │       │   └── ProductEntity.kt
│   │       ├── dto/
│   │       │   ├── UserDto.kt
│   │       │   ├── ProductDto.kt
│   │       │   └── requests/
│   │       │       └── CreateUserRequest.kt
│   │       ├── util/
│   │       │   └── Extensions.kt
│   │       └── exception/
│   │           ├── CustomException.kt
│   │           └── GlobalExceptionHandler.kt
│   └── resources/
│       ├── application.yml                 # Main configuration
│       ├── application-dev.yml             # Development profile
│       ├── application-prod.yml            # Production profile
│       ├── db/
│       │   └── migration/
│       │       ├── V1__initial_schema.sql
│       │       └── V2__add_users_table.sql
│       └── static/
│           └── (CSS, JavaScript, etc.)
└── test/
    └── kotlin/
        └── com/example/api/
            ├── controller/
            │   └── UserControllerTest.kt
            ├── service/
            │   └── UserServiceTest.kt
            └── repository/
                └── UserRepositoryTest.kt
```

### 4.2 Layer Responsibilities

**Controller Layer (Presentation)**
- Handles HTTP requests/responses
- Request validation
- Maps HTTP concepts to domain models

**Service Layer (Business Logic)**
- Contains business logic
- Transaction management
- Orchestrates repositories

**Repository Layer (Data Access)**
- Database CRUD operations
- Custom queries
- Transaction context

**Entity Layer (Domain Model)**
- JPA entities
- Database mapping

**DTO Layer (Data Transfer)**
- Request/Response objects
- Different from entities (avoid exposing internal structure)

### 4.3 Configuration Best Practices

**application.yml**
```yaml
spring:
  application:
    name: my-api
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
        use_sql_comments: true
    show-sql: false
  
  h2:
    console:
      enabled: true
  
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null

server:
  servlet:
    context-path: /api
  error:
    include-stacktrace: never
    include-message: always
    include-binding-errors: always

logging:
  level:
    root: INFO
    com.example.api: DEBUG
    org.springframework.web: DEBUG
```

### 4.4 Main Application Class

```kotlin
package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
```

---

## 5. Spring Boot vs Express.js/NestJS

### 5.1 Architecture Comparison

| Aspect | Spring Boot | Express.js | NestJS |
|--------|-------------|-----------|--------|
| **Language** | Kotlin/Java | JavaScript | TypeScript |
| **Paradigm** | Full OOP, DI Container | Minimal, Flexible | Full OOP, DI Container |
| **Routing** | Annotations | Express methods | Decorators |
| **Middleware** | Servlet Filters, Interceptors | Functions | Decorators |
| **Database** | ORM (JPA/Hibernate) | Multiple options | TypeORM/Prisma |
| **Type Safety** | Compile-time (Kotlin) | Runtime only | Compile-time |
| **Learning Curve** | Moderate-Steep | Shallow | Moderate |
| **Performance** | Very high (compiled JVM) | Good (V8 optimized) | Good (compiled TypeScript) |
| **Enterprise Ready** | Excellent | Limited | Good |
| **Community Size** | Very large | Largest | Growing |

### 5.2 Route Definition Comparison

**Spring Boot (Kotlin)**
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserDto {
        return service.getUser(id)
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserDto {
        return service.create(request)
    }
}
```

**Express.js**
```javascript
const express = require('express');
const app = express();

app.get('/api/users/:id', async (req, res) => {
    try {
        const user = await userService.getUser(req.params.id);
        res.json(user);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/users', async (req, res) => {
    try {
        const user = await userService.create(req.body);
        res.json(user);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});
```

**NestJS**
```typescript
@Controller('api/users')
export class UserController {
    constructor(private readonly userService: UserService) {}

    @Get(':id')
    async getUser(@Param('id') id: number): Promise<UserDto> {
        return this.userService.getUser(id);
    }

    @Post()
    async createUser(@Body() request: CreateUserRequest): Promise<UserDto> {
        return this.userService.create(request);
    }
}
```

### 5.3 Dependency Injection Comparison

**Spring Boot (Kotlin) - Constructor Injection**
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val config: AppConfig
) {
    fun createUser(request: CreateUserRequest) {
        // DI handled automatically by Spring
        val user = userRepository.save(request.toEntity())
        emailService.sendWelcomeEmail(user)
    }
}
```

**Express.js - Manual DI**
```javascript
class UserService {
    constructor(userRepository, emailService, config) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.config = config;
    }

    async createUser(request) {
        const user = await this.userRepository.save(request);
        await this.emailService.sendWelcomeEmail(user);
    }
}

// Manual instantiation
const userService = new UserService(
    new UserRepository(),
    new EmailService(),
    config
);
```

**NestJS - Constructor Injection (similar to Spring)**
```typescript
@Injectable()
export class UserService {
    constructor(
        private readonly userRepository: UserRepository,
        private readonly emailService: EmailService,
        private readonly config: AppConfig
    ) {}

    async createUser(request: CreateUserRequest) {
        const user = await this.userRepository.save(request);
        await this.emailService.sendWelcomeEmail(user);
    }
}
```

### 5.4 Request Validation Comparison

**Spring Boot (Kotlin)**
```kotlin
import jakarta.validation.constraints.*

data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:Email(message = "Email must be valid")
    val email: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
)

@RestController
class UserController {
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        // Validated automatically, 400 Bad Request on error
    }
}
```

**Express.js - Manual Validation**
```javascript
const { body, validationResult } = require('express-validator');

app.post('/api/users',
    body('name').notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Email must be valid'),
    body('password').isLength({ min: 8 }).withMessage('Password must be at least 8 characters'),
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }
        // Handle request
    }
);
```

**NestJS - Class Validator (similar to Spring)**
```typescript
import { IsEmail, IsNotEmpty, MinLength } from 'class-validator';

export class CreateUserRequest {
    @IsNotEmpty({ message: 'Name is required' })
    name: string;

    @IsEmail({}, { message: 'Email must be valid' })
    email: string;

    @MinLength(8, { message: 'Password must be at least 8 characters' })
    password: string;
}

@Controller('users')
export class UserController {
    @Post()
    @UsePipes(new ValidationPipe())
    async createUser(@Body() request: CreateUserRequest): Promise<UserDto> {
        // Validated automatically
    }
}
```

### 5.5 Error Handling Comparison

**Spring Boot (Kotlin)**
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        e: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "User Not Found",
                message = e.message ?: "User does not exist",
                path = request.requestURI
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "An unexpected error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
```

**Express.js - Error Middleware**
```javascript
function errorHandler(error, req, res, next) {
    if (error instanceof UserNotFoundException) {
        return res.status(404).json({
            status: 404,
            error: 'User Not Found',
            message: error.message,
            path: req.originalUrl
        });
    }

    res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: 'An unexpected error occurred'
    });
}

app.use(errorHandler);
```

**NestJS - Global Exception Filter**
```typescript
@Catch(UserNotFoundException)
export class UserNotFoundExceptionFilter implements ExceptionFilter {
    catch(exception: UserNotFoundException, host: ArgumentsHost) {
        const ctx = host.switchToHttp();
        const response = ctx.getResponse<Response>();
        const request = ctx.getRequest<Request>();

        response.status(HttpStatus.NOT_FOUND).json({
            status: HttpStatus.NOT_FOUND,
            error: 'User Not Found',
            message: exception.message,
            path: request.url
        });
    }
}

@UseFilters(UserNotFoundExceptionFilter)
@Controller('users')
export class UserController {
    // ...
}
```

### 5.6 Summary: When to Use What

**Use Spring Boot with Kotlin when:**
- Building enterprise applications
- You need high performance and type safety
- Complex business logic requiring OOP
- Large teams with Java background
- Need advanced ORM/database features
- Long-term maintenance is priority

**Use Express.js when:**
- Building simple APIs quickly
- You already have Node.js infrastructure
- Minimal dependencies preferred
- Prototyping and rapid development
- Full-stack JavaScript desired

**Use NestJS when:**
- Want Node.js with Spring-like structure
- Need TypeScript type safety
- Building scalable Node applications
- Want a more opinionated framework
- Team knows or wants to learn TypeScript

---

## 6. Complete Code Examples

### 6.1 Complete REST API Example: Blog API

#### File: `ApiApplication.kt`
```kotlin
package com.example.blog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
```

#### File: `entity/BlogEntity.kt`
```kotlin
package com.example.blog.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "posts")
data class PostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, length = 200)
    val title: String,

    @Column(nullable = false, length = 500)
    val excerpt: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false, name = "author_id")
    val authorId: Long,

    @Column(nullable = false, unique = true)
    val slug: String,

    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "authors")
data class AuthorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val email: String
)
```

#### File: `dto/BlogDto.kt`
```kotlin
package com.example.blog.dto

import java.time.LocalDateTime

data class PostDto(
    val id: Long,
    val title: String,
    val excerpt: String,
    val content: String,
    val author: AuthorDto,
    val slug: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AuthorDto(
    val id: Long,
    val username: String,
    val name: String,
    val email: String
)

data class CreatePostRequest(
    val title: String,
    val excerpt: String,
    val content: String,
    val authorId: Long
)

data class UpdatePostRequest(
    val title: String?,
    val excerpt: String?,
    val content: String?
)
```

#### File: `repository/BlogRepository.kt`
```kotlin
package com.example.blog.repository

import com.example.blog.entity.PostEntity
import com.example.blog.entity.AuthorEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<PostEntity, Long> {
    fun findBySlug(slug: String): PostEntity?
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<PostEntity>
}

@Repository
interface AuthorRepository : JpaRepository<AuthorEntity, Long> {
    fun findByUsername(username: String): AuthorEntity?
}
```

#### File: `service/BlogService.kt`
```kotlin
package com.example.blog.service

import com.example.blog.entity.PostEntity
import com.example.blog.entity.AuthorEntity
import com.example.blog.repository.PostRepository
import com.example.blog.repository.AuthorRepository
import com.example.blog.dto.PostDto
import com.example.blog.dto.AuthorDto
import com.example.blog.dto.CreatePostRequest
import com.example.blog.dto.UpdatePostRequest
import com.example.blog.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostService(
    private val postRepository: PostRepository,
    private val authorRepository: AuthorRepository
) {

    fun getAllPosts(pageable: Pageable): Page<PostDto> {
        val posts = postRepository.findAll(pageable)
        return PageImpl(
            posts.content.map { it.toDto() },
            pageable,
            posts.totalElements
        )
    }

    fun getPostBySlug(slug: String): PostDto {
        val post = postRepository.findBySlug(slug)
            ?: throw ResourceNotFoundException("Post not found with slug: $slug")
        return post.toDto()
    }

    fun getPostById(id: Long): PostDto {
        val post = postRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Post not found with id: $id") }
        return post.toDto()
    }

    fun createPost(request: CreatePostRequest): PostDto {
        val author = authorRepository.findById(request.authorId)
            .orElseThrow { ResourceNotFoundException("Author not found") }

        val slug = request.title.toSlug()
        if (postRepository.findBySlug(slug) != null) {
            throw IllegalArgumentException("A post with this slug already exists")
        }

        val post = PostEntity(
            title = request.title,
            excerpt = request.excerpt,
            content = request.content,
            authorId = author.id,
            slug = slug
        )

        val saved = postRepository.save(post)
        return saved.toDto()
    }

    fun updatePost(id: Long, request: UpdatePostRequest): PostDto {
        val post = postRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        val updated = post.copy(
            title = request.title ?: post.title,
            excerpt = request.excerpt ?: post.excerpt,
            content = request.content ?: post.content,
            updatedAt = LocalDateTime.now()
        )

        val saved = postRepository.save(updated)
        return saved.toDto()
    }

    fun deletePost(id: Long) {
        if (!postRepository.existsById(id)) {
            throw ResourceNotFoundException("Post not found")
        }
        postRepository.deleteById(id)
    }

    private fun PostEntity.toDto(): PostDto {
        val author = authorRepository.findById(authorId)
            .orElseThrow { ResourceNotFoundException("Author not found") }

        return PostDto(
            id = id,
            title = title,
            excerpt = excerpt,
            content = content,
            author = author.toDto(),
            slug = slug,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AuthorEntity.toDto() = AuthorDto(
        id = id,
        username = username,
        name = name,
        email = email
    )

    private fun String.toSlug(): String = this
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}
```

#### File: `controller/PostController.kt`
```kotlin
package com.example.blog.controller

import com.example.blog.service.PostService
import com.example.blog.dto.PostDto
import com.example.blog.dto.CreatePostRequest
import com.example.blog.dto.UpdatePostRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
@CrossOrigin(origins = ["http://localhost:3000"])
class PostController(private val postService: PostService) {

    @GetMapping
    fun getAllPosts(pageable: Pageable): ResponseEntity<Page<PostDto>> {
        val posts = postService.getAllPosts(pageable)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/slug/{slug}")
    fun getPostBySlug(@PathVariable slug: String): ResponseEntity<PostDto> {
        val post = postService.getPostBySlug(slug)
        return ResponseEntity.ok(post)
    }

    @GetMapping("/{id}")
    fun getPostById(@PathVariable id: Long): ResponseEntity<PostDto> {
        val post = postService.getPostById(id)
        return ResponseEntity.ok(post)
    }

    @PostMapping
    fun createPost(@RequestBody request: CreatePostRequest): ResponseEntity<PostDto> {
        val post = postService.createPost(request)
        return ResponseEntity(post, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updatePost(
        @PathVariable id: Long,
        @RequestBody request: UpdatePostRequest
    ): ResponseEntity<PostDto> {
        val post = postService.updatePost(id, request)
        return ResponseEntity.ok(post)
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<Void> {
        postService.deletePost(id)
        return ResponseEntity.noContent().build()
    }
}
```

#### File: `exception/CustomException.kt`
```kotlin
package com.example.blog.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)
```

#### File: `exception/GlobalExceptionHandler.kt`
```kotlin
package com.example.blog.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import jakarta.servlet.http.HttpServletRequest

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        e: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = e.message ?: "Resource not found",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        e: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Invalid argument provided",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.requestURI
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
```

#### File: `application.yml`
```yaml
spring:
  application:
    name: blog-api
  
  datasource:
    url: jdbc:h2:mem:blogdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
    show-sql: false
  
  h2:
    console:
      enabled: true
  
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true

server:
  servlet:
    context-path: /
  port: 8080

logging:
  level:
    root: INFO
    com.example.blog: DEBUG
```

### 6.2 Testing Example

#### File: `test/PostControllerTest.kt`
```kotlin
package com.example.blog.controller

import com.example.blog.service.PostService
import com.example.blog.dto.PostDto
import com.example.blog.dto.AuthorDto
import com.example.blog.dto.CreatePostRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(PostController::class)
class PostControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {

    @MockBean
    lateinit var postService: PostService

    @Test
    fun `should get all posts`() {
        val author = AuthorDto(1, "john", "John Doe", "john@example.com")
        val post = PostDto(
            id = 1,
            title = "Test Post",
            excerpt = "Excerpt",
            content = "Content",
            author = author,
            slug = "test-post",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        mockMvc.perform(get("/api/v1/posts"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should create a post`() {
        val request = CreatePostRequest(
            title = "New Post",
            excerpt = "New excerpt",
            content = "New content",
            authorId = 1
        )

        mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
    }
}
```

---

## Key Takeaways

1. **Spring Boot with Kotlin** combines strong type safety, null-safety, and modern language features with an opinionated, production-ready framework.

2. **Setup is streamlined** - Spring Initializr and auto-configuration handle most boilerplate.

3. **JSON handling is automatic** - Jackson integration means DTOs become JSON with minimal configuration.

4. **Project structure** follows clear layer separation (controller → service → repository) for maintainability.

5. **Spring Boot differs from Express/NestJS** in maturity, scalability, and enterprise focus, making it ideal for large systems.

6. **For developers from Node.js**, NestJS provides a gentler transition with Spring-like patterns in Node.

7. **Kotlin advantages** include null-safety, extension functions, data classes, and cleaner syntax compared to Java.

