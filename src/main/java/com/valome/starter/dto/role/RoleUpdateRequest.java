package com.valome.starter.dto.role;

import com.valome.starter.dto.core.BaseRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for updating an existing role.
 * 
 * All fields are optional to support partial updates.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateRequest extends BaseRequest {

    @Size(max = 255, message = "Role name must not exceed 255 characters")
    private String name;
}
