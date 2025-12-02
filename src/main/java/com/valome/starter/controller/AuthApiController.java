package com.valome.starter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valome.starter.dto.auth.AuthRequest;
import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.service.auth.AuthService;
import com.valome.starter.util.ResponseHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseHandler.success("Login successful", authService.login(request));
        } catch (Exception e) {
            return ResponseHandler.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseHandler.success("User registered successfully", authService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error registering user", e);
            return ResponseHandler.error("Failed to register user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
