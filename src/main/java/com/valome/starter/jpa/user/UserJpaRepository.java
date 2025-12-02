package com.valome.starter.jpa.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.valome.starter.model.User;
import com.valome.starter.repository.jpa.core.BaseRepository;

public interface UserJpaRepository extends BaseRepository<User, Long> {
    @EntityGraph(attributePaths = { "userRoles", "userRoles.role" })
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.deletedAt IS NULL")
    User findByUsername(String username);
}
