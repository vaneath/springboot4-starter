# Spring Boot Starter Project

A production-ready Spring Boot starter template with JWT authentication, CRUD generator, soft delete, audit trail, and advanced pagination/search capabilities.

**Made by:** vaneath - vaneathzz@gmail.com

## Table of Contents

- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Renaming the Project](#6-rename-the-project-optional)
- [Project Structure](#project-structure)
- [CRUD Generator](#crud-generator)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Development](#development)

## Technology Stack

- **Spring Boot:** 4.0.0
- **Java:** 21
- **Build Tool:** Gradle 9.2.1
- **Database:** PostgreSQL
- **Key Dependencies:**
  - Spring Security (JWT-based authentication)
  - Spring Data JPA
  - MapStruct (DTO mapping)
  - Lombok (boilerplate reduction)
  - Flyway (database migrations)
  - JWT (io.jsonwebtoken:jjwt)

## Prerequisites

Before you begin, ensure you have the following installed:

- **JDK 21+** (Java Development Kit)
- **PostgreSQL** (database server)
- **Gradle** (or use the included Gradle wrapper)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd starter
```

### 2. Configure Application Properties

Copy the example properties file to create your configuration:

```bash
cp src/main/resources/application.example.properties src/main/resources/application.properties
```

### 3. Configure Database Connection

Edit `src/main/resources/application.properties` and update the database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_starter?useUnicode=yes&characterEncoding=UTF-8
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 4. Create PostgreSQL Database

```sql
CREATE DATABASE spring_starter;
```

### 5. Configure JWT Secrets

Generate secure JWT secrets using OpenSSL:

```bash
openssl rand -base64 32
```

Update the JWT configuration in `application.properties`:

```properties
jwt.access.secret=your-generated-secret-here
jwt.refresh.secret=your-generated-secret-here
```

### 6. Rename the Project (Optional)

If you want to customize this starter template with your own project name and package structure, use the rename script:

```bash
./rename-project.sh <new-group> <new-name> [new-description]
```

**Examples:**

```bash
# Basic rename
./rename-project.sh com.mycompany myapp

# With custom description
./rename-project.sh com.mycompany myapp "My Awesome Application"
```

**What the script does:**

- Updates package names in all Java files
- Renames package directories
- Updates `build.gradle`, `settings.gradle`, and `gradle.properties`
- Updates `application.properties` files
- Renames the main application class
- Updates database name references
- Updates README.md and other documentation

**After renaming:**

1. Create the new database: `CREATE DATABASE spring_<new-name>;`
2. Rebuild: `./gradlew clean build`
3. Review changes: `git diff`

**Manual Configuration:**
You can also manually edit `template-config.properties` and run the script without arguments to see current values.

### 7. Build and Run

Using Gradle wrapper:

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

Or using Gradle directly:

```bash
gradle build
gradle bootRun
```

The application will start on `http://localhost:8080` with context path `/api`.

## Project Structure

```
src/main/java/com/valome/starter/
├── builder/              # Builder utilities (e.g., SortBuilder)
├── config/               # Configuration classes
│   ├── JwtProperties.java
│   └── SecurityConfig.java
├── constant/             # Constants
│   └── JwtConstant.java
├── controller/           # REST controllers
│   ├── AuthApiController.java
│   ├── ProductApiController.java
│   ├── RoleApiController.java
│   └── UserApiController.java
├── dto/                  # Data Transfer Objects
│   ├── auth/            # Authentication DTOs
│   ├── core/            # Base DTOs (BaseRequest, BaseResponse, SuccessResponse, ErrorResponse)
│   ├── product/         # Product DTOs
│   ├── role/            # Role DTOs
│   ├── search/          # Pagination and search DTOs
│   └── store/           # Store DTOs
├── exception/            # Exception handlers
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── filter/               # HTTP filters
│   └── JwtFilter.java
├── jpa/                  # JPA repositories (legacy naming)
│   ├── role/
│   ├── user/
│   └── userrole/
├── mapper/               # MapStruct mappers
│   ├── ProductMapper.java
│   └── RoleMapper.java
├── model/                # JPA entities
│   ├── BaseModel.java   # Base entity with audit fields
│   ├── Product.java
│   ├── Role.java
│   ├── User.java
│   └── UserRole.java
├── repository/           # Repository interfaces
│   ├── jdbc/
│   └── jpa/
│       └── core/
│           └── BaseRepository.java  # Base repository with soft delete
├── service/              # Business logic
│   ├── auth/            # Authentication services
│   ├── product/         # Product services
│   ├── role/            # Role services
│   ├── search/          # Pagination and search services
│   ├── store/           # Store services
│   └── user/            # User services
├── util/                 # Utility classes
│   └── ResponseHandler.java
└── StarterApplication.java
```

### Key Patterns

#### BaseModel

All entities extend `BaseModel`, which provides:

- **Audit Fields:**

  - `id` (Long, auto-generated)
  - `createdBy` (Long)
  - `updatedBy` (Long)
  - `deletedBy` (Long)
  - `createdAt` (LocalDateTime)
  - `updatedAt` (LocalDateTime)
  - `deletedAt` (LocalDateTime)
  - `active` (Boolean, default: true)

- **Automatic Audit Trail:**
  - `@PrePersist` sets `createdAt` and `createdBy`
  - `@PreUpdate` sets `updatedAt` and `updatedBy`
  - `softDelete()` method sets `deletedAt` and `deletedBy`

#### BaseRepository

All repositories extend `BaseRepository<T, ID>`, which:

- Automatically filters out soft-deleted records (`deletedAt IS NULL`)
- Provides standard CRUD operations
- Supports JPA Specifications for advanced querying
- Overrides `findAll()`, `findById()`, `count()` to exclude deleted records

#### Soft Delete

Entities are never physically deleted. Instead, they are soft-deleted by:

1. Setting `deletedAt` timestamp
2. Setting `deletedBy` user ID
3. Automatically filtered out by `BaseRepository` queries

## CRUD Generator

The project includes a powerful CRUD generator script that automatically creates all necessary components for a new entity.

### Usage

```bash
./generate-crud.sh EntityName [fields]
```

### Examples

**Basic entity (no fields):**

```bash
./generate-crud.sh Product
```

**Entity with fields:**

```bash
./generate-crud.sh Product "name:String:@NotBlank,price:BigDecimal:@NotNull,description:String"
```

**Complex entity:**

```bash
./generate-crud.sh Order "orderNumber:String:@NotBlank,totalAmount:BigDecimal:@NotNull,orderDate:LocalDateTime,status:String:@NotBlank"
```

### Field Format

```
name:Type:@Annotation1;@Annotation2
```

**Supported Types:**

- `String`
- `Integer`
- `Long`
- `BigDecimal`
- `LocalDate`
- `LocalDateTime`
- `Boolean`

**Common Annotations:**

- `@NotBlank` - String cannot be blank
- `@NotNull` - Field cannot be null
- `@Email` - Must be valid email format
- `@Size(max=100)` - String size constraint
- `@Past` - Date must be in the past
- `@Pattern(regexp="...")` - Custom regex pattern
- `@Min(0)` - Minimum value for numbers
- `@Max(100)` - Maximum value for numbers

**Multiple annotations:** Separate with semicolon (`;`)

Example:

```bash
./generate-crud.sh User "email:String:@NotBlank;@Email,age:Integer:@NotNull;@Min(18);@Max(120)"
```

### Generated Files

The generator creates the following files:

```
src/main/java/com/valome/starter/
├── model/
│   └── {EntityName}.java                    # JPA Entity
├── repository/jpa/
│   └── {EntityName}Repository.java          # JPA Repository
├── service/{entity-lower}/
│   ├── {EntityName}Service.java             # Service Interface
│   └── {EntityName}ServiceImpl.java         # Service Implementation
├── mapper/
│   └── {EntityName}Mapper.java              # MapStruct Mapper
├── controller/
│   └── {EntityName}ApiController.java       # REST Controller
└── dto/{entity-lower}/
    ├── {EntityName}CreateRequest.java        # Create DTO
    ├── {EntityName}UpdateRequest.java        # Update DTO
    └── {EntityName}Response.java             # Response DTO
```

### After Generation

1. **Review generated files** - Customize as needed
2. **Compile the project:**
   ```bash
   ./gradlew compileJava
   ```
3. **Test endpoints** - The controller will be available at:
   ```
   POST   /api/v1/{route-base}/search   - Search with pagination
   POST   /api/v1/{route-base}          - Create
   GET    /api/v1/{route-base}/{id}     - Get by ID
   PUT    /api/v1/{route-base}/{id}     - Update
   DELETE /api/v1/{route-base}/{id}     - Delete (soft delete)
   ```

**Note:** `{route-base}` is the pluralized, kebab-case version of your entity name (e.g., `Product` → `products`, `Order` → `orders`).

## Configuration

### Database Configuration

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_starter?useUnicode=yes&characterEncoding=UTF-8
spring.datasource.username=postgres
spring.datasource.password=postgres

# Hikari Connection Pool (optional)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.connection-timeout=20000

# JPA Configuration
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=update
```

### JWT Configuration

```properties
# Access token (1 hour in milliseconds)
jwt.access.secret=your-secret-key-here
jwt.access.expiration=3600000

# Refresh token (7 days in milliseconds)
jwt.refresh.secret=your-secret-key-here
jwt.refresh.expiration=604800000
```

**Generate secrets:**

```bash
openssl rand -base64 32
```

### Security Configuration

```properties
spring.security.user.name=admin
spring.security.user.password=admin123
spring.security.debug=true  # Enable for development only
```

### Logging Configuration

```properties
# Root logging level
logging.level.root=INFO

# Application package logging level
logging.level.com.valome.starter=DEBUG

# SQL Logging (optional, for debugging)
# logging.level.org.hibernate.SQL=DEBUG
# logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
# spring.jpa.properties.hibernate.format_sql=true
```

### Application Configuration

```properties
spring.application.name=starter
spring.output.ansi.enabled=always
server.servlet.context-path=/api
```

## API Endpoints

### Base URL

All endpoints are prefixed with `/api/v1`

### Authentication Endpoints

```
POST   /api/v1/auth/login      - Login and get JWT tokens
POST   /api/v1/auth/register   - Register new user
```

### CRUD Endpoints Pattern

For each entity (e.g., Product, Role, User):

```
POST   /api/v1/{entity-plural}/search   - Search with pagination, filtering, sorting
POST   /api/v1/{entity-plural}          - Create new entity
GET    /api/v1/{entity-plural}/{id}     - Get entity by ID
PUT    /api/v1/{entity-plural}/{id}     - Update entity
DELETE /api/v1/{entity-plural}/{id}     - Soft delete entity
```

### Example: Product Endpoints

```
POST   /api/v1/products/search
POST   /api/v1/products
GET    /api/v1/products/1
PUT    /api/v1/products/1
DELETE /api/v1/products/1
```

### Search/Pagination Request Format

```json
{
  "search": "keyword",
  "filters": [
    {
      "field": "name",
      "operator": "LIKE",
      "value": "product"
    }
  ],
  "sorts": [
    {
      "field": "createdAt",
      "direction": "DESC"
    }
  ],
  "page": 0,
  "size": 20
}
```

### Response Format

All responses follow this structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

Error responses:

```json
{
  "success": false,
  "message": "Error message",
  "errors": [ ... ]
}
```

## Features

### 🔐 JWT Authentication

- Access tokens (short-lived, 1 hour)
- Refresh tokens (long-lived, 7 days)
- Automatic token validation via `JwtFilter`
- User context available in SecurityContext

### 🗑️ Soft Delete

- All entities support soft delete
- Deleted records are automatically filtered out
- Audit trail maintained (deletedAt, deletedBy)

### 📝 Audit Trail

- Automatic tracking of:
  - Creation (createdAt, createdBy)
  - Updates (updatedAt, updatedBy)
  - Deletion (deletedAt, deletedBy)
- User context automatically extracted from SecurityContext

### 🔍 Advanced Search & Pagination

- Full-text search across multiple fields
- Filtering with various operators (LIKE, EQUALS, GREATER_THAN, etc.)
- Multi-field sorting
- Configurable pagination
- Field-level searchability and filterability configuration

### 🗺️ DTO Mapping

- MapStruct for type-safe DTO mapping
- Automatic null handling
- Custom mapping logic support

### 🏗️ BaseModel Pattern

- Consistent entity structure
- Automatic audit field management
- Soft delete support built-in

### 🚀 CRUD Generator

- Generate complete CRUD stack with one command
- Smart validation annotations
- Consistent code structure
- Time-saving development tool

## Development

### Building the Project

```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Compile only
./gradlew compileJava
```

### Running Tests

```bash
./gradlew test
```

### Running the Application

```bash
# Using Gradle
./gradlew bootRun

# Using JAR
./gradlew bootJar
java -jar build/libs/starter-0.0.1-SNAPSHOT.jar
```

### Development Tips

1. **Hot Reload:** Spring Boot DevTools is included for automatic restart on code changes

2. **Database Migrations:** Use Flyway for version-controlled database migrations

3. **API Testing:** A Postman collection is available at `starter.postman_collection.json`

4. **Code Generation:** After running the CRUD generator, always compile to generate MapStruct implementations:

   ```bash
   ./gradlew compileJava
   ```

5. **Logging:** Enable SQL logging in development by uncommenting SQL logging properties in `application.properties`

6. **Security:** Remember to disable `spring.security.debug=true` in production

### Project Conventions

- **Package:** `com.valome.starter`
- **API Version:** `v1`
- **Context Path:** `/api`
- **Naming:**
  - Controllers: `{Entity}ApiController`
  - Services: `{Entity}Service` / `{Entity}ServiceImpl`
  - Repositories: `{Entity}Repository`
  - Mappers: `{Entity}Mapper`
  - DTOs: `{Entity}CreateRequest`, `{Entity}UpdateRequest`, `{Entity}Response`

## Contact

**Author:** vaneath  
**Email:** vaneathzz@gmail.com

---

**Happy Coding! 🚀**
