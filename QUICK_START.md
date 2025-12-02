# Quick Start Guide

Get your Spring Boot application up and running in minutes!

> 📖 **Documentation Navigation:**
>
> - [README.md](README.md) - Project overview and reference
> - [Template Customization Guide](TEMPLATE_CUSTOMIZATION.md) - Customize the template
> - [Quick Start Guide](QUICK_START.md) - This guide

## Step 1: Clone and Setup

```bash
git clone <repository-url>
cd starter
```

## Step 2: Rename Project (Optional but Recommended)

Customize the project with your own name and package:

```bash
./rename-project.sh com.yourcompany your-app-name "Your App Description"
```

**Example:**

```bash
./rename-project.sh com.acme ecommerce "E-Commerce Platform"
```

This will:

- Change package from `com.valome.starter` to `com.acme.ecommerce`
- Rename application class
- Update all configuration files
- Update database name

## Step 3: Configure Application

```bash
# Copy example properties
cp src/main/resources/application.example.properties src/main/resources/application.properties

# Edit application.properties with your database credentials
nano src/main/resources/application.properties
```

**Required changes:**

- Database URL, username, password
- JWT secrets (generate with: `openssl rand -base64 32`)

## Step 4: Create Database

```sql
CREATE DATABASE spring_your_app_name;
```

(Replace `your_app_name` with your actual app name)

## Step 5: Build and Run

```bash
# Build
./gradlew build

# Run
./gradlew bootRun
```

## Step 6: Test

The application will be available at:

- **Base URL:** `http://localhost:8080/api`
- **API Version:** `v1`

**Test endpoints:**

```bash
# Register a user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## Next Steps

1. **Generate CRUD entities:**

   ```bash
   ./generate-crud.sh Product "name:String:@NotBlank,price:BigDecimal:@NotNull"
   ```

2. **Review generated code** and customize as needed

3. **Compile:**

   ```bash
   ./gradlew compileJava
   ```

4. **Start building your application!**

## Troubleshooting

**Build fails?**

- Make sure JDK 21+ is installed: `java -version`
- Clean and rebuild: `./gradlew clean build`

**Database connection error?**

- Check PostgreSQL is running: `sudo systemctl status postgresql`
- Verify database exists and credentials are correct

**Port already in use?**

- Change port in `application.properties`: `server.port=8081`

## Need Help?

- **[README.md](README.md)** - Project overview, features, API reference, and more
- **[Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)** - Detailed customization instructions
- Review `template-config.properties` for configuration options

---

**Next Steps:**

- Customize the template? See [Template Customization Guide](TEMPLATE_CUSTOMIZATION.md)
- Need API reference? See [README.md](README.md#api-endpoints)
- Want to generate CRUD entities? See [README.md](README.md#crud-generator)
