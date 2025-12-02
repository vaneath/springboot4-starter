package com.valome.starter.repository.jpa;

import org.springframework.stereotype.Repository;

import com.valome.starter.model.Product;
import com.valome.starter.repository.jpa.core.BaseRepository;

/**
 * Repository interface for Product entity operations.
 * 
 * Provides standard CRUD operations and specification support for advanced
 * querying.
 */
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {
}
