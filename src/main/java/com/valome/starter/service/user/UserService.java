package com.valome.starter.service.user;

import com.valome.starter.model.User;

public interface UserService {
    User findByUsername(String username);
}
