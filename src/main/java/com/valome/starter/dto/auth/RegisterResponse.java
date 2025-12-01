package com.valome.starter.dto.auth;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RegisterResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dob;
}
