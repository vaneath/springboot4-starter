package com.valome.starter.jpa.role;

import org.springframework.data.jpa.repository.Query;

import com.valome.starter.model.Role;
import com.valome.starter.repository.jpa.core.BaseRepository;

public interface RoleJpaRepository extends BaseRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.name = ?1 AND r.deletedAt IS NULL")
    Role findByName(String name);
}
