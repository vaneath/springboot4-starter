package com.valome.starter.service.auth;

import com.valome.starter.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.security.Key;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    // ------------------ ACCESS TOKEN ------------------
    public String generateAccessToken(String username) {
        return generateToken(username, jwtProperties.getAccess().getSecret(),
                jwtProperties.getAccess().getExpiration());
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        return validateToken(token, jwtProperties.getAccess().getSecret(), userDetails);
    }

    public String extractUsernameFromAccessToken(String token) {
        return extractUsername(token, jwtProperties.getAccess().getSecret());
    }

    // ------------------ REFRESH TOKEN ------------------
    public String generateRefreshToken(String username) {
        return generateToken(username, jwtProperties.getRefresh().getSecret(),
                jwtProperties.getRefresh().getExpiration());
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        return validateToken(token, jwtProperties.getRefresh().getSecret(), userDetails);
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractUsername(token, jwtProperties.getRefresh().getSecret());
    }

    // ------------------ PRIVATE METHODS ------------------
    private String generateToken(String username, String secret, long expirationMillis) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean validateToken(String token, String secret, UserDetails userDetails) {
        final String username = extractUsername(token, secret);
        final boolean isExpired = isTokenExpired(token, secret);
        final boolean isEqualUsername = username.equals(userDetails.getUsername());

        return isEqualUsername && !isExpired;
    }

    private String extractUsername(String token, String secret) {
        log.debug("Secret: " + secret); // Should be a long base64 string
        log.debug("Token: " + token); // Should have 2 periods
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean isTokenExpired(String token, String secret) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }
}
