package com.valome.starter.service.role;

import java.util.List;

import org.springframework.data.domain.Page;

import com.valome.starter.dto.role.UserRoleRequest;
import com.valome.starter.dto.role.RoleCreateRequest;
import com.valome.starter.dto.role.RoleResponse;
import com.valome.starter.dto.role.RoleUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.exception.ResourceNotFoundException;

/**
 * Service interface for Role management operations.
 * 
 * Defines the contract for role-related business operations including
 * CRUD operations, role assignment, and search functionality.
 */
public interface RoleService {

    /**
     * Searches roles with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts,
     *                page, and size
     * @return page of role responses matching the criteria
     */
    Page<RoleResponse> search(PaginationRequest request);

    /**
     * Creates a new role.
     * 
     * @param request the role creation request
     * @return the created role response
     */
    RoleResponse create(RoleCreateRequest request);

    /**
     * Retrieves a role by ID.
     * 
     * @param id the role ID
     * @return the role response
     * @throws ResourceNotFoundException if role not found
     */
    RoleResponse getById(Long id);

    /**
     * Updates an existing role.
     * 
     * @param id      the role ID to update
     * @param request the update request
     * @return the updated role response
     * @throws ResourceNotFoundException if role not found
     */
    RoleResponse update(Long id, RoleUpdateRequest request);

    /**
     * Soft deletes a role.
     * 
     * @param id the role ID to delete
     * @throws ResourceNotFoundException if role not found
     */
    void delete(Long id);

    /**
     * Assigns a role to a user.
     * 
     * @param request the assignment request containing userId and roleId
     * @return the assigned role response
     * @throws ResourceNotFoundException if user or role not found
     * @throws IllegalArgumentException  if role is already assigned to user
     */
    RoleResponse assignRoleToUser(UserRoleRequest request);

    /**
     * Removes a role from a user.
     * 
     * @param userId the user ID
     * @param roleId the role ID
     * @throws ResourceNotFoundException if user, role, or assignment not found
     */
    void removeRoleFromUser(UserRoleRequest request);

    /**
     * Gets all roles assigned to a user.
     * 
     * @param userId the user ID
     * @return list of role responses
     * @throws ResourceNotFoundException if user not found
     */
    List<RoleResponse> getUserRoles(Long userId);
}
