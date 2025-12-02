package com.valome.starter.dto.role;

import com.valome.starter.dto.core.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for creating a new role.
 * 
 * Contains validation constraints to ensure data integrity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleCreateRequest extends BaseRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 255, message = "Role name must not exceed 255 characters")
    private String name;
}
