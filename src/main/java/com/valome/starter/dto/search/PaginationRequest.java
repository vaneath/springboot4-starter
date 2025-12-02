package com.valome.starter.dto.search;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    private String search; // global search text (optional)

    private Map<String, Object> filters;
    // { "status": "ACTIVE", "region": "KH" }

    private List<SortRequest> sorts;
    // [
    // { "field": "createdAt", "direction": "desc" },
    // { "field": "phoneNumber", "direction": "asc" }
    // ]

    private Integer page;
    private Integer size;

    // Getters with default values
    public int getPage() {
        return page != null ? page : 0;
    }

    public int getSize() {
        return size != null ? size : 10;
    }
}
