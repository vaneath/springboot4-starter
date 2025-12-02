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

class CrudGenerator:
    def __init__(self, entity_name, fields):
        self.entity_name = entity_name
        self.entity_lower = entity_name.lower()
        self.entity_camel = to_lower_camel_case(entity_name)
        self.table_name = to_snake_case(entity_name).replace('_', '_') + 's'  # pluralize
        # Route base in kebab-case plural (e.g., MessageTemplate -> message-templates, Ability -> abilities)
        kebab = to_snake_case(entity_name).replace('_', '-')
        if kebab.endswith('y'):
            self.route_base = kebab[:-1] + 'ies'
        else:
            self.route_base = kebab + 's'
        self.fields = self.parse_fields(fields)
        
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
            f"{BASE_PACKAGE}.model.core.BaseModel",
            "lombok.*",
            "lombok.experimental.SuperBuilder"
        ]
        
        # Add imports based on field types
        for field in self.fields:
            if field['type'] in ['LocalDate', 'LocalDateTime']:
                imports.append(f"java.time.{field['type']}")
            elif field['type'] == 'BigDecimal':
                imports.append("java.math.BigDecimal")
        
        fields_code = ""
        for field in self.fields:
            fields_code += f"\n    @Column(name = \"{field['column_name']}\")\n"
            fields_code += f"    private {field['type']} {field['name']};\n"
        
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
{fields_code}}}
"""
        return template
    
    def generate_repository(self):
        """Generate JPA Repository"""
        template = f"""package {BASE_PACKAGE}.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import {BASE_PACKAGE}.model.{self.entity_name};

import java.util.Optional;

/**
 * Repository interface for {self.entity_name} entity operations.
 * 
 * Provides standard CRUD operations plus custom queries for {self.entity_lower} management.
 * Includes soft delete support.
 */
@Repository
public interface {self.entity_name}Repository extends JpaRepository<{self.entity_name}, Long> {{

    /**
     * Finds all active (non-deleted) {self.entity_lower}s.
     * 
     * @param pageable pagination information
     * @return Page of active {self.entity_lower}s
     */
    @Query("SELECT e FROM {self.entity_name} e WHERE e.deletedAt IS NULL")
    Page<{self.entity_name}> findAllActive(Pageable pageable);

    /**
     * Finds an active {self.entity_lower} by ID.
     * 
     * @param id the {self.entity_lower} ID
     * @return Optional containing the {self.entity_lower} if found and not deleted
     */
    @Query("SELECT e FROM {self.entity_name} e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<{self.entity_name}> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Searches {self.entity_lower}s by various criteria.
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return Page of matching {self.entity_lower}s
     */
    @Query("SELECT e FROM {self.entity_name} e WHERE e.deletedAt IS NULL AND " +
           "(LOWER(CAST(e.id AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<{self.entity_name}> search{self.entity_name}s(@Param("searchTerm") String searchTerm, Pageable pageable);
}}
"""
        return template
    
    def generate_jdbc_repository(self):
        """Generate JDBC Repository for advanced pagination"""
        # Build ALLOWED_COLUMNS map entries
        columns_map = []
        columns_map.append('            "id", FilterType.NUMBER')
        columns_map.append('            "created_at", FilterType.DATE')
        
        for field in self.fields:
            column_name = field['column_name']
            field_type = field['type']
            
            # Map Java types to FilterType
            if field_type in ['String']:
                filter_type = 'FilterType.STRING'
            elif field_type in ['Long', 'Integer', 'BigDecimal']:
                filter_type = 'FilterType.NUMBER'
            elif field_type in ['LocalDate', 'LocalDateTime']:
                filter_type = 'FilterType.DATE'
            elif field_type == 'Boolean':
                filter_type = 'FilterType.BOOLEAN'
            else:
                filter_type = 'FilterType.STRING'  # Default to STRING
            
            columns_map.append(f'            "{column_name}", {filter_type}')
        
        columns_map_str = ',\n'.join(columns_map)
        
        template = f"""package {BASE_PACKAGE}.repository.jdbc;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.enums.FilterType;
import {BASE_PACKAGE}.service.core.PaginationQueryService;
import {BASE_PACKAGE}.util.FilterRequest;
import {BASE_PACKAGE}.util.ValidationColumnUtil;
import lombok.RequiredArgsConstructor;

/**
 * JDBC Repository for {self.entity_name} advanced pagination and filtering.
 * 
 * Provides custom SQL-based pagination with column validation.
 */
@Repository
@RequiredArgsConstructor
public class {self.entity_name}JdbcRepository {{
    private final PaginationQueryService paginationQueryService;

