package com.valome.starter.dto.product;

import com.valome.starter.dto.core.BaseRequest;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for creating a new product.
 * 
 * Contains validation constraints to ensure data integrity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCreateRequest extends BaseRequest {

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String description;

}
