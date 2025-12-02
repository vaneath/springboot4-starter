# CRUD Generator Guide

The project includes a powerful CRUD generator script that automatically creates all necessary components for a new entity.

> 📖 **Documentation Navigation:**
>
> - [README.md](README.md) - Project overview and reference
> - [Quick Start Guide](QUICK_START.md) - Initial setup instructions
> - [Template Customization Guide](TEMPLATE_CUSTOMIZATION.md) - Customize the template
> - [CRUD Generator Guide](CRUD_GENERATOR.md) - This guide

## Usage

```bash
./generate-crud.sh EntityName [fields]
```

## Examples

### Basic Entity (No Fields)

```bash
./generate-crud.sh Product
```

### Entity with Fields

```bash
./generate-crud.sh Product "name:String:@NotBlank,price:BigDecimal:@NotNull,description:String"
```

### Complex Entity

```bash
./generate-crud.sh Order "orderNumber:String:@NotBlank,totalAmount:BigDecimal:@NotNull,orderDate:LocalDateTime,status:String:@NotBlank"
```

## Field Format

```
name:Type:@Annotation1;@Annotation2
```

### Supported Types

- `String`
- `Integer`
- `Long`
- `BigDecimal`
- `LocalDate`
- `LocalDateTime`
- `Boolean`

### Common Annotations

- `@NotBlank` - String cannot be blank
- `@NotNull` - Field cannot be null
- `@Email` - Must be valid email format
- `@Size(max=100)` - String size constraint
- `@Past` - Date must be in the past
- `@Pattern(regexp="...")` - Custom regex pattern
- `@Min(0)` - Minimum value for numbers
- `@Max(100)` - Maximum value for numbers

**Multiple annotations:** Separate with semicolon (`;`)

### Example with Multiple Annotations

```bash
./generate-crud.sh User "email:String:@NotBlank;@Email,age:Integer:@NotNull;@Min(18);@Max(120)"
```

## Generated Files

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

## After Generation

### Step 1: Review Generated Files

Review and customize the generated files as needed. The generator creates production-ready code, but you may want to:

- Add custom validation logic
- Modify field configurations
- Add relationships to other entities
- Customize business logic in services

### Step 2: Compile the Project

After generating CRUD components, compile the project to generate MapStruct implementations:

```bash
./gradlew compileJava
```

### Step 3: Test Endpoints

The controller will be available at the following endpoints:

```
POST   /api/v1/{route-base}/search   - Search with pagination
POST   /api/v1/{route-base}          - Create
GET    /api/v1/{route-base}/{id}     - Get by ID
PUT    /api/v1/{route-base}/{id}     - Update
DELETE /api/v1/{route-base}/{id}     - Delete (soft delete)
```

**Note:** `{route-base}` is the pluralized, kebab-case version of your entity name:

- `Product` → `products`
- `Order` → `orders`
- `MessageTemplate` → `message-templates`
- `UserRole` → `user-roles`

## How It Works

The CRUD generator uses a Python script (`generate-crud.py`) that:

1. **Detects the base package** automatically from your project structure
2. **Generates all CRUD components** following the project's conventions
3. **Applies smart defaults** for validation annotations
4. **Creates consistent code** that follows the project's patterns

### Generated Components

#### Entity Model

- Extends `BaseModel` for audit trail support
- Includes `PAGINATION_FIELDS` configuration for search/filter
- Proper JPA annotations and column mappings

#### Repository

- Extends `BaseRepository` for soft delete support
- Ready for JPA Specifications

#### Service Layer

- Interface and implementation
- Full CRUD operations
- Search with pagination support
- Proper transaction management

#### Mapper (MapStruct)

- DTO to Entity mapping
- Entity to Response mapping
- Update entity mapping with null handling

#### Controller

- RESTful endpoints
- Proper validation
- Consistent response format
- API versioning (`/v1/`)

#### DTOs

- `CreateRequest` - With validation annotations
- `UpdateRequest` - Optional fields for partial updates
- `Response` - Complete entity information

## Tips & Best Practices

1. **Generate early:** Use the generator when starting a new entity to ensure consistency
2. **Review before committing:** Always review generated code and customize as needed
3. **Compile after generation:** Run `./gradlew compileJava` to generate MapStruct implementations
4. **Test endpoints:** Use the generated endpoints to verify everything works
5. **Customize as needed:** The generator creates a solid foundation, but feel free to add custom logic

## Troubleshooting

### MapStruct Implementation Not Generated?

Run:

```bash
./gradlew clean compileJava
```

### Package Not Found Errors?

Make sure you've run the rename script if you customized the package:

```bash
./rename-project.sh com.yourcompany yourapp
```

### Import Errors?

1. Clean and rebuild: `./gradlew clean build`
2. Refresh your IDE
3. Check that all generated files are in the correct package structure

## Related Documentation

- **[README.md](README.md)** - Project overview and API reference
- **[Quick Start Guide](QUICK_START.md)** - Initial setup
- **[Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)** - Customize the template

---

**Need help?** Check the main [README.md](README.md) or review the generated code examples in the project.
