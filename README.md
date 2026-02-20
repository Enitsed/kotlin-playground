# Kotlin API Servers - Learning Guide

Welcome! This project contains **two complete REST API server examples** built with Kotlin to help you transition from TypeScript/NestJS/Express to Kotlin/JVM ecosystem.

## 🎯 Project Overview

You'll find two fully-functional API servers:

1. **Ktor API Server** - Lightweight, modern, async-first framework
2. **Spring Boot API Server** - Enterprise-grade, feature-rich framework with MySQL integration

Both implement the same **Users REST API** for easy comparison.

## 📁 Project Structure

```
kotlin-example/
├── ktor-api-server/                 # Ktor example (lightweight)
│   ├── src/main/kotlin/
│   │   └── Application.kt           # All-in-one application file
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── README.md
│   └── gradlew                      # Gradle wrapper
│
├── spring-boot-api-server/          # Spring Boot example (enterprise)
│   ├── src/main/kotlin/com/example/
│   │   ├── Application.kt           # Spring Boot main class
│   │   ├── controller/              # REST endpoints
│   │   ├── service/                 # Business logic
│   │   ├── repository/              # Database access
│   │   ├── entity/                  # Database models
│   │   ├── dto/                     # API request/response objects
│   │   └── exception/               # Error handling
│   ├── src/main/resources/
│   │   ├── application.yaml         # Default config
│   │   ├── application-dev.yaml     # Dev config
│   │   └── application-prod.yaml    # Prod config
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── docker-compose.yml           # MySQL container setup
│   ├── init.sql                     # Database initialization
│   ├── README.md
│   └── gradlew                      # Gradle wrapper
│
└── README.md                        # This file
```

## 🚀 Quick Start Guide

### For Ktor (Simple, Lightweight)

```bash
cd ktor-api-server
./gradlew run
```

- API runs on: `http://localhost:8080`
- In-memory data storage (no database needed)
- ~15 lines of main application code
- Perfect for learning Kotlin fundamentals

### For Spring Boot (Enterprise, Production-Ready)

**Step 1: Start MySQL**
```bash
cd spring-boot-api-server
docker-compose up -d
```

**Step 2: Build and Run**
```bash
./gradlew bootRun
```

- API runs on: `http://localhost:8080`
- MySQL database integration
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Full layered architecture (Controller → Service → Repository → Entity)

## 🔄 API Endpoints (Same for Both Projects)

Both APIs implement the same Users REST API:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users |
| GET | `/api/v1/users/{id}` | Get specific user |
| POST | `/api/v1/users` | Create new user |
| PUT | `/api/v1/users/{id}` | Update user |
| DELETE | `/api/v1/users/{id}` | Delete user |
| GET | `/api/v1/health` | Health check |

### Example: Get All Users

**Ktor:**
```bash
curl http://localhost:8080/api/v1/users
```

**Spring Boot:**
```bash
curl http://localhost:8080/api/v1/users
```

Same endpoint, different implementation!

### Example: Create User

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "age": 25
  }'
