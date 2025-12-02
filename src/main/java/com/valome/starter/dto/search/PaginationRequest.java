package com.valome.starter.dto.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    // Centralized default values
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;

    private String search; // global search text (optional)

    private Map<String, Object> filters;
    // { "status": "ACTIVE", "region": "KH", "createdAt": "2025-01-01" }

    private List<SortRequest> sorts;
    // [
    // { "field": "createdAt", "direction": "desc" },
    // { "field": "phoneNumber", "direction": "asc" }
    // ]

    private Integer page;
    private Integer size;

    /**
     * Creates a default PaginationRequest with all fields properly initialized.
     * 
     * @return A new PaginationRequest instance with default values
     */
    public static PaginationRequest createDefault() {
        PaginationRequest request = new PaginationRequest();
        request.setPage(DEFAULT_PAGE);
        request.setSize(DEFAULT_SIZE);
        request.setSearch(null);
        request.setFilters(new HashMap<>());
        request.setSorts(new ArrayList<>());
        return request;
    }

    /**
     * Ensures the request has all required fields initialized (not null).
     * If fields are null, they are initialized with default/empty values.
     * 
     * @return This instance for method chaining
     */
    public PaginationRequest ensureInitialized() {
        if (this.page == null) {
            this.page = DEFAULT_PAGE;
        }
        if (this.size == null) {
            this.size = DEFAULT_SIZE;
        }
        if (this.filters == null) {
            this.filters = new HashMap<>();
        }
        if (this.sorts == null) {
            this.sorts = new ArrayList<>();
        }
        // search can remain null as it's optional
        return this;
    }

    // Getters with default values
    public int getPage() {
        return page != null ? page : DEFAULT_PAGE;
    }

    public int getSize() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
