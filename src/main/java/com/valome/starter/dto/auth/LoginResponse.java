package com.valome.starter.dto.auth;

import com.valome.starter.model.User;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private User user;
}
