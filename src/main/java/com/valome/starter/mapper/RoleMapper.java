package com.valome.starter.mapper;

import org.mapstruct.*;

import com.valome.starter.dto.role.RoleCreateRequest;
import com.valome.starter.dto.role.RoleResponse;
import com.valome.starter.dto.role.RoleUpdateRequest;
import com.valome.starter.model.Role;

import java.time.LocalDateTime;
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
    Role toEntity(RoleCreateRequest request);

    /**
     * Maps Role entity to RoleResponse DTO.
     * 
     * @param role the Role entity
     * @return RoleResponse DTO
     */
    @Mapping(target = "deleted", source = "deletedAt", qualifiedByName = "mapDeleted")
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
    void updateEntity(@MappingTarget Role role, RoleUpdateRequest request);

    /**
     * Maps deletedAt timestamp to boolean deleted flag.
     */
    @Named("mapDeleted")
    default boolean mapDeleted(LocalDateTime deletedAt) {
        return deletedAt != null;
    }
}