```

## 📚 Learning Path

### Week 1: Learn Ktor
- [ ] Read `ktor-api-server/README.md`
- [ ] Run `./gradlew run` and test endpoints
- [ ] Study `Application.kt` - understand the routing DSL
- [ ] Learn Kotlin coroutines basics
- [ ] Compare with your Express.js experience

### Week 2: Learn Spring Boot
- [ ] Start MySQL with `docker-compose up -d`
- [ ] Read `spring-boot-api-server/README.md`
- [ ] Run `./gradlew bootRun`
- [ ] Explore Swagger UI at `/swagger-ui.html`
- [ ] Study the layered architecture (controller → service → repository)
- [ ] Learn Spring annotations (@RestController, @Service, @Repository, etc.)
- [ ] Compare with your NestJS experience

### Week 3: Compare and Deepen
- [ ] Run both servers side-by-side
- [ ] Make the same API calls to both
- [ ] Study the differences in structure
- [ ] Modify code to add new endpoints
- [ ] Experiment with validation and error handling

## 🧭 Framework Comparison

### Ktor vs Express.js
```
Express.js:                  Ktor:
app.get('/')       →         get { }
app.post('/')      →         post { }
Middleware         →         Plugins/Install
req.body           →         call.receive()
res.json()         →         call.respond()
Promises/async     →         Coroutines
```

### Spring Boot vs NestJS
```
NestJS:                      Spring Boot:
@Controller()      →         @RestController
@Get()             →         @GetMapping()
@Injectable()      →         @Service
@Inject()          →         @Autowired
ValidationPipe     →         @Valid + validators
Global filter      →         @ControllerAdvice
```

## 🛠 Technology Stack

### Ktor Project
- **Framework**: Ktor 2.3.6
- **Language**: Kotlin 1.9.21
- **Runtime**: Netty (async)
- **Serialization**: kotlinx.serialization
- **Build**: Gradle with Kotlin DSL
- **JVM**: Java 17+

### Spring Boot Project
- **Framework**: Spring Boot 3.1.6
- **Language**: Kotlin 1.9.21
- **Database**: MySQL 8.0
- **ORM**: Hibernate/JPA
- **Validation**: Jakarta Bean Validation
- **Documentation**: Springdoc OpenAPI (Swagger)
- **Build**: Gradle with Kotlin DSL
- **JVM**: Java 17+

## 📖 Key Kotlin Concepts to Learn

1. **Data Classes**: Immutable objects with auto-generated methods
   ```kotlin
   data class User(val id: Long, val name: String)
   ```

2. **Coroutines**: Lightweight threading for async programming
   ```kotlin
   suspend fun fetchUser(): User { ... }
   ```

3. **Extension Functions**: Add methods to existing classes
   ```kotlin
   fun String.isValidEmail(): Boolean { ... }
   ```

4. **Sealed Classes**: Type-safe error handling
   ```kotlin
   sealed class Result<T> {
       data class Success<T>(val data: T) : Result<T>()
       data class Error<T>(val error: String) : Result<T>()
   }
   ```

5. **Scope Functions**: Clean object initialization
   ```kotlin
   user.apply { name = "Updated" }
        .also { println(it) }
   ```

## 🧪 Testing the APIs

### Manual Testing with Curl

**Get all users:**
```bash
curl http://localhost:8080/api/v1/users
```

**Create user:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "bob@example.com",
    "age": 30
  }'
```

**Update user:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "John Updated", "age": 31}'
```

**Delete user:**
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

### Using Swagger UI (Spring Boot Only)

Open browser: `http://localhost:8080/swagger-ui.html`

You can test all endpoints directly from the web interface!

## 📝 File Organization Pattern

### Ktor (Single File)
```kotlin
// Application.kt contains:
- Data Models (@Serializable)
- Business Logic (UserStore)
- Routes (DSL style)
```

Good for: Learning, microservices, prototypes

### Spring Boot (Layered)
```
controller/    → HTTP layer (@RestController)
service/       → Business logic (@Service)
repository/    → Database access (Spring Data JPA)
entity/        → Database models (@Entity)
dto/           → API contracts (Request/Response)
exception/     → Error handling (@RestControllerAdvice)
```

Good for: Enterprise apps, complex logic, large teams

## 🐳 Docker Support

### Build Docker Images

**Ktor:**
```bash
cd ktor-api-server
docker build -t ktor-users-api .
docker run -p 8080:8080 ktor-users-api
```

**Spring Boot:**
```bash
cd spring-boot-api-server
docker build -t spring-boot-users-api .
docker run -p 8080:8080 spring-boot-users-api
```

### Using Docker Compose for Spring Boot

```bash
cd spring-boot-api-server
docker-compose up -d        # Start MySQL
docker-compose down         # Stop MySQL
```

## 🔧 Configuration Profiles (Spring Boot)

### Default Profile
```bash
./gradlew bootRun
# Uses: application.yaml
# Database: users_db
# Logging: INFO
```

