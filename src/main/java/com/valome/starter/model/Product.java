package com.valome.starter.model;

import com.valome.starter.dto.search.FieldConfig;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Product entity.
 * 
 * Represents product data with full audit trail support.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product extends BaseModel {

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    /**
     * Field configurations for pagination, filtering, and searching.
     * All fields are searchable and filterable by default.
     */
    public static final List<FieldConfig> PAGINATION_FIELDS = List.of(
            new FieldConfig("name", String.class, true, true),
            new FieldConfig("price", BigDecimal.class, true, true),
            new FieldConfig("description", String.class, true, true),
            new FieldConfig("createdAt", LocalDateTime.class, false, true));
}
