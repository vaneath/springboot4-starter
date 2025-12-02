package com.valome.starter.service.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import com.valome.starter.builder.SortBuilder;
import com.valome.starter.dto.search.FieldConfig;
import com.valome.starter.dto.search.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Generic implementation of pagination service with validation and error
 * handling.
 */
@Slf4j
@Service
public class PaginationServiceImpl implements PaginationService {
    private static final int MAX_PAGE_SIZE = 1000;

    @Override
    public <T> Page<T> search(PaginationRequest request, JpaSpecificationExecutor<T> repository,
            List<FieldConfig> fieldConfigs) {
        // Handle null request - create default initialized request
        if (request == null) {
            request = PaginationRequest.createDefault();
        } else {
            // Ensure all fields are initialized (not null)
            request.ensureInitialized();
        }

        // Validate request
        validateRequest(request, fieldConfigs);

        // Build specification for filtering and searching
        Specification<T> spec = new GenericSpecification<>(request, fieldConfigs);

        // Add soft-delete filter to exclude deleted records (deletedAt IS NULL)
        Specification<T> softDeleteSpec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        spec = spec.and(softDeleteSpec);

        // Build sort
        Sort sort = SortBuilder.build(request.getSorts(), fieldConfigs);

        // Build pageable with validated page and size
        int page = request.getPage();
        int size = request.getSize();

        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return repository.findAll(spec, pageable);
        } catch (Exception e) {
            log.error("Error executing pagination query", e);
            throw new IllegalArgumentException("Failed to execute search query: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the pagination request.
     * 
     * @param request      The request to validate
     * @param fieldConfigs The allowed field configurations
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRequest(PaginationRequest request, List<FieldConfig> fieldConfigs) {
        // Note: request should never be null at this point as it's handled above
        // but keeping check for safety
        if (request == null) {
            throw new IllegalArgumentException("Pagination request cannot be null");
        }

        // Validate page number
        int page = request.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }

        // Validate page size
        int size = request.getSize();
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Page size cannot exceed %d", MAX_PAGE_SIZE));
        }

        // Validate field configurations
        if (fieldConfigs == null || fieldConfigs.isEmpty()) {
            throw new IllegalArgumentException("Field configurations cannot be null or empty");
        }

        // Validate sort fields
        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            List<String> allowedFields = fieldConfigs.stream()
                    .map(FieldConfig::getName)
                    .toList();

            List<String> invalidSortFields = request.getSorts().stream()
                    .map(sort -> sort.getField())
                    .filter(field -> !allowedFields.contains(field))
                    .toList();

            if (!invalidSortFields.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("Invalid sort fields: %s. Allowed fields: %s",
                                invalidSortFields, allowedFields));
            }
        }

        // Validate filter fields
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            List<String> allowedFilterFields = fieldConfigs.stream()
                    .filter(FieldConfig::isFilterable)
                    .map(FieldConfig::getName)
                    .toList();

            List<String> invalidFilterFields = request.getFilters().keySet().stream()
                    .filter(field -> !allowedFilterFields.contains(field))
                    .toList();

            if (!invalidFilterFields.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("Invalid filter fields: %s. Allowed filterable fields: %s",
                                invalidFilterFields, allowedFilterFields));
            }
        }
    }
}
