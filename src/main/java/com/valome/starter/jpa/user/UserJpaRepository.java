package com.valome.starter.jpa.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.valome.starter.model.User;

public interface UserJpaRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = { "userRoles", "userRoles.role" })
    User findByUsername(String username);
}
