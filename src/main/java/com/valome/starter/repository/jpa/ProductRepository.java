package com.valome.starter.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.valome.starter.model.Product;

/**
 * Repository interface for Product entity operations.
 * 
 * Provides standard CRUD operations and specification support for advanced querying.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
