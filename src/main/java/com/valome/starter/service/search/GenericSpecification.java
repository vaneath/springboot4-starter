package com.valome.starter.service.search;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

import com.valome.starter.dto.search.FieldConfig;
import com.valome.starter.dto.search.PaginationRequest;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class GenericSpecification<T> implements Specification<T> {

    private final PaginationRequest req;
    private final List<FieldConfig> whitelist;

    public GenericSpecification(PaginationRequest req, List<FieldConfig> whitelist) {
        this.req = req;
        this.whitelist = whitelist;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        // 1️⃣ Global search (only searchable whitelist fields)
        if (req.getSearch() != null && !req.getSearch().isEmpty()) {
            String keyword = "%" + req.getSearch().toLowerCase() + "%";

            List<Predicate> searchPredicates = whitelist.stream()
                    .filter(FieldConfig::isSearchable)
                    .filter(f -> f.getType() == String.class)
                    .map(f -> cb.like(cb.lower(root.get(f.getName())), keyword))
                    .toList();

            if (!searchPredicates.isEmpty()) {
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
        }

        // 2️⃣ Filters (only filterable whitelist fields)
        if (req.getFilters() != null && !req.getFilters().isEmpty()) {
            for (Map.Entry<String, Object> entry : req.getFilters().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                whitelist.stream()
                        .filter(f -> f.isFilterable() && f.getName().equals(key))
                        .findFirst()
                        .ifPresent(f -> {
                            if (f.getType() == String.class) {
                                predicates.add(cb.like(cb.lower(root.get(f.getName())),
                                        "%" + value.toString().toLowerCase() + "%"));
                            } else {
                                predicates.add(cb.equal(root.get(f.getName()), convertToType(f.getType(), value)));
                            }
                        });
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Object convertToType(Class<?> type, Object value) {
        if (value == null)
            return null;
        if (type == Integer.class || type == int.class)
            return Integer.valueOf(value.toString());
        if (type == Long.class || type == long.class)
            return Long.valueOf(value.toString());
        if (type == Boolean.class || type == boolean.class)
            return Boolean.valueOf(value.toString());
        if (type == Double.class || type == double.class)
            return Double.valueOf(value.toString());
        if (type == LocalDateTime.class)
            return LocalDateTime.parse(value.toString());
        return value;
    }
}
