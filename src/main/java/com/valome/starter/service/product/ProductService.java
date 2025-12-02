package com.valome.starter.service.product;

import org.springframework.data.domain.Page;

import com.valome.starter.dto.product.ProductCreateRequest;
import com.valome.starter.dto.product.ProductResponse;
import com.valome.starter.dto.product.ProductUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;

/**
 * Service interface for Product management operations.
 * 
 * Defines the contract for product-related business operations including
 * CRUD operations and search functionality.
 */
public interface ProductService {

    /**
     * Searches products with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts, page, and size
     * @return page of product responses matching the criteria
     */
    Page<ProductResponse> search(PaginationRequest request);

    /**
     * Creates a new product.
     * 
     * @param request the product creation request
     * @return the created product response
     */
    ProductResponse create(ProductCreateRequest request);

    /**
     * Retrieves a product by ID.
     * 
     * @param id the product ID
     * @return the product response
     * @throws ResourceNotFoundException if product not found
     */
    ProductResponse getById(Long id);

    /**
     * Updates an existing product.
     * 
     * @param id      the product ID to update
     * @param request the update request
     * @return the updated product response
     * @throws ResourceNotFoundException if product not found
     */
    ProductResponse update(Long id, ProductUpdateRequest request);

    /**
     * Soft deletes a product.
     * 
     * @param id the product ID to delete
     * @throws ResourceNotFoundException if product not found
     */
    void delete(Long id);
}
