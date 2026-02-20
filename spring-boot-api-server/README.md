# Spring Boot Users API

A production-grade REST API server built with Spring Boot, Kotlin, and MySQL integration.

## Quick Start

### Prerequisites
- Java 17+
- Gradle (or use `./gradlew`)
- MySQL 8.0 (running on localhost:3306)
  - User: root
  - Password: 1234

### Option 1: Using Docker Compose (Recommended)

Start MySQL with Docker Compose:

```bash
docker-compose up -d
```

This will:
- Start MySQL 8.0 container on port 3306
- Create `users_db` and `users_db_dev` databases
- Load sample data from `init.sql`

Verify MySQL is running:
```bash
docker ps | grep mysql
```

### Option 2: Manual MySQL Setup

If MySQL is already running locally:
```bash
mysql -u root -p1234 -e "CREATE DATABASE IF NOT EXISTS users_db;"
mysql -u root -p1234 users_db < init.sql
```

### Build

```bash
./gradlew build
```

### Run

```bash
# Default (uses application.yaml)
./gradlew bootRun

# Development profile (shows SQL queries)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The API will be available at `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Get All Users
```bash
curl http://localhost:8080/api/v1/users
```

### Get User by ID
```bash
curl http://localhost:8080/api/v1/users/1
```

### Create User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "age": 25
  }'
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "age": 31
  }'
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

### Health Check
```bash
curl http://localhost:8080/api/v1/users/health
```

## Response Format

All responses follow a consistent format:

**Success Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30,
    "createdAt": "2024-02-20T10:15:30",
    "updatedAt": "2024-02-20T10:15:30"
  },
  "message": "Operation successful",
  "timestamp": "2024-02-20T10:15:30"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "User with email already exists",
  "timestamp": "2024-02-20T10:15:30"
}
```

**Validation Error Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "name: Name cannot be empty",
    "email: Email should be valid",
    "age: Age must be between 1 and 150"
  ],
  "timestamp": "2024-02-20T10:15:30"
}
```

## Docker

### Build Image
```bash
docker build -t spring-boot-users-api .
```

### Run Container
```bash
docker run -p 8080:8080 \
  --network spring-network \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/users_db" \
  spring-boot-users-api
```

## Project Structure

```
spring-boot-api-server/
├── src/main/kotlin/com/example/
│   ├── Application.kt              # Main Spring Boot application
│   ├── controller/
│   │   └── UserController.kt       # REST endpoints
│   ├── service/
│   │   └── UserService.kt          # Business logic
│   ├── repository/
│   │   └── UserRepository.kt       # Database access
│   ├── entity/
│   │   └── User.kt                 # JPA entity
│   ├── dto/
│   │   └── UserDTO.kt              # DTOs and API responses
│   └── exception/
│       ├── CustomExceptions.kt     # Custom exception classes
│       └── GlobalExceptionHandler.kt # Global error handler
├── src/main/resources/
│   ├── application.yaml            # Default configuration
│   ├── application-dev.yaml        # Development configuration
│   └── application-prod.yaml       # Production configuration
├── build.gradle.kts                # Gradle build configuration
├── Dockerfile                      # Docker configuration
├── docker-compose.yml              # Docker Compose for MySQL
├── init.sql                        # Database initialization
└── README.md                       # This file
```

## Key Features

- **Spring Boot 3.1.7**: Latest Spring Boot with Kotlin support
- **JPA/Hibernate**: ORM for database operations
- **Validation**: Bean Validation for input validation
- **Global Exception Handling**: Centralized error handling with @RestControllerAdvice
- **Swagger/OpenAPI**: Auto-generated API documentation
- **Configuration Profiles**: dev, prod profiles for different environments
- **MySQL Integration**: Full database integration with connection pooling
- **Structured DTOs**: Separate DTOs for requests, responses, and entities
- **Repository Pattern**: Spring Data JPA for clean data access

## Learning Points

This example demonstrates:

1. **Spring Boot Architecture**: How Spring Boot projects are structured
2. **REST Controllers**: Creating REST endpoints with @RestController
3. **Service Layer**: Business logic separation with @Service
4. **Repository Pattern**: Using Spring Data JPA
5. **JPA Entities**: Mapping objects to database tables
6. **Validation**: Using Jakarta validation annotations
7. **Global Exception Handling**: Handling errors consistently across the application
8. **DTOs**: Separating entity models from API contracts
9. **Dependency Injection**: Spring's automatic dependency management
10. **Swagger Integration**: Auto-generating API documentation

## Comparison with Express.js/NestJS

| Concept | Express.js | NestJS | Spring Boot |
|---------|-----------|--------|------------|
| **Routing** | `app.get()` | `@Get()` decorator | `@GetMapping()` |
| **Controllers** | Callback functions | `@Injectable()` classes | `@RestController` classes |
| **Services** | Manual separation | `@Injectable()` services | `@Service` classes |
| **ORM** | TypeORM/Sequelize | TypeORM | Hibernate/JPA |
| **Validation** | `class-validator` | `class-validator` | Jakarta validation |
| **Error Handling** | Middleware | Exception filters | `@ControllerAdvice` |
| **Dependency Injection** | Manual/decorators | Automatic | Automatic |
| **Documentation** | Swagger plugin | Swagger decorator | Springdoc OpenAPI |

## Configuration Files

### application.yaml (Default)
- Uses local MySQL at `localhost:3306`
- Database: `users_db`
- Hibernate ddl-auto: `update` (auto-creates tables)
- Logging level: INFO

### application-dev.yaml (Development)
- Uses local MySQL at `localhost:3306`
- Database: `users_db_dev`
- Hibernate ddl-auto: `update` (auto-creates tables)
- Logging level: DEBUG
- Shows SQL queries

### application-prod.yaml (Production)
- Expects environment variables for database connection
- Hibernate ddl-auto: `validate` (validates schema only)
- Logging level: WARN
- Swagger UI disabled

## Troubleshooting

### MySQL Connection Error
```
Error: Connection refused: localhost:3306
```
Solution: Ensure MySQL is running and accessible at localhost:3306

### Validation Error on Startup
```
Error: Table 'users_db.users' doesn't exist
```
Solution: Run `docker-compose up -d` or execute `init.sql` manually

### Port Already in Use
```
Error: Port 8080 already in use
```
Solution: Change port in application.yaml or kill the process using port 8080

## Tips for Learning

1. Start with the Swagger UI at `/swagger-ui.html` to explore the API
2. Test endpoints manually with curl before building frontend
3. Look at how annotations like `@GetMapping`, `@PostMapping` map to HTTP methods
4. Study the exception handling in `GlobalExceptionHandler.kt`
5. Compare the Spring Boot structure with the Ktor example to see different approaches
6. Try switching between dev and prod profiles to see different behaviors
7. Check the database directly: `mysql -u root -p1234 users_db`

## Running with Different Profiles

```bash
# Development - shows SQL, creates/updates tables
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production - validates schema, minimal logging
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Useful MySQL Commands

```bash
# Connect to MySQL
mysql -u root -p1234

# Use database
USE users_db;

# View users table
SELECT * FROM users;

# View table structure
DESCRIBE users;

# Delete all users
DELETE FROM users;
```
