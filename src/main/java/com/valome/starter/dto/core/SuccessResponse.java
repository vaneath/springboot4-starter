package com.valome.starter.dto.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse<T> {
    private T data;
    private String message;
    private int status;
}
