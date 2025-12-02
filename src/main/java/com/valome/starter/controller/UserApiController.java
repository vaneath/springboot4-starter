package com.valome.starter.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valome.starter.dto.core.SuccessResponse;
import com.valome.starter.dto.search.PaginationRequest;
import com.valome.starter.model.User;
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
    public ResponseEntity<SuccessResponse<User>> getProfile() {
        return ResponseHandler.success("Profile retrieved successfully", userService.getProfile());
    }

    @PostMapping("/list")
    public ResponseEntity<SuccessResponse<Page<User>>> search(
            @RequestBody(required = false) PaginationRequest request) {

        if (request == null) {
            request = new PaginationRequest();
        }

        return ResponseHandler.success("Users searched successfully", userService.search(request));
    }
}
