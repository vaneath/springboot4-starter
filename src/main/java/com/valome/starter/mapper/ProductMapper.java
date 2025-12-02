package com.valome.starter.mapper;

import org.mapstruct.*;

import com.valome.starter.dto.product.ProductCreateRequest;
import com.valome.starter.dto.product.ProductResponse;
import com.valome.starter.dto.product.ProductUpdateRequest;
import com.valome.starter.model.Product;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper for Product entity conversions.
 * 
 * Handles mapping between Product entities and DTOs with proper null handling
 * and custom mapping logic.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, 
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    /**
     * Maps ProductCreateRequest to Product entity.
     * 
     * @param request the create request DTO
     * @return new Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Product toEntity(ProductCreateRequest request);

    /**
     * Maps Product entity to ProductResponse DTO.
     * 
     * @param product the Product entity
     * @return ProductResponse DTO
     */
    @Mapping(target = "deleted", source = "deletedAt", qualifiedByName = "mapDeleted")
    ProductResponse toResponse(Product product);

    /**
     * Maps list of Product entities to list of ProductResponse DTOs.
     * 
     * @param products list of Product entities
     * @return list of ProductResponse DTOs
     */
    List<ProductResponse> toResponseList(List<Product> products);

    /**
     * Updates existing Product entity with data from ProductUpdateRequest.
     * 
     * @param product the existing Product entity to update
     * @param request the update request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductUpdateRequest request);

    /**
     * Maps deletedAt timestamp to boolean deleted flag.
     */
    @Named("mapDeleted")
    default boolean mapDeleted(LocalDateTime deletedAt) {
        return deletedAt != null;
    }
}
