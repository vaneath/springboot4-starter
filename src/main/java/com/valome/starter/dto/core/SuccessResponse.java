package com.valome.starter.dto.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse<T> {
    private T data;
    private String message;
    private int statusCode;
    private String status;

    @Default
    private boolean success = true;
}
