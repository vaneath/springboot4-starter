package com.valome.starter.dto.product;

import com.valome.starter.dto.core.BaseResponse;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for Product responses.
 * 
 * Contains all product information including audit fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductResponse extends BaseResponse {
    private String name;
    private BigDecimal price;
    private String description;
}
