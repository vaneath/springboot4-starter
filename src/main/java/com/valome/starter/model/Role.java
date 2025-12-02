package com.valome.starter.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.valome.starter.dto.search.FieldConfig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "roles_uk_name")
})
public class Role extends BaseModel implements GrantedAuthority {
    @Column(name = "name", nullable = false)
    private String name;

    @Override
    public String getAuthority() {
        return this.name;
    }

    public static final List<FieldConfig> PAGINATION_FIELDS = List.of(
            new FieldConfig("name", String.class, true, true),
            new FieldConfig("createdAt", LocalDateTime.class, false, true));
}
