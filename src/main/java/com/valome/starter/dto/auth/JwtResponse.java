package com.valome.starter.dto.auth;

import lombok.Data;

@Data
public class JwtResponse {
    private final String accessToken;
    private final String refreshToken;
}
