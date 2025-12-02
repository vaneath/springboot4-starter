package com.valome.starter.builder;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.valome.starter.dto.search.FieldConfig;
import com.valome.starter.dto.search.SortRequest;

public class SortBuilder {
    public static Sort build(List<SortRequest> sorts, List<FieldConfig> whitelist) {
        if (sorts == null || sorts.isEmpty())
            return Sort.unsorted();

        List<Sort.Order> orders = sorts.stream()
                .filter(s -> whitelist.stream().anyMatch(f -> f.getName().equals(s.getField())))
                .map(s -> {
                    Sort.Direction dir = "desc".equalsIgnoreCase(s.getDirection()) ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    return new Sort.Order(dir, s.getField());
                })
                .toList();

        return Sort.by(orders);
    }
}
