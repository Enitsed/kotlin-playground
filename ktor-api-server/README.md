# Ktor Users API

A lightweight, async-first REST API server built with Ktor and Kotlin.

## Quick Start

### Prerequisites
- Java 17+
- Gradle (or use `./gradlew`)

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew run
```

The API will be available at `http://localhost:8080`

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
    "name": "Alice",
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
curl http://localhost:8080/api/v1/health
```

## Docker

### Build Image
```bash
docker build -t ktor-api-server .
```

### Run Container
```bash
docker run -p 8080:8080 ktor-api-server
```

## Key Features

- **Lightweight & Fast**: Built on Netty, minimal overhead
- **Async-First**: Uses Kotlin coroutines for concurrent programming
- **JSON Serialization**: kotlinx.serialization for type-safe JSON handling
- **Error Handling**: Comprehensive error responses with proper HTTP status codes
- **Validation**: Input validation for all endpoints
- **In-Memory Storage**: Simple data store for learning purposes

## Project Structure

```
ktor-api-server/
├── src/main/kotlin/
│   └── Application.kt        # Main application with all routes and models
├── src/main/resources/
│   └── application.conf      # Ktor configuration
├── build.gradle.kts          # Gradle build configuration
└── Dockerfile               # Docker configuration
```

## Learning Points

This example demonstrates:

1. **Ktor Routing DSL**: How to define routes in a simple, readable way
2. **Coroutines**: Async request handling using Kotlin coroutines
3. **Serialization**: JSON serialization/deserialization with kotlinx.serialization
4. **Error Handling**: Proper error responses with HTTP status codes
5. **Request Validation**: Input validation before processing
6. **In-Memory Data Store**: Simple CRUD operations on in-memory data
7. **Status Codes**: Using correct HTTP status codes (201 for created, 404 for not found, etc.)

## Comparison with Express.js/NestJS

| Concept | Express.js | NestJS | Ktor |
|---------|-----------|--------|------|
| Routing | `app.get()` | `@Get()` decorator | `get { }` route block |
| Async | Promises/async-await | Observable/async-await | Coroutines/suspend |
| Middleware | `app.use()` | `@Injectable()` middleware | Built-in features |
| Validation | manual or joi | `class-validator` | manual or validation libraries |

## Tips for Learning

1. Start by running the server and testing the endpoints with curl
2. Look at how `CreateUserRequest` and `UpdateUserRequest` are defined - this is similar to DTOs in NestJS
3. Notice how error handling works - compare to Express error middleware
4. The `UserStore` class shows in-memory data management - in production you'd use a real database
5. The routing syntax is very readable - similar to Express but with Kotlin DSL
