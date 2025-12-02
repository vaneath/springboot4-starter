package com.valome.starter.service.user;

import org.springframework.data.domain.Page;

import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.model.User;

public interface UserService {
    User findByUsername(String username);

    User register(RegisterRequest request);

    User getProfile();

    Page<User> search(PaginationRequest request);
}