    private static final Map<String, FilterType> ALLOWED_COLUMNS = Map.of(
{columns_map_str}
    );

    /**
     * Retrieves paginated {self.entity_lower}s with advanced filtering.
     * 
     * @param filter the filter request containing pagination, sorting, and search criteria
     * @return page of {self.entity_name}Response matching the filter criteria
     */
    public Page<{self.entity_name}Response> getPaginated{self.entity_name}s(FilterRequest filter) {{
        ValidationColumnUtil.validate(filter, ALLOWED_COLUMNS);

        String sql = "SELECT * FROM {self.table_name} WHERE deleted_at IS NULL";
        String countSql = "SELECT COUNT(*) FROM {self.table_name} WHERE deleted_at IS NULL";

        return paginationQueryService.executeQuery(
                filter,
                new StringBuilder(sql),
                new StringBuilder(countSql),
                {self.entity_name}Response.class);
    }}
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
        template = f"""package {BASE_PACKAGE}.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.util.FilterRequest;

/**
 * Service interface for {self.entity_name} management operations.
 * 
 * Defines the contract for {self.entity_lower}-related business operations including
 * CRUD operations and search functionality.
 */
public interface {self.entity_name}Service {{

    /**
     * Retrieves paginated {self.entity_lower}s with advanced filtering.
     * 
     * @param filter the filter request containing pagination, sorting, and search criteria
     * @return page of {self.entity_lower} responses matching the filter criteria
     */
    Page<{self.entity_name}Response> getPaginated{self.entity_name}s(FilterRequest filter);

    /**
     * Creates a new {self.entity_lower}.
     * 
     * @param request the {self.entity_lower} creation request
     * @return the created {self.entity_lower} response
     */
    {self.entity_name}Response create{self.entity_name}({self.entity_name}CreateRequest request);

    /**
     * Retrieves a {self.entity_lower} by ID.
     * 
     * @param id the {self.entity_lower} ID
     * @return the {self.entity_lower} response
     * @throws IllegalArgumentException if {self.entity_lower} not found or deleted
     */
    {self.entity_name}Response get{self.entity_name}ById(Long id);

    /**
     * Retrieves all active {self.entity_lower}s with pagination.
     * 
     * @param pageable pagination information
     * @return page of {self.entity_lower} responses
     */
    Page<{self.entity_name}Response> getAll{self.entity_name}s(Pageable pageable);

    /**
     * Searches {self.entity_lower}s by term.
     * 
     * @param searchTerm the search term
     * @param pageable   pagination information
     * @return page of matching {self.entity_lower} responses
     */
    Page<{self.entity_name}Response> search{self.entity_name}s(String searchTerm, Pageable pageable);

    /**
     * Updates an existing {self.entity_lower}.
     * 
     * @param id      the {self.entity_lower} ID to update
     * @param request the update request
     * @return the updated {self.entity_lower} response
     * @throws IllegalArgumentException if {self.entity_lower} not found or deleted
     */
    {self.entity_name}Response update{self.entity_name}(Long id, {self.entity_name}UpdateRequest request);

    /**
     * Soft deletes a {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to delete
     * @throws IllegalArgumentException if {self.entity_lower} not found or already deleted
     */
    void delete{self.entity_name}(Long id);

    /**
     * Restores a soft-deleted {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to restore
     * @throws IllegalArgumentException if {self.entity_lower} not found or not deleted
     */
    void restore{self.entity_name}(Long id);
}}
"""
        return template
    