### Development Profile
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
# Uses: application-dev.yaml
# Shows SQL queries
# More logging
```

### Production Profile
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
# Uses: application-prod.yaml
# Less logging
# Swagger disabled
```

## 📊 Database Setup (Spring Boot Only)

### Option 1: Docker Compose (Recommended)
```bash
cd spring-boot-api-server
docker-compose up -d
```

This automatically:
- Starts MySQL 8.0
- Creates databases
- Loads sample data from init.sql

### Option 2: Manual MySQL Setup
```bash
# If MySQL is already running locally
mysql -u root -p1234 < spring-boot-api-server/init.sql
```

### Useful MySQL Commands
```bash
# Connect
mysql -u root -p1234

# View users
USE users_db;
SELECT * FROM users;

# Clear data
DELETE FROM users;
```

## 🎓 Learning Resources

### Understanding Kotlin
- **Data Classes**: Perfect for DTOs and models
- **Extension Functions**: Can add methods to existing classes
- **Coroutines**: Learn async/await alternative
- **Scope Functions**: `apply`, `also`, `let` for clean code

### Understanding Ktor
- **Routing DSL**: Express-like syntax
- **Plugins**: Similar to middleware
- **Serialization**: Type-safe JSON handling
- **Coroutines First**: Everything is async

### Understanding Spring Boot
- **Dependency Injection**: Automatic wiring with annotations
- **Repositories**: JPA makes database queries simple
- **Services**: Business logic separation
- **Controllers**: HTTP request handling
- **Exception Handlers**: Centralized error handling

## 💡 Tips for Success

1. **Start Simple**: Run Ktor first, it's simpler
2. **Compare Code**: Look at both implementations
3. **Modify and Experiment**: Change the code, see what happens
4. **Use Swagger**: Test Spring Boot APIs through web UI
5. **Read Error Messages**: Kotlin compiler is very helpful
6. **Check Logs**: Spring Boot logs are detailed and informative
7. **Use Your IDE**: IntelliJ IDEA has excellent Kotlin support
8. **Debug**: Set breakpoints and step through code
9. **Document**: Add comments to understand concepts

## 🚨 Common Issues

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### MySQL Connection Failed (Spring Boot)
```bash
# Start MySQL
docker-compose up -d

# Verify it's running
docker ps | grep mysql
```

### Gradle Build Failed
```bash
# Clean and rebuild
./gradlew clean build
```

## 📞 Next Steps

After completing this learning guide:

1. **Add new endpoints**: Create endpoints for products, posts, etc.
2. **Add authentication**: Implement JWT token validation
3. **Add pagination**: Support limit/offset for list endpoints
4. **Add filtering**: Filter users by name, age, email
5. **Add sorting**: Sort results by different fields
6. **Add caching**: Implement response caching
7. **Add monitoring**: Add metrics and tracing
8. **Add tests**: Write unit and integration tests

## 📚 File References

- **Ktor Implementation**: See `ktor-api-server/src/main/kotlin/Application.kt`
- **Spring Boot Implementation**: See files in `spring-boot-api-server/src/main/kotlin/com/example/`
- **Ktor Detailed Guide**: See `ktor-api-server/README.md`
- **Spring Boot Detailed Guide**: See `spring-boot-api-server/README.md`

## 🎯 Quick Command Reference

### Ktor
```bash
cd ktor-api-server
./gradlew run                 # Start server
./gradlew build               # Build project
./gradlew clean               # Clean build
```

### Spring Boot
```bash
cd spring-boot-api-server
docker-compose up -d          # Start MySQL
./gradlew bootRun             # Start server
./gradlew build               # Build project
./gradlew clean               # Clean build
./gradlew bootRun --args='--spring.profiles.active=dev'  # Dev mode
```

## 📄 License

These examples are provided for learning purposes.

---

**Happy Learning!** 🎓

Start with Ktor, then move to Spring Boot. Take your time to understand each framework before moving to the next one.

