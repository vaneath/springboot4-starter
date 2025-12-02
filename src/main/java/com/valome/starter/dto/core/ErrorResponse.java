package com.valome.starter.dto.core;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private List<String> errors;
    private int statusCode;
    private String status;

    @Default
    private boolean success = false;
}
