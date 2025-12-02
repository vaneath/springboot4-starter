package com.valome.starter.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldConfig {
    private String name; // entity field name
    private Class<?> type; // String.class, Integer.class, Boolean.class, etc.
    private boolean searchable; // can be used in global search
    private boolean filterable; // can be used as filter
}
