package com.valome.starter.service.auth;

import com.valome.starter.dto.auth.LoginResponse;
import com.valome.starter.dto.auth.AuthRequest;
import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.dto.auth.RegisterResponse;

public interface AuthService {
    LoginResponse login(AuthRequest request);

    RegisterResponse register(RegisterRequest request);
}
