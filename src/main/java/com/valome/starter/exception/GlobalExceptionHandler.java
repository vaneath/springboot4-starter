package com.valome.starter.exception;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.valome.starter.dto.core.ErrorResponse;
import com.valome.starter.util.ResponseHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseHandler.error(errors, HttpStatus.BAD_REQUEST);
    }

    // Handle IllegalArgumentException (often used for business rule errors)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseHandler.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handle Not Found exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseHandler.error(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle No Resource Found exceptions (404 for routes/resources)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseHandler.error("Resource not found: " + ex.getResourcePath(), HttpStatus.NOT_FOUND);
    }

    // Handle Property Reference exceptions (invalid field/property references)
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException ex) {
        return ResponseHandler.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handle HTTP Message Not Readable exceptions (empty/malformed request body)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("No content to map due to end-of-input")) {
            return ResponseHandler.error(
                    "Request body is empty. Please send an empty JSON object {} or provide a valid request body.",
                    HttpStatus.BAD_REQUEST);
        }
        if (message != null && message.contains("Required request body is missing")) {
            return ResponseHandler.error(
                    "Request body is required but was not provided.",
                    HttpStatus.BAD_REQUEST);
        }
        return ResponseHandler.error("Invalid request body format: " + (message != null ? message : "Malformed JSON"),
                HttpStatus.BAD_REQUEST);
    }

    // Handle all uncaught exceptions (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ex.printStackTrace(); // optional but useful for debugging
        return ResponseHandler.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
