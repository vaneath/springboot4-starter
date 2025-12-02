package com.valome.starter.service.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.valome.starter.dto.search.FieldConfig;
import com.valome.starter.dto.search.PaginationRequest;

/**
 * Generic service for handling pagination, sorting, and filtering across
 * entities.
 */
public interface PaginationService {

    /**
     * Performs paginated search with filtering, sorting, and global search.
     * Automatically handles null requests by creating a default initialized
     * request.
     * 
     * @param <T>          The entity type
     * @param request      The pagination request containing filters, sorts, search,
     *                     page, and size. Can be null - will be replaced with
     *                     default values.
     * @param repository   The JPA repository that supports specification execution
     * @param fieldConfigs The whitelist of allowed fields for filtering, sorting,
     *                     and searching
     * @return A paginated result
     * @throws IllegalArgumentException if the request is invalid
     */
    <T> Page<T> search(PaginationRequest request, JpaSpecificationExecutor<T> repository,
            List<FieldConfig> fieldConfigs);
}
