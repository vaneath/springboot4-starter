package com.valome.starter.jpa.userrole;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valome.starter.model.UserRole;
import com.valome.starter.model.UserRoleId;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);

    Optional<UserRole> findByIdUserIdAndIdRoleId(Long userId, Long roleId);

    void deleteByIdUserIdAndIdRoleId(Long userId, Long roleId);
}
