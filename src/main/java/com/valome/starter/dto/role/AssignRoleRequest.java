package com.valome.starter.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for assigning a role to a user.
 */
@Data
public class AssignRoleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
