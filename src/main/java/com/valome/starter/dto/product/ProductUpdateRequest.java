package com.valome.starter.dto.product;

import com.valome.starter.dto.core.BaseRequest;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for updating an existing product.
 * 
 * All fields are optional to support partial updates.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductUpdateRequest extends BaseRequest {

    @Size(max = 255)
    private String name;

    private BigDecimal price;

    @Size(max = 255)
    private String description;

}
