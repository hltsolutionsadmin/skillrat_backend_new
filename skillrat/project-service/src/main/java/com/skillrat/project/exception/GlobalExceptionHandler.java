package com.skillrat.project.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler that processes exceptions and returns standardized error responses.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Object> handleProjectNotFoundException(
            ProjectNotFoundException ex, WebRequest request) {
        
        log.warn("Project not found: {}", ex.getMessage());
        
        return ErrorResponse.builder(HttpStatus.NOT_FOUND.value(), "Not Found")
                .message(ex.getMessage())
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }

    @ExceptionHandler(DuplicateProjectCodeException.class)
    public ResponseEntity<Object> handleDuplicateProjectCodeException(
            DuplicateProjectCodeException ex, WebRequest request) {
        
        log.warn("Duplicate project code: {}", ex.getMessage());
        
        return ErrorResponse.builder(HttpStatus.CONFLICT.value(), "Conflict")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("Constraint violation: {}", ex.getMessage());
        
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        return ErrorResponse.builder(HttpStatus.BAD_REQUEST.value(), "Bad Request")
                .message("Validation failed")
                .details(errors)
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }


    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        
        log.warn("Method argument not valid: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        return ErrorResponse.builder(HttpStatus.BAD_REQUEST.value(), "Bad Request")
                .message("Validation failed")
                .details(errors)
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        log.error("Business exception: {}", ex.getMessage(), ex);
        
        return ErrorResponse.builder(HttpStatus.BAD_REQUEST.value(), "Bad Request")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        return ErrorResponse.builder(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Internal Server Error")
                .message("An unexpected error occurred")
                .path(getRequestPath(request))
                .build()
                .toResponseEntity();
    }
    
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return "";
    }
}