    def generate_service_impl(self):
        """Generate Service Implementation"""
        template = f"""package {BASE_PACKAGE}.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.mapper.{self.entity_name}Mapper;
import {BASE_PACKAGE}.model.{self.entity_name};
import {BASE_PACKAGE}.repository.jpa.{self.entity_name}Repository;
import {BASE_PACKAGE}.repository.jdbc.{self.entity_name}JdbcRepository;
import {BASE_PACKAGE}.service.{self.entity_name}Service;
import {BASE_PACKAGE}.exception.ResourceNotFoundException;
import {BASE_PACKAGE}.util.FilterRequest;

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
    private final {self.entity_name}JdbcRepository {self.entity_camel}JdbcRepository;
    private final {self.entity_name}Mapper {self.entity_camel}Mapper;

    @Override
    public Page<{self.entity_name}Response> getPaginated{self.entity_name}s(FilterRequest filter) {{
        return {self.entity_camel}JdbcRepository.getPaginated{self.entity_name}s(filter);
    }}

    @Override
    public {self.entity_name}Response create{self.entity_name}({self.entity_name}CreateRequest request) {{
        log.info("Creating new {self.entity_lower}");

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Mapper.toEntity(request);
        {self.entity_camel} = {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Created {self.entity_lower} with ID: {{}}", {self.entity_camel}.getId());
        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    @Transactional(readOnly = true)
    public {self.entity_name}Response get{self.entity_name}ById(Long id) {{
        log.debug("Fetching {self.entity_lower} by ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    @Transactional(readOnly = true)
    public Page<{self.entity_name}Response> getAll{self.entity_name}s(Pageable pageable) {{
        log.debug("Fetching all {self.entity_lower}s with pagination: {{}}", pageable);

        Page<{self.entity_name}> {self.entity_camel}Page = {self.entity_camel}Repository.findAllActive(pageable);
        return {self.entity_camel}Page.map({self.entity_camel}Mapper::toResponse);
    }}

    @Override
    @Transactional(readOnly = true)
    public Page<{self.entity_name}Response> search{self.entity_name}s(String searchTerm, Pageable pageable) {{
        log.debug("Searching {self.entity_lower}s with term: '{{}}', pagination: {{}}", searchTerm, pageable);

        Page<{self.entity_name}> {self.entity_camel}Page = {self.entity_camel}Repository.search{self.entity_name}s(searchTerm, pageable);
        return {self.entity_camel}Page.map({self.entity_camel}Mapper::toResponse);
    }}

    @Override
    public {self.entity_name}Response update{self.entity_name}(Long id, {self.entity_name}UpdateRequest request) {{
        log.info("Updating {self.entity_lower} with ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        {self.entity_camel}Mapper.updateEntity({self.entity_camel}, request);
        {self.entity_camel} = {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Updated {self.entity_lower} with ID: {{}}", id);
        return {self.entity_camel}Mapper.toResponse({self.entity_camel});
    }}

    @Override
    public void delete{self.entity_name}(Long id) {{
        log.info("Deleting {self.entity_lower} with ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        {self.entity_camel}.setDeletedAt(LocalDateTime.now());
        {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Deleted {self.entity_lower} with ID: {{}}", id);
    }}

    @Override
    public void restore{self.entity_name}(Long id) {{
        log.info("Restoring {self.entity_lower} with ID: {{}}", id);

        {self.entity_name} {self.entity_camel} = {self.entity_camel}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{self.entity_name} not found with ID: " + id));

        if ({self.entity_camel}.getDeletedAt() == null) {{
            throw new IllegalArgumentException("{self.entity_name} is not deleted");
        }}

        {self.entity_camel}.setDeletedAt(null);
        {self.entity_camel}Repository.save({self.entity_camel});

        log.info("Restored {self.entity_lower} with ID: {{}}", id);
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

import java.time.LocalDateTime;
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
    @Mapping(target = "deleted", source = "deletedAt", qualifiedByName = "mapDeleted")
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

    /**
     * Maps deletedAt timestamp to boolean deleted flag.
     */
    @Named("mapDeleted")
    default boolean mapDeleted(LocalDateTime deletedAt) {{
        return deletedAt != null;
    }}
}}
"""
        return template
    
