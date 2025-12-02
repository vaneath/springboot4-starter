package com.valome.starter.mapper;

import org.mapstruct.*;

import com.valome.starter.dto.role.RoleCreateRequest;
import com.valome.starter.dto.role.RoleResponse;
import com.valome.starter.dto.role.RoleUpdateRequest;
import com.valome.starter.model.Role;

import java.util.List;

/**
 * MapStruct mapper for Role entity conversions.
 * 
 * Handles mapping between Role entities and DTOs with proper null handling
 * and custom mapping logic.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    /**
     * Maps RoleCreateRequest to Role entity.
     * 
     * @param request the create request DTO
     * @return new Role entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "active", source = "active")
    Role toEntity(RoleCreateRequest request);

    /**
     * Maps Role entity to RoleResponse DTO.
     * 
     * @param role the Role entity
     * @return RoleResponse DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "longToString")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "longToString")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "longToString")
    @Mapping(target = "active", source = "active")
    RoleResponse toResponse(Role role);

    /**
     * Maps list of Role entities to list of RoleResponse DTOs.
     * 
     * @param roles list of Role entities
     * @return list of RoleResponse DTOs
     */
    List<RoleResponse> toResponseList(List<Role> roles);

    /**
     * Updates existing Role entity with data from RoleUpdateRequest.
     * 
     * @param role    the existing Role entity to update
     * @param request the update request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "active", source = "active")
    void updateEntity(@MappingTarget Role role, RoleUpdateRequest request);

    /**
     * Converts Long to String for ID fields.
     * Returns null if the Long value is null.
     */
    @Named("longToString")
    default String longToString(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
