package com.valome.starter.model;

import org.springframework.security.core.GrantedAuthority;

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
}
