#!/usr/bin/env python3

import os
import sys
import argparse
from pathlib import Path

def detect_base_package_and_path():
    """Dynamically detect the base package and path from the project structure."""
    # Get the script's directory
    script_dir = Path(__file__).parent.absolute()
    
    # Find the project root (directory containing src/main/java)
    current = script_dir
    java_base = None
    
    # Traverse up to find src/main/java
    while current != current.parent:
        potential_java_base = current / "src" / "main" / "java"
        if potential_java_base.exists() and potential_java_base.is_dir():
            java_base = potential_java_base
            break
        current = current.parent
    
    if java_base is None:
        # Fallback: try relative to script location
        java_base = script_dir / "src" / "main" / "java"
        if not java_base.exists():
            raise ValueError("Could not find src/main/java directory. Please run the script from the project root.")
    
    # Find the base package by traversing down until we find a directory
    # that contains typical Spring Boot structure (model, controller, service, etc.)
    # or contains Java files directly
    base_path_parts = []
    current_dir = java_base
    
    # Common Spring Boot package indicators
    spring_boot_indicators = {'model', 'controller', 'service', 'repository', 'dto', 'config', 'exception', 'mapper', 'util', 'filter'}
    
    # Traverse down the directory structure
    while True:
        subdirs = [d for d in current_dir.iterdir() 
                  if d.is_dir() and not d.name.startswith('.')]
        
        if not subdirs:
            break
        
        # Check if current directory contains Spring Boot indicators or Java files
        current_dir_names = {d.name for d in current_dir.iterdir() if d.is_dir()}
        has_java_files = bool(list(current_dir.glob("*.java")))
        
        # If we find Spring Boot indicators or Java files, this directory IS the base package
        if current_dir_names.intersection(spring_boot_indicators) or has_java_files:
            break
        
        # Otherwise, continue traversing down (take the first subdirectory)
        next_dir = subdirs[0]
        base_path_parts.append(next_dir.name)
        current_dir = next_dir
    
    # Convert path parts to package name (join with dots)
    base_package = ".".join(base_path_parts) if base_path_parts else ""
    
    # Convert path parts to file system path
    if base_path_parts:
        base_path = "src/main/java/" + "/".join(base_path_parts)
    else:
        # If base_path_parts is empty, check if java_base itself contains the structure
        current_dir_names = {d.name for d in java_base.iterdir() if d.is_dir()}
        if current_dir_names.intersection(spring_boot_indicators):
            base_path = "src/main/java"
        else:
            raise ValueError("Could not detect base package. No Java package structure found in src/main/java")
    
    return base_package, base_path

# Configuration - dynamically detected
BASE_PACKAGE, BASE_PATH = detect_base_package_and_path()

def to_camel_case(snake_str):
    """Convert snake_case to CamelCase"""
    components = snake_str.split('_')
    return ''.join(word.capitalize() for word in components)

def to_snake_case(camel_str):
    """Convert CamelCase to snake_case"""
    import re
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', camel_str)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

def to_lower_camel_case(camel_str):
    """Convert CamelCase to lowerCamelCase"""
    return camel_str[0].lower() + camel_str[1:] if camel_str else ""

def pluralize(word):
    """
    Convert a word to its plural form following English grammar rules.
    
    Rules handled:
    - Words ending in s, sh, ch, x, z → add 'es'
    - Words ending in 'y' preceded by a consonant → replace 'y' with 'ies'
    - Words ending in 'y' preceded by a vowel → add 's'
    - Words ending in 'f' or 'fe' → replace with 'ves' (common cases)
    - Words ending in 'o' preceded by a consonant → add 'es'
    - Words ending in 'o' preceded by a vowel → add 's'
    - Default → add 's'
    """
    if not word:
        return word
    
    word_lower = word.lower()
    vowels = {'a', 'e', 'i', 'o', 'u'}
    
    # Words ending in s, sh, ch, x, z → add 'es'
    if word_lower.endswith(('s', 'sh', 'ch', 'x', 'z')):
        return word + 'es'
    
    # Words ending in 'y' preceded by a consonant → replace 'y' with 'ies'
    # Words ending in 'y' preceded by a vowel → add 's'
    if word_lower.endswith('y'):
        if len(word_lower) > 1 and word_lower[-2] not in vowels:
            return word[:-1] + 'ies'
        else:
            return word + 's'
    
    # Words ending in 'f' → replace with 'ves'
    if word_lower.endswith('f'):
        # Common exceptions: roof → roofs, chief → chiefs, etc.
        # But for most cases: wolf → wolves, leaf → leaves
        return word[:-1] + 'ves'
    
    # Words ending in 'fe' → replace with 'ves'
    if word_lower.endswith('fe'):
        return word[:-2] + 'ves'
    
    # Words ending in 'o' preceded by a consonant → add 'es'
    # Words ending in 'o' preceded by a vowel → add 's'
    if word_lower.endswith('o'):
        if len(word_lower) > 1 and word_lower[-2] not in vowels:
            return word + 'es'
        else:
            return word + 's'
    
    # Default: add 's'
    return word + 's'

