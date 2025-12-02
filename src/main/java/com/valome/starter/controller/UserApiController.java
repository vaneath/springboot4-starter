package com.valome.starter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valome.starter.service.user.UserService;
import com.valome.starter.util.ResponseHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            return ResponseHandler.success("Profile retrieved successfully", userService.getProfile());
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("Invalid authentication")) {
                return ResponseHandler.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else if (e.getMessage().contains("not found")) {
                return ResponseHandler.error(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            return ResponseHandler.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            return ResponseHandler.error("Failed to fetch user profile", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
