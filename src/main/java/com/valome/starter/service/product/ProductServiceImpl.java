package com.valome.starter.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valome.starter.dto.product.ProductCreateRequest;
import com.valome.starter.dto.product.ProductResponse;
import com.valome.starter.dto.product.ProductUpdateRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.mapper.ProductMapper;
import com.valome.starter.model.Product;
import com.valome.starter.repository.jpa.ProductRepository;
import com.valome.starter.service.search.PaginationService;
import com.valome.starter.exception.ResourceNotFoundException;

/**
 * Implementation of ProductService interface.
 * 
 * Provides business logic for product management operations including
 * validation and data transformation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final PaginationService paginationService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(PaginationRequest request) {
        log.debug("Searching products with request: {}", request);
        
        Page<Product> productPage = paginationService.search(
                request, 
                productRepository, 
                Product.PAGINATION_FIELDS);
        
        return productPage.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse create(ProductCreateRequest request) {
        log.info("Creating new product");

        Product product = productMapper.toEntity(request);
        product = productRepository.save(product);

        log.info("Created product with ID: {}", product.getId());
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        log.debug("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productMapper.updateEntity(product, request);
        product = productRepository.save(product);

        log.info("Updated product with ID: {}", id);
        return productMapper.toResponse(product);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Deleted product with ID: {}", id);
    }
}
