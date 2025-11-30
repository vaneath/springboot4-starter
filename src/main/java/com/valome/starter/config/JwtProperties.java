package com.valome.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private Token access = new Token();
    private Token refresh = new Token();

    @Data
    public static class Token {
        private String secret;
        private long expiration;
    }
}