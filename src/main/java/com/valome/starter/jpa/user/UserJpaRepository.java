package com.valome.starter.jpa.user;

import org.springframework.data.jpa.repository.JpaRepository;
import com.valome.starter.model.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
