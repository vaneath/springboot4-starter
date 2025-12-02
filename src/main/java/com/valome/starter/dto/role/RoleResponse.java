package com.valome.starter.dto.role;

import com.valome.starter.dto.core.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for Role responses.
 * 
 * Contains all role information including audit fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleResponse extends BaseResponse {
    private String name;
}
