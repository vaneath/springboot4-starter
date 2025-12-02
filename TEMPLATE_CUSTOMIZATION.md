# Template Customization Guide

This starter template is designed to be easily customizable. Here's how to adapt it to your needs.

## Configuration Files

### `template-config.properties`

This file contains all customizable values:

```properties
# Project Information
project.name=starter
project.description=Valome Springboot Starter

# Package Information
package.group=com.valome
package.name=starter

# Application Information
application.name=starter
application.class.name=StarterApplication

# Database Information
database.name=spring_starter
```

## Automated Renaming

### Using the Rename Script

The easiest way to customize the template is using the `rename-project.sh` script:

```bash
./rename-project.sh <new-group> <new-name> [new-description]
```

**What it does:**

1. Updates all package declarations in Java files
2. Renames package directories
3. Updates build configuration files
4. Updates application properties
5. Renames the main application class
6. Updates database name references
7. Updates documentation

**Example:**

```bash
./rename-project.sh com.acme ecommerce "E-Commerce Platform"
```

This will:

- Change package: `com.valome.starter` → `com.acme.ecommerce`
- Rename app class: `StarterApplication` → `EcommerceApplication`
- Update database: `spring_starter` → `spring_ecommerce`
- Update all references throughout the project

## Manual Customization

If you prefer to customize manually, here's what needs to be changed:

### 1. Package Structure

**Files to update:**

- All `.java` files (package declarations and imports)
- `build.gradle` (group)
- `settings.gradle` (rootProject.name)
- `gradle.properties` (group)
- `application.properties` (logging.level)
- `generate-crud.py` (hardcoded references)

**Directory structure:**

```
src/main/java/com/valome/starter/  →  src/main/java/com/yourcompany/yourapp/
```

### 2. Application Name

**Files to update:**

- `build.gradle` (description)
- `settings.gradle` (rootProject.name)
- `application.properties` (spring.application.name)
- `README.md` (references)

### 3. Database Name

**Files to update:**

- `application.properties` (spring.datasource.url)
- `README.md` (documentation)

### 4. Main Application Class

**Files to update:**

- Rename `StarterApplication.java` to `YourAppApplication.java`
- Update class name inside the file
- Update references in documentation

## Version Management

### Using gradle.properties

Version is managed in `gradle.properties`:

```properties
version=1.0.0
```

### Using the Bump Script

Automatically bump versions:

```bash
./bump-version.sh patch   # 1.0.0 → 1.0.1
./bump-version.sh minor   # 1.0.0 → 1.1.0
./bump-version.sh major   # 1.0.0 → 2.0.0
./bump-version.sh release # Remove SNAPSHOT
./bump-version.sh snapshot # Add SNAPSHOT
```

## Best Practices

1. **Rename early:** Run `rename-project.sh` before making significant changes
2. **Use config file:** Keep `template-config.properties` updated
3. **Version control:** Commit after renaming, before major development
4. **Test after rename:** Always rebuild and test after renaming

## Troubleshooting

### Package not found after rename?

1. Clean build: `./gradlew clean`
2. Rebuild: `./gradlew build`
3. Refresh IDE (if using IntelliJ/Eclipse)

### Import errors?

- Check package declarations match directory structure
- Verify `generate-crud.py` uses `BASE_PACKAGE` variable
- Rebuild the project

### Database connection issues?

- Verify database name matches `database.name` in config
- Check `application.properties` has correct database name
- Ensure database exists: `CREATE DATABASE spring_your_app_name;`

## After Customization

1. **Update README.md** with your project-specific information
2. **Update QUICK_START.md** if needed
3. **Review and customize** generated code from CRUD generator
4. **Set up CI/CD** with your project name
5. **Update Postman collection** name if needed

## Configuration Reference

| Config Value             | Used In                     | Example                |
| ------------------------ | --------------------------- | ---------------------- |
| `package.group`          | Java packages, build.gradle | `com.acme`             |
| `package.name`           | Java packages, directories  | `ecommerce`            |
| `project.name`           | settings.gradle, docs       | `ecommerce`            |
| `application.name`       | application.properties      | `ecommerce`            |
| `application.class.name` | Main class file             | `EcommerceApplication` |
| `database.name`          | application.properties      | `spring_ecommerce`     |

---

**Tip:** Always run `./gradlew clean build` after renaming to ensure everything compiles correctly.