class CrudGenerator:
    def __init__(self, entity_name, fields):
        self.entity_name = entity_name
        self.entity_lower = entity_name.lower()
        self.entity_camel = to_lower_camel_case(entity_name)
        # Pluralize table name (snake_case)
        snake_name = to_snake_case(entity_name)
        self.table_name = pluralize(snake_name)
        # Route base in kebab-case plural (e.g., MessageTemplate -> message-templates, Ability -> abilities)
        kebab = snake_name.replace('_', '-')
        self.route_base = pluralize(kebab)
        self.fields = self.parse_fields(fields)
        
        self.api_version = "v1"
        self.api_route = f"/{self.api_version}/{self.route_base}"

    def parse_fields(self, fields_str):
        """Parse field definitions like 'name:String:@NotBlank,age:Integer,email:String:@Email'"""
        fields = []
        if not fields_str:
            return fields
            
        for field_def in fields_str.split(','):
            parts = field_def.strip().split(':')
            if len(parts) >= 2:
                field_name = parts[0].strip()
                field_type = parts[1].strip()
                annotations = parts[2].split(';') if len(parts) > 2 else []
                fields.append({
                    'name': field_name,
                    'type': field_type,
                    'annotations': annotations,
                    'column_name': to_snake_case(field_name)
                })
        return fields
    
    def generate_model(self):
        """Generate JPA Entity Model"""
        imports = [
            "jakarta.persistence.*",
            f"{BASE_PACKAGE}.dto.search.FieldConfig",
            "lombok.*",
            "lombok.experimental.SuperBuilder",
            "java.util.List"
        ]
        
        # Add imports based on field types
        has_local_date_time = False
        for field in self.fields:
            if field['type'] in ['LocalDate', 'LocalDateTime']:
                imports.append(f"java.time.{field['type']}")
                if field['type'] == 'LocalDateTime':
                    has_local_date_time = True
            elif field['type'] == 'BigDecimal':
                imports.append("java.math.BigDecimal")
        
        # Always import LocalDateTime for createdAt field in PAGINATION_FIELDS
        if not has_local_date_time:
            imports.append("java.time.LocalDateTime")
        
        fields_code = ""
        for field in self.fields:
            fields_code += f"\n    @Column(name = \"{field['column_name']}\")\n"
            fields_code += f"    private {field['type']} {field['name']};\n"
        
        # Generate PAGINATION_FIELDS constant
        pagination_fields = []
        for field in self.fields:
            field_type = field['type']
            # Map Java type string to Class reference
            if field_type == 'String':
                type_class = 'String.class'
            elif field_type == 'Integer':
                type_class = 'Integer.class'
            elif field_type == 'Long':
                type_class = 'Long.class'
            elif field_type == 'BigDecimal':
                type_class = 'BigDecimal.class'
            elif field_type == 'Boolean':
                type_class = 'Boolean.class'
            elif field_type == 'LocalDate':
                type_class = 'LocalDate.class'
            elif field_type == 'LocalDateTime':
                type_class = 'LocalDateTime.class'
            else:
                type_class = 'String.class'  # Default
            
            pagination_fields.append(f'            new FieldConfig("{field["name"]}", {type_class}, true, true)')
        
        # Add createdAt field (filterable but not searchable, like in User model)
        pagination_fields.append('            new FieldConfig("createdAt", LocalDateTime.class, false, true)')
        
        pagination_fields_str = ',\n'.join(pagination_fields)
        
        template = f"""package {BASE_PACKAGE}.model;

{chr(10).join(f'import {imp};' for imp in sorted(set(imports)))}

/**
 * {self.entity_name} entity.
 * 
 * Represents {self.entity_lower} data with full audit trail support.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "{self.table_name}")
public class {self.entity_name} extends BaseModel {{
{fields_code}
    /**
     * Field configurations for pagination, filtering, and searching.
     * All fields are searchable and filterable by default.
     */
    public static final List<FieldConfig> PAGINATION_FIELDS = List.of(
{pagination_fields_str});
}}
"""
        return template
    
    def generate_repository(self):
        """Generate JPA Repository"""
        template = f"""package {BASE_PACKAGE}.repository.jpa;

import org.springframework.stereotype.Repository;

import {BASE_PACKAGE}.model.{self.entity_name};
import com.valome.starter.repository.jpa.core.BaseRepository;

/**
 * Repository interface for {self.entity_name} entity operations.
 * 
 * Provides standard CRUD operations and specification support for advanced querying.
 */
@Repository
public interface {self.entity_name}Repository extends BaseRepository<{self.entity_name}, Long> {{
}}
"""
        return template
    
    
    def generate_create_request(self):
        """Generate Create Request DTO"""
        imports = ["jakarta.validation.constraints.*", "lombok.Data", "lombok.EqualsAndHashCode"]
        imports.append(f"{BASE_PACKAGE}.dto.core.BaseRequest")
        
        for field in self.fields:
            if field['type'] in ['LocalDate', 'LocalDateTime']:
                imports.append(f"java.time.{field['type']}")
            elif field['type'] == 'BigDecimal':
                imports.append("java.math.BigDecimal")
        
        fields_code = ""
        for field in self.fields:
            # Get user-provided annotations
            user_annotations = [ann.strip() for ann in field['annotations']] if field['annotations'] else []
            
            # Determine smart defaults based on field type
            smart_defaults = []
            field_type = field['type']
            
            # Check if user already provided @NotBlank or @NotNull
            has_not_blank = any(ann.startswith('@NotBlank') for ann in user_annotations)
            has_not_null = any(ann.startswith('@NotNull') for ann in user_annotations)
            
            # Apply smart defaults if user hasn't provided them
            if not has_not_blank and not has_not_null:
                if field_type == 'String':
                    smart_defaults.append("@NotBlank")
                elif field_type in ['Long', 'Integer', 'BigDecimal', 'LocalDate', 'LocalDateTime']:
                    smart_defaults.append("@NotNull")
            
            # Combine user annotations and smart defaults
            all_annotations = user_annotations + smart_defaults
            
            # Write annotations
            for annotation in all_annotations:
                fields_code += f"    {annotation}\n"
            
            fields_code += f"    private {field['type']} {field['name']};\n\n"
        
        template = f"""package {BASE_PACKAGE}.dto.{self.entity_lower};

{chr(10).join(f'import {imp};' for imp in sorted(set(imports)))}

/**
 * DTO for creating a new {self.entity_lower}.
 * 
 * Contains validation constraints to ensure data integrity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class {self.entity_name}CreateRequest extends BaseRequest {{

{fields_code}}}
"""
        return template
    
    def generate_update_request(self):
        """Generate Update Request DTO"""
        imports = ["jakarta.validation.constraints.*", "lombok.Data", "lombok.EqualsAndHashCode"]
        imports.append(f"{BASE_PACKAGE}.dto.core.BaseRequest")
        
        for field in self.fields:
            if field['type'] in ['LocalDate', 'LocalDateTime']:
                imports.append(f"java.time.{field['type']}")
            elif field['type'] == 'BigDecimal':
                imports.append("java.math.BigDecimal")
        
        fields_code = ""
        for field in self.fields:
            # Get user-provided annotations, filtering out @NotBlank and @NotNull
            user_annotations = []
            if field['annotations']:
                user_annotations = [ann.strip() for ann in field['annotations'] 
                                  if not ann.strip().startswith('@NotBlank') 
                                  and not ann.strip().startswith('@NotNull')]
            
            # Determine smart defaults for optional validation
            smart_defaults = []
            field_type = field['type']
            
            # Apply optional validation constraints
            if field_type == 'String':
                # Add @Size for strings if not already present
                has_size = any(ann.startswith('@Size') for ann in user_annotations)
                if not has_size:
                    smart_defaults.append("@Size(max = 255)")
            elif field_type in ['Integer', 'Long']:
                # Add @Min(0) for numeric types if not already present
                has_min = any(ann.startswith('@Min') for ann in user_annotations)
                if not has_min:
                    smart_defaults.append("@Min(0)")
            
            # Combine user annotations and smart defaults
            all_annotations = user_annotations + smart_defaults
            
            # Write annotations
            for annotation in all_annotations:
                fields_code += f"    {annotation}\n"
            
            fields_code += f"    private {field['type']} {field['name']};\n\n"
        
        template = f"""package {BASE_PACKAGE}.dto.{self.entity_lower};

{chr(10).join(f'import {imp};' for imp in sorted(set(imports)))}

/**
 * DTO for updating an existing {self.entity_lower}.
 * 
 * All fields are optional to support partial updates.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class {self.entity_name}UpdateRequest extends BaseRequest {{

{fields_code}}}
"""
        return template
    
    def generate_response(self):
        """Generate Response DTO"""
        imports = ["lombok.Data", "lombok.EqualsAndHashCode"]
        imports.append(f"{BASE_PACKAGE}.dto.core.BaseResponse")
        
        for field in self.fields:
            if field['type'] in ['LocalDate', 'LocalDateTime']:
                imports.append(f"java.time.{field['type']}")
            elif field['type'] == 'BigDecimal':
                imports.append("java.math.BigDecimal")
        
        fields_code = ""
        for field in self.fields:
            fields_code += f"    private {field['type']} {field['name']};\n"
        
        template = f"""package {BASE_PACKAGE}.dto.{self.entity_lower};

{chr(10).join(f'import {imp};' for imp in sorted(set(imports)))}

/**
 * DTO for {self.entity_name} responses.
 * 
 * Contains all {self.entity_lower} information including audit fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class {self.entity_name}Response extends BaseResponse {{
{fields_code}}}
"""
        return template
    
    def generate_service_interface(self):
        """Generate Service Interface"""
        template = f"""package {BASE_PACKAGE}.service.{self.entity_lower};

import org.springframework.data.domain.Page;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.dto.search.PaginationRequest;

/**
 * Service interface for {self.entity_name} management operations.
 * 
 * Defines the contract for {self.entity_lower}-related business operations including
 * CRUD operations and search functionality.
 */
public interface {self.entity_name}Service {{

    /**
     * Searches {self.entity_lower}s with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts, page, and size
     * @return page of {self.entity_lower} responses matching the criteria
     */
    Page<{self.entity_name}Response> search(PaginationRequest request);

    /**
     * Creates a new {self.entity_lower}.
     * 
     * @param request the {self.entity_lower} creation request
     * @return the created {self.entity_lower} response
     */
    {self.entity_name}Response create({self.entity_name}CreateRequest request);

    /**
     * Retrieves a {self.entity_lower} by ID.
     * 
     * @param id the {self.entity_lower} ID
     * @return the {self.entity_lower} response
     * @throws ResourceNotFoundException if {self.entity_lower} not found
     */
    {self.entity_name}Response getById(Long id);

    /**
     * Updates an existing {self.entity_lower}.
     * 
     * @param id      the {self.entity_lower} ID to update
     * @param request the update request
     * @return the updated {self.entity_lower} response
     * @throws ResourceNotFoundException if {self.entity_lower} not found
     */
    {self.entity_name}Response update(Long id, {self.entity_name}UpdateRequest request);

    /**
     * Soft deletes a {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to delete
     * @throws ResourceNotFoundException if {self.entity_lower} not found
     */
    void delete(Long id);
}}
"""
        return template
    
    def generate_service_impl(self):
        """Generate Service Implementation"""
        template = f"""package {BASE_PACKAGE}.service.{self.entity_lower};

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.dto.search.PaginationRequest;
import {BASE_PACKAGE}.mapper.{self.entity_name}Mapper;
import {BASE_PACKAGE}.model.{self.entity_name};
import {BASE_PACKAGE}.repository.jpa.{self.entity_name}Repository;
import {BASE_PACKAGE}.service.search.PaginationService;
import {BASE_PACKAGE}.exception.ResourceNotFoundException;

/**
 * Implementation of {self.entity_name}Service interface.
 * 
 * Provides business logic for {self.entity_lower} management operations including
 * validation and data transformation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class {self.entity_name}ServiceImpl implements {self.entity_name}Service {{

    private final {self.entity_name}Repository {self.entity_camel}Repository;
    private final {self.entity_name}Mapper {self.entity_camel}Mapper;
    private final PaginationService paginationService;

    @Override
    @Transactional(readOnly = true)
    public Page<{self.entity_name}Response> search(PaginationRequest request) {{
        log.debug("Searching {self.entity_lower}s with request: {{}}", request);
        
        Page<{self.entity_name}> {self.entity_camel}Page = paginationService.search(
                request, 
                {self.entity_camel}Repository, 
                {self.entity_name}.PAGINATION_FIELDS);
        
        return {self.entity_camel}Page.map({self.entity_camel}Mapper::toResponse);
    }}

    @Override
    public {self.entity_name}Response create({self.entity_name}CreateRequest request) {{
        log.info("Creating new {self.entity_lower}");

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Mapper.toEntity(request);
        {self.entity_camel} = {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Created {self.entity_lower} with ID: {{}}", {self.entity_camel}.getId());
        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    @Transactional(readOnly = true)
    public {self.entity_name}Response getById(Long id) {{
        log.debug("Fetching {self.entity_lower} by ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    public {self.entity_name}Response update(Long id, {self.entity_name}UpdateRequest request) {{
        log.info("Updating {self.entity_lower} with ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        {self.entity_camel}Mapper.updateEntity({self.entity_camel}, request);
        {self.entity_camel} = {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Updated {self.entity_lower} with ID: {{}}", id);
        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    public void delete(Long id) {{
        log.info("Deleting {self.entity_lower} with ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        {self.entity_camel}.softDelete();
        {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Deleted {self.entity_lower} with ID: {{}}", id);
    }}
}}
"""
        return template
    
    def generate_mapper(self):
        """Generate MapStruct Mapper"""
        template = f"""package {BASE_PACKAGE}.mapper;

import org.mapstruct.*;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.model.{self.entity_name};

import java.util.List;

/**
 * MapStruct mapper for {self.entity_name} entity conversions.
 * 
 * Handles mapping between {self.entity_name} entities and DTOs with proper null handling
 * and custom mapping logic.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, 
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface {self.entity_name}Mapper {{

    /**
     * Maps {self.entity_name}CreateRequest to {self.entity_name} entity.
     * 
     * @param request the create request DTO
     * @return new {self.entity_name} entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    {self.entity_name} toEntity({self.entity_name}CreateRequest request);

    /**
     * Maps {self.entity_name} entity to {self.entity_name}Response DTO.
     * 
     * @param {self.entity_camel} the {self.entity_name} entity
     * @return {self.entity_name}Response DTO
     */
    {self.entity_name}Response toResponse({self.entity_name} {self.entity_camel});

    /**
     * Maps list of {self.entity_name} entities to list of {self.entity_name}Response DTOs.
     * 
     * @param {self.entity_camel}s list of {self.entity_name} entities
     * @return list of {self.entity_name}Response DTOs
     */
    List<{self.entity_name}Response> toResponseList(List<{self.entity_name}> {self.entity_camel}s);

    /**
     * Updates existing {self.entity_name} entity with data from {self.entity_name}UpdateRequest.
     * 
     * @param {self.entity_camel} the existing {self.entity_name} entity to update
     * @param request the update request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(@MappingTarget {self.entity_name} {self.entity_camel}, {self.entity_name}UpdateRequest request);
}}
"""
        return template
    
    def generate_controller(self):
        """Generate REST Controller"""
        template = f"""package {BASE_PACKAGE}.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import {BASE_PACKAGE}.dto.core.SuccessResponse;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.dto.search.PaginationRequest;
import {BASE_PACKAGE}.service.{self.entity_lower}.{self.entity_name}Service;
import {BASE_PACKAGE}.util.ResponseHandler;

/**
 * REST controller for {self.entity_name} management operations.
 * 
 * Provides RESTful endpoints for CRUD operations on {self.entity_lower}s.
 */
@RestController
@RequestMapping("{self.api_route}")
@RequiredArgsConstructor
@Validated
@Slf4j
public class {self.entity_name}ApiController {{
    private final {self.entity_name}Service {self.entity_camel}Service;

    /**
     * Searches {self.entity_lower}s with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts, page, and size
     * @return page of {self.entity_lower} responses with HTTP 200
     */
    @PostMapping("/search")
    public ResponseEntity<SuccessResponse<Page<{self.entity_name}Response>>> search(@RequestBody PaginationRequest request) {{
        log.info("REST request to search {self.entity_lower}s - request: {{}}", request);
        Page<{self.entity_name}Response> response = {self.entity_camel}Service.search(request);
        return ResponseHandler.success("{self.entity_name}s retrieved successfully", response);
    }}

    /**
     * Creates a new {self.entity_lower}.
     * 
     * @param request the {self.entity_lower} creation request
     * @return created {self.entity_lower} response with HTTP 200
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<{self.entity_name}Response>> create(@Valid @RequestBody {self.entity_name}CreateRequest request) {{
        log.info("REST request to create {self.entity_lower}");

        {self.entity_name}Response response = {self.entity_camel}Service.create(request);
        return ResponseHandler.success("{self.entity_name} created successfully", response);
    }}

    /**
     * Retrieves a {self.entity_lower} by ID.
     * 
     * @param id the {self.entity_lower} ID
     * @return {self.entity_lower} response with HTTP 200
     */
    @GetMapping("/{{id}}")
    public ResponseEntity<SuccessResponse<{self.entity_name}Response>> getById(@PathVariable Long id) {{
        log.debug("REST request to get {self.entity_lower} by ID: {{}}", id);

        {self.entity_name}Response response = {self.entity_camel}Service.getById(id);
        return ResponseHandler.success("{self.entity_name} retrieved successfully", response);
    }}

    /**
     * Updates an existing {self.entity_lower}.
     * 
     * @param id      the {self.entity_lower} ID to update
     * @param request the update request
     * @return updated {self.entity_lower} response with HTTP 200
     */
    @PutMapping("/{{id}}")
    public ResponseEntity<SuccessResponse<{self.entity_name}Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody {self.entity_name}UpdateRequest request) {{
        log.info("REST request to update {self.entity_lower} with ID: {{}}", id);

        {self.entity_name}Response response = {self.entity_camel}Service.update(id, request);
        return ResponseHandler.success("{self.entity_name} updated successfully", response);
    }}

    /**
     * Soft deletes a {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to delete
     * @return HTTP 200 with success message
     */
    @DeleteMapping("/{{id}}")
    public ResponseEntity<SuccessResponse<Object>> delete(@PathVariable Long id) {{
        log.info("REST request to delete {self.entity_lower} with ID: {{}}", id);

        {self.entity_camel}Service.delete(id);
        return ResponseHandler.success("{self.entity_name} deleted successfully");
    }}
}}
"""
        return template
    
    def create_directories(self):
        """Create necessary directories"""
        directories = [
            f"{BASE_PATH}/model",
            f"{BASE_PATH}/repository/jpa",
            f"{BASE_PATH}/dto/{self.entity_lower}",
            f"{BASE_PATH}/service/{self.entity_lower}",
            f"{BASE_PATH}/mapper",
            f"{BASE_PATH}/controller"
        ]
        
        for directory in directories:
            os.makedirs(directory, exist_ok=True)
    
    def generate_all(self):
        """Generate all CRUD components"""
        self.create_directories()
        
        components = {
            f"{BASE_PATH}/model/{self.entity_name}.java": self.generate_model(),
            f"{BASE_PATH}/repository/jpa/{self.entity_name}Repository.java": self.generate_repository(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}CreateRequest.java": self.generate_create_request(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}UpdateRequest.java": self.generate_update_request(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}Response.java": self.generate_response(),
            f"{BASE_PATH}/service/{self.entity_lower}/{self.entity_name}Service.java": self.generate_service_interface(),
            f"{BASE_PATH}/service/{self.entity_lower}/{self.entity_name}ServiceImpl.java": self.generate_service_impl(),
            f"{BASE_PATH}/mapper/{self.entity_name}Mapper.java": self.generate_mapper(),
            f"{BASE_PATH}/controller/{self.entity_name}ApiController.java": self.generate_controller(),
        }
        
        created_files = []
        for file_path, content in components.items():
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            created_files.append(file_path)
            
        return created_files

def main():
    parser = argparse.ArgumentParser(description='Generate Spring Boot CRUD components')
    parser.add_argument('entity_name', help='Entity name (e.g., Product)')
    parser.add_argument('--fields', help='Field definitions (e.g., "name:String:@NotBlank,price:BigDecimal:@NotNull,description:String")')
    
    args = parser.parse_args()
    
    if not args.entity_name:
        print("Error: Entity name is required")
        return 1
        
    generator = CrudGenerator(args.entity_name, args.fields or "")
    
    print(f"Generating CRUD components for entity: {args.entity_name}")
    if args.fields:
        print(f"Fields: {args.fields}")
    
    try:
        created_files = generator.generate_all()
        
        print("\n✅ Successfully generated the following files:")
        for file_path in created_files:
            print(f"  - {file_path}")
            
        print(f"\n🎉 CRUD generation completed for {args.entity_name}!")
        print(f"\nNext steps:")
        print("1. Review and customize the generated files")
        print("2. Compile the project: ./gradlew compileJava")
        print("3. Test the endpoints using your API client")
        
        return 0
        
    except Exception as e:
        print(f"❌ Error generating CRUD components: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
