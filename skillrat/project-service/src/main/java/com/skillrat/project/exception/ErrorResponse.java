package com.skillrat.project.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response format for API errors.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String errorCode;
    private final List<String> details;

    // Helper method to create a builder with common fields
    public static ErrorResponseBuilder builder(int status, String error) {
        return new ErrorResponseBuilder()
                .status(status)
                .error(error);
    }

    // Convert this error payload into a ResponseEntity with the given HTTP status
    public ResponseEntity<Object> toResponseEntity() {
        return new ResponseEntity<>(this, HttpStatus.valueOf(this.status));
    }
}
