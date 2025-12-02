package com.valome.starter.service.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valome.starter.dto.role.AssignRoleRequest;
import com.valome.starter.dto.role.RoleCreateRequest;
import com.valome.starter.dto.role.RoleResponse;
import com.valome.starter.dto.role.RoleUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.exception.ResourceNotFoundException;
import com.valome.starter.jpa.role.RoleJpaRepository;
import com.valome.starter.jpa.user.UserJpaRepository;
import com.valome.starter.jpa.userrole.UserRoleJpaRepository;
import com.valome.starter.mapper.RoleMapper;
import com.valome.starter.model.Role;
import com.valome.starter.model.User;
import com.valome.starter.model.UserRole;
import com.valome.starter.model.UserRoleId;
import com.valome.starter.service.search.PaginationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService interface.
 * 
 * Provides business logic for role management operations including
 * validation and data transformation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserRoleJpaRepository userRoleJpaRepository;
    private final RoleMapper roleMapper;
    private final PaginationService paginationService;

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> search(PaginationRequest request) {
        log.debug("Searching roles with request: {}", request);

        Page<Role> rolePage = paginationService.search(
                request,
                roleJpaRepository,
                Role.PAGINATION_FIELDS);

        return rolePage.map(roleMapper::toResponse);
    }

    @Override
    public RoleResponse create(RoleCreateRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        // Check if role name already exists
        if (roleJpaRepository.findByName(request.getName()) != null) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }

        Role role = roleMapper.toEntity(request);
        role = roleJpaRepository.save(role);

        log.info("Created role with ID: {}", role.getId());
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        log.debug("Fetching role by ID: {}", id);

        Role role = roleJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        return roleMapper.toResponse(role);
    }

    @Override
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        log.info("Updating role with ID: {}", id);

        Role role = roleJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        // Check if new name already exists (if name is being updated)
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            Role existingRole = roleJpaRepository.findByName(request.getName());
            if (existingRole != null && !existingRole.getId().equals(id)) {
                throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
            }
        }

        roleMapper.updateEntity(role, request);
        role = roleJpaRepository.save(role);

        log.info("Updated role with ID: {}", id);
        return roleMapper.toResponse(role);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting role with ID: {}", id);

        Role role = roleJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        role.setDeletedAt(LocalDateTime.now());
        roleJpaRepository.save(role);

        log.info("Deleted role with ID: {}", id);
    }

    @Override
    public RoleResponse assignRoleToUser(AssignRoleRequest request) {
        log.info("Assigning role {} to user {}", request.getRoleId(), request.getUserId());

        // Validate user exists
        User user = userJpaRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Validate role exists
        Role role = roleJpaRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        // Check if role is already assigned to user
        if (userRoleJpaRepository.findByIdUserIdAndIdRoleId(request.getUserId(), request.getRoleId()).isPresent()) {
            throw new IllegalArgumentException("Role is already assigned to user");
        }

        // Create and save UserRole
        UserRoleId userRoleId = new UserRoleId(request.getUserId(), request.getRoleId());
        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleJpaRepository.save(userRole);

        log.info("Assigned role {} to user {}", request.getRoleId(), request.getUserId());
        return roleMapper.toResponse(role);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        // Validate user exists
        userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate role exists
        roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Check if assignment exists and remove it
        UserRole userRole = userRoleJpaRepository.findByIdUserIdAndIdRoleId(userId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role is not assigned to user"));

        userRoleJpaRepository.delete(userRole);

        log.info("Removed role {} from user {}", roleId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        log.debug("Fetching roles for user ID: {}", userId);

        // Validate user exists
        userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<UserRole> userRoles = userRoleJpaRepository.findByIdUserId(userId);

        return userRoles.stream()
                .map(UserRole::getRole)
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
}