    def generate_controller(self):
        """Generate REST Controller"""
        template = f"""package {BASE_PACKAGE}.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}CreateRequest;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}Response;
import {BASE_PACKAGE}.dto.{self.entity_lower}.{self.entity_name}UpdateRequest;
import {BASE_PACKAGE}.service.{self.entity_name}Service;
import {BASE_PACKAGE}.util.FilterRequest;
import {BASE_PACKAGE}.util.HttpResponseHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for {self.entity_name} management operations.
 * 
 * Provides RESTful endpoints for complete CRUD operations on {self.entity_lower}s.
 * 
 * Base URL: /api/v2/{self.route_base}
 */
@RestController
@RequestMapping("/{self.route_base}")
@RequiredArgsConstructor
@Validated
@Slf4j
public class {self.entity_name}ApiController {{
    private final {self.entity_name}Service {self.entity_camel}Service;

    /**
     * Retrieves paginated {self.entity_lower}s with advanced filtering and search capabilities.
     * 
     * @param filter the filter request containing pagination, sorting, and search criteria
     * @return page of {self.entity_lower} responses with HTTP 200
     */
    @PostMapping("/list")
    public ResponseEntity<Object> getPaginated{self.entity_name}s(@Valid @RequestBody FilterRequest filter) {{
        log.info("REST request to get paginated {self.entity_lower}s - filter: {{}}", filter);
        Page<{self.entity_name}Response> response = {self.entity_camel}Service.getPaginated{self.entity_name}s(filter);
        return HttpResponseHandler.success("{self.entity_name}s retrieved successfully", response);
    }}

    /**
     * Creates a new {self.entity_lower}.
     * 
     * @param request the {self.entity_lower} creation request
     * @return created {self.entity_lower} response with HTTP 201
     */
    @PostMapping
    public ResponseEntity<Object> create{self.entity_name}(@Valid @RequestBody {self.entity_name}CreateRequest request) {{
        log.info("REST request to create {self.entity_lower}");

        {self.entity_name}Response response = {self.entity_camel}Service.create{self.entity_name}(request);
        return HttpResponseHandler.created("{self.entity_name} created successfully", response);
    }}

    /**
     * Retrieves a {self.entity_lower} by ID.
     * 
     * @param id the {self.entity_lower} ID
     * @return {self.entity_lower} response with HTTP 200
     */
    @GetMapping("/{{id}}")
    public ResponseEntity<Object> get{self.entity_name}ById(@PathVariable Long id) {{
        log.debug("REST request to get {self.entity_lower} by ID: {{}}", id);

        {self.entity_name}Response response = {self.entity_camel}Service.get{self.entity_name}ById(id);
        return HttpResponseHandler.success("{self.entity_name} retrieved successfully", response);
    }}

    /**
     * Retrieves all {self.entity_lower}s with pagination and sorting.
     * 
     * @param page page number (0-based, default: 0)
     * @param size page size (default: 20)
     * @param sort sort criteria (default: id,asc)
     * @return page of {self.entity_lower} responses with HTTP 200
     */
    @GetMapping
    public ResponseEntity<Object> getAll{self.entity_name}s(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id,asc") String sort) {{
        log.debug("REST request to get all {self.entity_lower}s - page: {{}}, size: {{}}, sort: {{}}", page, size, sort);

        Pageable pageable = createPageable(page, size, sort);
        Page<{self.entity_name}Response> response = {self.entity_camel}Service.getAll{self.entity_name}s(pageable);
        return HttpResponseHandler.success("{self.entity_name}s retrieved successfully", response);
    }}

    /**
     * Searches {self.entity_lower}s by term.
     * 
     * @param searchTerm the search term
     * @param page       page number (0-based, default: 0)
     * @param size       page size (default: 20)
     * @param sort       sort criteria (default: id,asc)
     * @return page of matching {self.entity_lower} responses with HTTP 200
     */
    @GetMapping("/search")
    public ResponseEntity<Object> search{self.entity_name}s(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id,asc") String sort) {{
        log.debug("REST request to search {self.entity_lower}s with term: '{{}}' - page: {{}}, size: {{}}, sort: {{}}",
                searchTerm, page, size, sort);

        Pageable pageable = createPageable(page, size, sort);
        Page<{self.entity_name}Response> response = {self.entity_camel}Service.search{self.entity_name}s(searchTerm, pageable);
        return HttpResponseHandler.success("{self.entity_name}s search completed successfully", response);
    }}

    /**
     * Updates an existing {self.entity_lower}.
     * 
     * @param id      the {self.entity_lower} ID to update
     * @param request the update request
     * @return updated {self.entity_lower} response with HTTP 200
     */
    @PutMapping("/{{id}}")
    public ResponseEntity<Object> update{self.entity_name}(
            @PathVariable Long id,
            @Valid @RequestBody {self.entity_name}UpdateRequest request) {{
        log.info("REST request to update {self.entity_lower} with ID: {{}}", id);

        {self.entity_name}Response response = {self.entity_camel}Service.update{self.entity_name}(id, request);
        return HttpResponseHandler.success("{self.entity_name} updated successfully", response);
    }}

    /**
     * Soft deletes a {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{{id}}")
    public ResponseEntity<Object> delete{self.entity_name}(@PathVariable Long id) {{
        log.info("REST request to delete {self.entity_lower} with ID: {{}}", id);

        {self.entity_camel}Service.delete{self.entity_name}(id);
        return HttpResponseHandler.noContent("{self.entity_name} deleted successfully");
    }}

    /**
     * Restores a soft-deleted {self.entity_lower}.
     * 
     * @param id the {self.entity_lower} ID to restore
     * @return HTTP 200 with success message
     */
    @PostMapping("/{{id}}/restore")
    public ResponseEntity<Object> restore{self.entity_name}(@PathVariable Long id) {{
        log.info("REST request to restore {self.entity_lower} with ID: {{}}", id);

        {self.entity_camel}Service.restore{self.entity_name}(id);

        Map<String, String> response = new HashMap<>();
        response.put("{self.entity_lower}Id", id.toString());

        return HttpResponseHandler.success("{self.entity_name} restored successfully", response);
    }}

    /**
     * Helper method to create Pageable with sorting.
     * 
     * @param page page number
     * @param size page size
     * @param sort sort criteria
     * @return Pageable instance
     */
    private Pageable createPageable(int page, int size, String sort) {{
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }}
}}
"""
        return template
    
    def create_directories(self):
        """Create necessary directories"""
        directories = [
            f"{BASE_PATH}/model",
            f"{BASE_PATH}/repository/jpa",
            f"{BASE_PATH}/repository/jdbc",
            f"{BASE_PATH}/dto/{self.entity_lower}",
            f"{BASE_PATH}/service",
            f"{BASE_PATH}/service/impl",
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
            f"{BASE_PATH}/repository/jdbc/{self.entity_name}JdbcRepository.java": self.generate_jdbc_repository(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}CreateRequest.java": self.generate_create_request(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}UpdateRequest.java": self.generate_update_request(),
            f"{BASE_PATH}/dto/{self.entity_lower}/{self.entity_name}Response.java": self.generate_response(),
            f"{BASE_PATH}/service/{self.entity_name}Service.java": self.generate_service_interface(),
            f"{BASE_PATH}/service/impl/{self.entity_name}ServiceImpl.java": self.generate_service_impl(),
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
