# Spring Boot Starter Project

A production-ready Spring Boot starter template with JWT authentication, CRUD generator, soft delete, audit trail, and advanced pagination/search capabilities.

**Made by:** vaneath - vaneathzz@gmail.com

## 📚 Documentation

- **[Quick Start Guide](QUICK_START.md)** - Get up and running in minutes
- **[CRUD Generator Guide](CRUD_GENERATOR.md)** - Generate CRUD components automatically
- **[Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)** - Customize the template for your project
- **[README.md](README.md)** - This file (overview and reference)

---

## 📑 Table of Contents

<details>
<summary>Click to expand navigation</summary>

### Getting Started

- [Quick Start](#quick-start)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)

### Project Overview

- [Project Structure](#project-structure)
  - [Key Patterns](#key-patterns)
    - [BaseModel](#basemodel)
    - [BaseRepository](#baserepository)
    - [Soft Delete](#soft-delete)

### Development Tools

- [CRUD Generator](#crud-generator) - See [CRUD Generator Guide](CRUD_GENERATOR.md) for details

### Configuration

- [Configuration](#configuration)
  - [Database Configuration](#database-configuration)
  - [JWT Configuration](#jwt-configuration)
  - [Security Configuration](#security-configuration)
  - [Logging Configuration](#logging-configuration)
  - [Application Configuration](#application-configuration)

### API Reference

- [API Endpoints](#api-endpoints)
  - [Base URL](#base-url)
  - [Authentication Endpoints](#authentication-endpoints)
  - [CRUD Endpoints Pattern](#crud-endpoints-pattern)
  - [Example: Product Endpoints](#example-product-endpoints)
  - [Search/Pagination Request Format](#searchpagination-request-format)
  - [Response Format](#response-format)

### Features & Development

- [Features](#features)
- [Development](#development)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
  - [Running the Application](#running-the-application)
  - [Development Tips](#development-tips)
  - [Project Conventions](#project-conventions)

</details>

---

## Technology Stack

- **Spring Boot:** 4.0.0
- **Java:** 24
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

## Quick Start

For detailed setup instructions, see the **[Quick Start Guide](QUICK_START.md)**.

**Quick overview:**

1. Clone the repository
2. (Optional) Rename the project using `./rename-project.sh` - see [Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)
3. Copy `application.example.properties` to `application.properties` and configure
4. Create PostgreSQL database
5. Build and run: `./gradlew bootRun`

The application will start on `http://localhost:8080` with context path `/api`.

**Want to customize the template?** Check out the **[Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)** for detailed instructions on renaming packages, managing versions, and more.

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

**Quick example:**

```bash
./generate-crud.sh Product "name:String:@NotBlank,price:BigDecimal:@NotNull"
```

For complete documentation, examples, and best practices, see the **[CRUD Generator Guide](CRUD_GENERATOR.md)**.

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

<div align="center">

**[⬆ Back to Top](#spring-boot-starter-project)** | **[Quick Start](QUICK_START.md)** | **[CRUD Generator](CRUD_GENERATOR.md)** | **[Customization](TEMPLATE_CUSTOMIZATION.md)**

**Happy Coding! 🚀**

</div>
