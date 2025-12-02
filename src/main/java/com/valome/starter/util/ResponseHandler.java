package com.valome.starter.util;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.valome.starter.dto.core.ErrorResponse;
import com.valome.starter.dto.core.SuccessResponse;

public class ResponseHandler {

    // SUCCESS (with data)
    public static <T> ResponseEntity<SuccessResponse<T>> success(String message, T data) {
        SuccessResponse<T> response = SuccessResponse.<T>builder()
                .message(message)
                .data(data)
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK.getReasonPhrase())
                .build();

        return ResponseEntity.ok(response);
    }

    // SUCCESS (no data)
    public static ResponseEntity<SuccessResponse<Object>> success(String message) {
        SuccessResponse<Object> response = SuccessResponse.builder()
                .message(message)
                .statusCode(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build();

        return ResponseEntity.ok(response);
    }

    // ERROR (single message)
    public static ResponseEntity<ErrorResponse> error(String errorMessage, HttpStatus status) {
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorMessage))
                .statusCode(status.value())
                .status(status.getReasonPhrase())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    // ERROR (multiple errors)
    public static ResponseEntity<ErrorResponse> error(List<String> errors, HttpStatus status) {
        ErrorResponse response = ErrorResponse.builder()
                .errors(errors)
                .statusCode(status.value())
                .status(status.getReasonPhrase())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
