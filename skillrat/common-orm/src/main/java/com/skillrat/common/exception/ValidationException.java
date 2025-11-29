package com.skillrat.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when validation fails.
 * This results in an HTTP 422 Unprocessable Entity response when not handled.
 */
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ValidationException extends BusinessException {
    
    private final List<String> errors = new ArrayList<>();

    /**
     * Constructs a new ValidationException with the specified error message.
     *
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with the specified error message and errors list.
     *
     * @param message the error message
     * @param errors  the list of validation errors
     */
    public ValidationException(String message, List<String> errors) {
        super(message);
        if (errors != null) {
            this.errors.addAll(errors);
        }
    }

    /**
     * Returns the list of validation errors.
     *
     * @return the list of validation errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Adds a validation error to the list of errors.
     *
     * @param error the error message to add
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Checks if there are any validation errors.
     *
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
