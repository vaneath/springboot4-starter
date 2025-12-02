package com.valome.starter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.valome.starter.dto.core.SuccessResponse;
import com.valome.starter.dto.product.ProductCreateRequest;
import com.valome.starter.dto.product.ProductResponse;
import com.valome.starter.dto.product.ProductUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.service.product.ProductService;
import com.valome.starter.util.ResponseHandler;

/**
 * REST controller for Product management operations.
 * 
 * Provides RESTful endpoints for CRUD operations on products.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductApiController {
    private final ProductService productService;

    /**
     * Searches products with pagination, filtering, and sorting.
     * 
     * @param request the pagination request containing search, filters, sorts,
     *                page, and size
     * @return page of product responses with HTTP 200
     */
    @PostMapping("/search")
    public ResponseEntity<SuccessResponse<Page<ProductResponse>>> search(@RequestBody PaginationRequest request) {
        log.info("REST request to search products - request: {}", request);
        Page<ProductResponse> response = productService.search(request);
        return ResponseHandler.success("Products retrieved successfully", response);
    }

    /**
     * Creates a new product.
     * 
     * @param request the product creation request
     * @return created product response with HTTP 200
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<ProductResponse>> create(@Valid @RequestBody ProductCreateRequest request) {
        log.info("REST request to create product");

        ProductResponse response = productService.create(request);
        return ResponseHandler.success("Product created successfully", response);
    }

    /**
     * Retrieves a product by ID.
     * 
     * @param id the product ID
     * @return product response with HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> getById(@PathVariable Long id) {
        log.debug("REST request to get product by ID: {}", id);

        ProductResponse response = productService.getById(id);
        return ResponseHandler.success("Product retrieved successfully", response);
    }

    /**
     * Updates an existing product.
     * 
     * @param id      the product ID to update
     * @param request the update request
     * @return updated product response with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("REST request to update product with ID: {}", id);

        ProductResponse response = productService.update(id, request);
        return ResponseHandler.success("Product updated successfully", response);
    }

    /**
     * Soft deletes a product.
     * 
     * @param id the product ID to delete
     * @return HTTP 200 with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Object>> delete(@PathVariable Long id) {
        log.info("REST request to delete product with ID: {}", id);

        productService.delete(id);
        return ResponseHandler.success("Product deleted successfully");
    }
}
