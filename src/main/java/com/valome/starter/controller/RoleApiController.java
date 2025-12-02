package com.valome.starter.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valome.starter.dto.core.SuccessResponse;
import com.valome.starter.dto.role.AssignRoleRequest;
import com.valome.starter.dto.role.RoleCreateRequest;
import com.valome.starter.dto.role.RoleResponse;
import com.valome.starter.dto.role.RoleUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.service.role.RoleService;
import com.valome.starter.util.ResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for Role management operations.
 * 
 * Provides RESTful endpoints for CRUD operations on roles and role assignment.
 */
@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RoleApiController {
    private final RoleService roleService;

    /**
     * Searches roles with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts,
     *                page, and size
     * @return page of role responses with HTTP 200
     */
    @PostMapping("/search")
    public ResponseEntity<SuccessResponse<Page<RoleResponse>>> search(@RequestBody PaginationRequest request) {
        log.info("REST request to search roles - request: {}", request);
        Page<RoleResponse> response = roleService.search(request);
        return ResponseHandler.success("Roles retrieved successfully", response);
    }

    /**
     * Creates a new role.
     * 
     * @param request the role creation request
     * @return created role response with HTTP 200
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<RoleResponse>> create(@Valid @RequestBody RoleCreateRequest request) {
        log.info("REST request to create role");

        RoleResponse response = roleService.create(request);
        return ResponseHandler.success("Role created successfully", response);
    }

    /**
     * Retrieves a role by ID.
     * 
     * @param id the role ID
     * @return role response with HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<RoleResponse>> getById(@PathVariable Long id) {
        log.debug("REST request to get role by ID: {}", id);

        RoleResponse response = roleService.getById(id);
        return ResponseHandler.success("Role retrieved successfully", response);
    }

    /**
     * Updates an existing role.
     * 
     * @param id      the role ID to update
     * @param request the update request
     * @return updated role response with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        log.info("REST request to update role with ID: {}", id);

        RoleResponse response = roleService.update(id, request);
        return ResponseHandler.success("Role updated successfully", response);
    }

    /**
     * Soft deletes a role.
     * 
     * @param id the role ID to delete
     * @return HTTP 200 with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Object>> delete(@PathVariable Long id) {
        log.info("REST request to delete role with ID: {}", id);

        roleService.delete(id);
        return ResponseHandler.success("Role deleted successfully");
    }

    /**
     * Assigns a role to a user.
     * 
     * @param request the assignment request containing userId and roleId
     * @return assigned role response with HTTP 200
     */
    @PostMapping("/assign")
    public ResponseEntity<SuccessResponse<RoleResponse>> assignRoleToUser(
            @Valid @RequestBody AssignRoleRequest request) {
        log.info("REST request to assign role {} to user {}", request.getRoleId(), request.getUserId());

        RoleResponse response = roleService.assignRoleToUser(request);
        return ResponseHandler.success("Role assigned successfully", response);
    }

    /**
     * Removes a role from a user.
     * 
     * @param userId the user ID
     * @param roleId the role ID
     * @return HTTP 200 with success message
     */
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<SuccessResponse<Object>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        log.info("REST request to remove role {} from user {}", roleId, userId);

        roleService.removeRoleFromUser(userId, roleId);
        return ResponseHandler.success("Role removed successfully");
    }

    /**
     * Gets all roles assigned to a user.
     * 
     * @param userId the user ID
     * @return list of role responses with HTTP 200
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<SuccessResponse<List<RoleResponse>>> getUserRoles(@PathVariable Long userId) {
        log.debug("REST request to get roles for user ID: {}", userId);

        List<RoleResponse> response = roleService.getUserRoles(userId);
        return ResponseHandler.success("User roles retrieved successfully", response);
    }
}
