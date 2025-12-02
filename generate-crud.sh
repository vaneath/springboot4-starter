#!/bin/bash

# MJQE Spring Boot CRUD Generator
# Usage: ./generate-crud.sh EntityName [fields]
# Example: ./generate-crud.sh Product "name:String:@NotBlank,price:BigDecimal:@NotNull,description:String"

if [ $# -eq 0 ]; then
    echo "Usage: $0 EntityName [fields]"
    echo ""
    echo "Examples:"
    echo "  $0 Product"
    echo "  $0 Category \"name:String:@NotBlank,description:String\""
    echo "  $0 Order \"orderNumber:String:@NotBlank,totalAmount:BigDecimal:@NotNull,orderDate:LocalDateTime\""
    echo ""
    echo "Field format: name:Type:@Annotation1;@Annotation2"
    echo "Supported types: String, Integer, Long, BigDecimal, LocalDate, LocalDateTime, Boolean"
    echo "Common annotations: @NotBlank, @NotNull, @Email, @Size(max=100), @Past, @Pattern(regexp=\"...\")"
    exit 1
fi

ENTITY_NAME="$1"
FIELDS="$2"

echo "🚀 Generating CRUD components for: $ENTITY_NAME"

if [ -n "$FIELDS" ]; then
    python3 generate-crud.py "$ENTITY_NAME" --fields "$FIELDS"
else
    python3 generate-crud.py "$ENTITY_NAME"
fi

exit_code=$?

if [ $exit_code -eq 0 ]; then
    echo ""
    echo "✨ Generation completed successfully!"
    echo ""
    echo "📁 Generated files structure:"
    echo "  📄 Model:        src/main/java/.../model/${ENTITY_NAME}.java"
    echo "  📄 Repository:   src/main/java/.../repository/jpa/${ENTITY_NAME}Repository.java"
    echo "  📄 Service:      src/main/java/.../service/$(echo $ENTITY_NAME | tr '[:upper:]' '[:lower:]')/${ENTITY_NAME}Service.java"
    echo "  📄 Service Impl: src/main/java/.../service/$(echo $ENTITY_NAME | tr '[:upper:]' '[:lower:]')/${ENTITY_NAME}ServiceImpl.java"
    echo "  📄 Mapper:       src/main/java/.../mapper/${ENTITY_NAME}Mapper.java"
    echo "  📄 Controller:   src/main/java/.../controller/${ENTITY_NAME}ApiController.java"
    echo "  📄 Create DTO:   src/main/java/.../dto/$(echo $ENTITY_NAME | tr '[:upper:]' '[:lower:]')/${ENTITY_NAME}CreateRequest.java"
    echo "  📄 Update DTO:   src/main/java/.../dto/$(echo $ENTITY_NAME | tr '[:upper:]' '[:lower:]')/${ENTITY_NAME}UpdateRequest.java"
    echo "  📄 Response DTO: src/main/java/.../dto/$(echo $ENTITY_NAME | tr '[:upper:]' '[:lower:]')/${ENTITY_NAME}Response.java"
    echo ""
    echo "🔧 Next steps:"
    echo "  1. Review and customize the generated files"
    echo "  2. Compile: ./gradlew compileJava"
    echo "  3. Test endpoints (check controller for exact route base):"
    echo "     POST   /{route-base}/search   - Search"
    echo "     POST   /{route-base}          - Create"
    echo "     GET    /{route-base}/{id}     - Get by ID"
    echo "     PUT    /{route-base}/{id}     - Update"
    echo "     DELETE /{route-base}/{id}     - Delete"
else
    echo "❌ Generation failed with exit code: $exit_code"
fi

exit $exit_code
