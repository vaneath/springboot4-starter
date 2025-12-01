package com.valome.starter.service.user;

import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.model.User;

public interface UserService {
    User findByUsername(String username);

    User register(RegisterRequest request);

    User getProfile();
}
