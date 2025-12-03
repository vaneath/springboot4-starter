#!/bin/bash

# Project Rename Script for Spring Boot Starter Template
# Usage: ./rename-project.sh <new-group> <new-name> [new-description]
# Example: ./rename-project.sh com.mycompany myapp "My Awesome App"

set -e

CONFIG_FILE="template-config.properties"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    print_error "Config file $CONFIG_FILE not found!"
    exit 1
fi

# Parse arguments
if [ $# -lt 2 ]; then
    print_error "Usage: $0 <new-group> <new-name> [new-description]"
    echo ""
    echo "Examples:"
    echo "  $0 com.mycompany myapp"
    echo "  $0 com.mycompany myapp \"My Awesome Application\""
    echo ""
    echo "Current values from $CONFIG_FILE:"
    grep -E "^package.group=|^package.name=|^project.name=|^project.description=" "$CONFIG_FILE"
    exit 1
fi

NEW_GROUP=$1
NEW_NAME=$2
NEW_DESCRIPTION=${3:-"${NEW_NAME^} Application"}

# Validate inputs
if [[ ! "$NEW_GROUP" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$ ]]; then
    print_error "Invalid group format: $NEW_GROUP"
    print_error "Group should be like: com.company or com.company.project"
    exit 1
fi

if [[ ! "$NEW_NAME" =~ ^[a-z][a-z0-9-]*$ ]]; then
    print_error "Invalid name format: $NEW_NAME"
    print_error "Name should be lowercase alphanumeric with hyphens (e.g., my-app)"
    exit 1
fi

# Extract current values from config
OLD_GROUP=$(grep "^package.group=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_NAME=$(grep "^package.name=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_PROJECT_NAME=$(grep "^project.name=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_DESCRIPTION=$(grep "^project.description=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_APP_NAME=$(grep "^application.name=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_APP_CLASS=$(grep "^application.class.name=" "$CONFIG_FILE" | cut -d'=' -f2)
OLD_DB_NAME=$(grep "^database.name=" "$CONFIG_FILE" | cut -d'=' -f2)

# Calculate package paths
OLD_PACKAGE="${OLD_GROUP}.${OLD_NAME}"
NEW_PACKAGE="${NEW_GROUP}.${NEW_NAME}"
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')
NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

# Calculate application class name (PascalCase)
NEW_APP_CLASS=$(echo "$NEW_NAME" | sed -r 's/(^|-)([a-z])/\U\2/g' | sed 's/-//g')
NEW_APP_CLASS="${NEW_APP_CLASS}Application"

# Calculate database name
NEW_DB_NAME="spring_${NEW_NAME}"

print_info "Starting project rename..."
echo ""
print_info "Old Group:      $OLD_GROUP"
print_info "New Group:      $NEW_GROUP"
echo ""
print_info "Old Name:       $OLD_NAME"
print_info "New Name:       $NEW_NAME"
echo ""
print_info "Old Package:    $OLD_PACKAGE"
print_info "New Package:    $NEW_PACKAGE"
echo ""
print_info "Old App Class:  $OLD_APP_CLASS"
print_info "New App Class:  $NEW_APP_CLASS"
echo ""
print_info "Old DB Name:    $OLD_DB_NAME"
print_info "New DB Name:    $NEW_DB_NAME"
echo ""

read -p "Continue with rename? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warn "Rename cancelled."
    exit 0
fi

# Backup original config
cp "$CONFIG_FILE" "${CONFIG_FILE}.backup"
print_info "Backed up config file"

# Step 1: Update config file
print_info "Updating $CONFIG_FILE..."
sed -i.bak "s|^package.group=.*|package.group=$NEW_GROUP|" "$CONFIG_FILE"
sed -i.bak "s|^package.name=.*|package.name=$NEW_NAME|" "$CONFIG_FILE"
sed -i.bak "s|^project.name=.*|project.name=$NEW_NAME|" "$CONFIG_FILE"
sed -i.bak "s|^project.description=.*|project.description=$NEW_DESCRIPTION|" "$CONFIG_FILE"
sed -i.bak "s|^application.name=.*|application.name=$NEW_NAME|" "$CONFIG_FILE"
sed -i.bak "s|^application.class.name=.*|application.class.name=$NEW_APP_CLASS|" "$CONFIG_FILE"
sed -i.bak "s|^database.name=.*|database.name=$NEW_DB_NAME|" "$CONFIG_FILE"
rm -f "${CONFIG_FILE}.bak"

# Step 2: Update build.gradle
print_info "Updating build.gradle..."
sed -i.bak "s|group = project.findProperty('group') ?: '${OLD_GROUP}'|group = project.findProperty('group') ?: '${NEW_GROUP}'|" build.gradle
sed -i.bak "s|description = '${OLD_DESCRIPTION}'|description = '${NEW_DESCRIPTION}'|" build.gradle
rm -f build.gradle.bak

# Step 3: Update settings.gradle
print_info "Updating settings.gradle..."
sed -i.bak "s|rootProject.name = '${OLD_PROJECT_NAME}'|rootProject.name = '${NEW_NAME}'|" settings.gradle
rm -f settings.gradle.bak

# Step 4: Update gradle.properties
print_info "Updating gradle.properties..."
if [ -f gradle.properties ]; then
    sed -i.bak "s|^group=.*|group=$NEW_GROUP|" gradle.properties
    rm -f gradle.properties.bak
fi

# Step 5: Update application.properties files
print_info "Updating application.properties files..."
for prop_file in src/main/resources/application*.properties; do
    if [ -f "$prop_file" ]; then
        sed -i.bak "s|spring.application.name=${OLD_APP_NAME}|spring.application.name=${NEW_NAME}|g" "$prop_file"
        sed -i.bak "s|spring_starter|${NEW_DB_NAME}|g" "$prop_file"
        sed -i.bak "s|logging.level.${OLD_PACKAGE}|logging.level.${NEW_PACKAGE}|g" "$prop_file"
        rm -f "${prop_file}.bak"
    fi
done

# Step 6: Rename package directories (main and test)
print_info "Renaming package directories..."

# Function to safely move package directory
move_package_directory() {
    local OLD_DIR="$1"
    local NEW_DIR="$2"
    local TYPE="$3"
    
    if [ ! -d "$OLD_DIR" ]; then
        print_warn "${TYPE} package directory not found: $OLD_DIR"
        return 0
    fi
    
    # Check if new directory already exists
    if [ -d "$NEW_DIR" ]; then
        print_error "Target directory already exists: $NEW_DIR"
        print_error "Cannot proceed with directory move."
        exit 1
    fi
    
    # Create parent directories for new path
    mkdir -p "$(dirname "$NEW_DIR")"
    
    # Use cp -r and then rm -rf to avoid mv issues with nested paths
    # This is safer when old and new paths might share parent directories
    cp -r "$OLD_DIR" "$NEW_DIR"
    
    if [ $? -eq 0 ]; then
        # Remove old directory recursively
        rm -rf "$OLD_DIR"
        print_info "Moved ${TYPE} package directory: $OLD_DIR -> $NEW_DIR"
        
        # Clean up empty parent directories (bottom-up)
        local current_dir=$(dirname "$OLD_DIR")
        while [ "$current_dir" != "src/main/java" ] && [ "$current_dir" != "src/test/java" ] && [ "$current_dir" != "." ]; do
            if [ -d "$current_dir" ] && [ -z "$(ls -A "$current_dir" 2>/dev/null)" ]; then
                rmdir "$current_dir" 2>/dev/null || break
                current_dir=$(dirname "$current_dir")
            else
                break
            fi
        done
    else
        print_error "Failed to copy ${TYPE} package directory"
        exit 1
    fi
}

# Rename main source directories
OLD_MAIN_DIR="src/main/java/${OLD_PACKAGE_PATH}"
NEW_MAIN_DIR="src/main/java/${NEW_PACKAGE_PATH}"
move_package_directory "$OLD_MAIN_DIR" "$NEW_MAIN_DIR" "main"

# Rename test source directories
OLD_TEST_DIR="src/test/java/${OLD_PACKAGE_PATH}"
NEW_TEST_DIR="src/test/java/${NEW_PACKAGE_PATH}"
move_package_directory "$OLD_TEST_DIR" "$NEW_TEST_DIR" "test"

# Step 7: Update all Java files (package declarations and imports)
print_info "Updating Java files (main and test)..."
# Use find with -print0 and while with -d '' to handle filenames with spaces
find src -name "*.java" -type f -print0 | while IFS= read -r -d '' file; do
    # Update package declarations (only at start of line, match complete package)
    sed -i.bak "s|^package ${OLD_PACKAGE}|package ${NEW_PACKAGE}|g" "$file"
    
    # Update imports (complete import statements)
    sed -i.bak "s|^import ${OLD_PACKAGE}\\.|import ${NEW_PACKAGE}.|g" "$file"
    sed -i.bak "s|^import static ${OLD_PACKAGE}\\.|import static ${NEW_PACKAGE}.|g" "$file"
    
    # Update string references in code (for BaseRepository import in generate-crud.py)
    # Use word boundaries to avoid partial replacements
    sed -i.bak "s|\\b${OLD_PACKAGE}\\b|${NEW_PACKAGE}|g" "$file"
    
    rm -f "${file}.bak"
done

# Update application class references separately to avoid over-replacement
print_info "Updating application class references in Java files..."
# Use find with -print0 and while with -d '' to handle filenames with spaces
find src -name "*.java" -type f -print0 | while IFS= read -r -d '' file; do
    # Update class references (e.g., StarterApplication.class)
    sed -i.bak "s|${OLD_APP_CLASS}\\.class|${NEW_APP_CLASS}.class|g" "$file"
    # Update class name references (but be careful not to replace partial matches)
    sed -i.bak "s|\\b${OLD_APP_CLASS}\\b|${NEW_APP_CLASS}|g" "$file"
    
    rm -f "${file}.bak"
done

# Step 8: Rename main application class (and test class if exists)
print_info "Renaming main application class..."

# Check both old and new locations for application class (in case directory move already happened)
OLD_APP_FILE_NEW_LOC="src/main/java/${NEW_PACKAGE_PATH}/${OLD_APP_CLASS}.java"
OLD_APP_FILE_OLD_LOC="src/main/java/${OLD_PACKAGE_PATH}/${OLD_APP_CLASS}.java"
NEW_APP_FILE="src/main/java/${NEW_PACKAGE_PATH}/${NEW_APP_CLASS}.java"

# Determine which location has the file
if [ -f "$OLD_APP_FILE_NEW_LOC" ]; then
    OLD_APP_FILE="$OLD_APP_FILE_NEW_LOC"
elif [ -f "$OLD_APP_FILE_OLD_LOC" ]; then
    OLD_APP_FILE="$OLD_APP_FILE_OLD_LOC"
else
    print_warn "Application class file not found in either location:"
    print_warn "  - $OLD_APP_FILE_NEW_LOC"
    print_warn "  - $OLD_APP_FILE_OLD_LOC"
    OLD_APP_FILE=""
fi

if [ -n "$OLD_APP_FILE" ] && [ -f "$OLD_APP_FILE" ]; then
    # Only rename if the file name actually changed
    if [ "$OLD_APP_FILE" != "$NEW_APP_FILE" ]; then
        mv "$OLD_APP_FILE" "$NEW_APP_FILE"
        print_info "Renamed application class file: $(basename "$OLD_APP_FILE") -> $(basename "$NEW_APP_FILE")"
    fi
    # Update class name inside the file
    sed -i.bak "s|class ${OLD_APP_CLASS}|class ${NEW_APP_CLASS}|g" "$NEW_APP_FILE"
    sed -i.bak "s|${OLD_APP_CLASS}\\.class|${NEW_APP_CLASS}.class|g" "$NEW_APP_FILE"
    rm -f "${NEW_APP_FILE}.bak"
    print_info "Updated application class: $OLD_APP_CLASS -> $NEW_APP_CLASS"
fi

# Rename test class if it exists
OLD_TEST_FILE_NEW_LOC="src/test/java/${NEW_PACKAGE_PATH}/${OLD_APP_CLASS}Tests.java"
OLD_TEST_FILE_OLD_LOC="src/test/java/${OLD_PACKAGE_PATH}/${OLD_APP_CLASS}Tests.java"
NEW_TEST_FILE="src/test/java/${NEW_PACKAGE_PATH}/${NEW_APP_CLASS}Tests.java"

# Determine which location has the test file
if [ -f "$OLD_TEST_FILE_NEW_LOC" ]; then
    OLD_TEST_FILE="$OLD_TEST_FILE_NEW_LOC"
elif [ -f "$OLD_TEST_FILE_OLD_LOC" ]; then
    OLD_TEST_FILE="$OLD_TEST_FILE_OLD_LOC"
else
    print_warn "Test class file not found (this is OK if no tests exist)"
    OLD_TEST_FILE=""
fi

if [ -n "$OLD_TEST_FILE" ] && [ -f "$OLD_TEST_FILE" ]; then
    # Only rename if the file name actually changed
    if [ "$OLD_TEST_FILE" != "$NEW_TEST_FILE" ]; then
        mv "$OLD_TEST_FILE" "$NEW_TEST_FILE"
        print_info "Renamed test class file: $(basename "$OLD_TEST_FILE") -> $(basename "$NEW_TEST_FILE")"
    fi
    # Update class name inside the test file
    sed -i.bak "s|class ${OLD_APP_CLASS}Tests|class ${NEW_APP_CLASS}Tests|g" "$NEW_TEST_FILE"
    sed -i.bak "s|${OLD_APP_CLASS}\\.class|${NEW_APP_CLASS}.class|g" "$NEW_TEST_FILE"
    rm -f "${NEW_TEST_FILE}.bak"
    print_info "Updated test class: ${OLD_APP_CLASS}Tests -> ${NEW_APP_CLASS}Tests"
fi

# Step 9: Update generate-crud.py
print_info "Updating generate-crud.py..."
if [ -f generate-crud.py ]; then
    sed -i.bak "s|com.valome.starter|${NEW_PACKAGE}|g" generate-crud.py
    rm -f generate-crud.py.bak
fi

# Step 10: Update README.md
print_info "Updating README.md..."
if [ -f README.md ]; then
    sed -i.bak "s|com.valome.starter|${NEW_PACKAGE}|g" README.md
    sed -i.bak "s|com/valome/starter|${NEW_PACKAGE_PATH}|g" README.md
    sed -i.bak "s|spring_starter|${NEW_DB_NAME}|g" README.md
    sed -i.bak "s|StarterApplication|${NEW_APP_CLASS}|g" README.md
    sed -i.bak "s|starter-0.0.1-SNAPSHOT|${NEW_NAME}-0.0.1-SNAPSHOT|g" README.md
    rm -f README.md.bak
fi

# Step 11: Update Postman collection if exists
print_info "Updating Postman collection..."
if [ -f starter.postman_collection.json ]; then
    NEW_POSTMAN_FILE="${NEW_NAME}.postman_collection.json"
    mv starter.postman_collection.json "$NEW_POSTMAN_FILE"
    sed -i.bak "s|\"starter\"|\"${NEW_NAME}\"|g" "$NEW_POSTMAN_FILE"
    rm -f "${NEW_POSTMAN_FILE}.bak"
fi

# Step 12: Update .gitignore if needed
print_info "Checking .gitignore..."
if [ -f .gitignore ]; then
    if grep -q "application.properties" .gitignore 2>/dev/null; then
        print_info ".gitignore already excludes application.properties"
    else
        print_warn ".gitignore does not exclude application.properties (consider adding it)"
    fi
else
    print_warn ".gitignore file not found"
fi

print_info ""
print_info "=========================================="
print_info "Project rename completed successfully!"
print_info "=========================================="
echo ""
print_info "Summary of changes:"
print_info "  - Package: ${OLD_PACKAGE} -> ${NEW_PACKAGE}"
print_info "  - Application class: ${OLD_APP_CLASS} -> ${NEW_APP_CLASS}"
print_info "  - Database name: ${OLD_DB_NAME} -> ${NEW_DB_NAME}"
print_info "  - Project name: ${OLD_NAME} -> ${NEW_NAME}"
echo ""
print_warn "Next steps:"
echo "  1. Review the changes: git diff"
echo "  2. Update database name in your PostgreSQL: CREATE DATABASE ${NEW_DB_NAME};"
echo "  3. Rebuild the project: ./gradlew clean build"
echo "  4. Test the application"
echo ""
print_info "Config backup saved as: ${CONFIG_FILE}.backup"

