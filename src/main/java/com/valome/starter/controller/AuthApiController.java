package com.valome.starter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valome.starter.dto.auth.AuthRequest;
import com.valome.starter.dto.auth.LoginResponse;
import com.valome.starter.dto.auth.RegisterRequest;
import com.valome.starter.dto.auth.RegisterResponse;
import com.valome.starter.dto.core.SuccessResponse;
import com.valome.starter.service.auth.AuthService;
import com.valome.starter.util.ResponseHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@RequestBody AuthRequest request) {
        return ResponseHandler.success("Login successfully", authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        return ResponseHandler.success("User registered successfully", authService.register(request));
    }
}
