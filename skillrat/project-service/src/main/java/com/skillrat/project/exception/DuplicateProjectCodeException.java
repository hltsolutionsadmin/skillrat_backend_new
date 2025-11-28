package com.skillrat.project.exception;

/**
 * Exception thrown when attempting to create or update a project with a code that already exists.
 */
public class DuplicateProjectCodeException extends BusinessException {
    
    private static final String ERROR_CODE = "DUPLICATE_PROJECT_CODE";
    
    public DuplicateProjectCodeException(String code) {
        super(String.format("Project with code '%s' already exists", code), ERROR_CODE);
    }
    
    public DuplicateProjectCodeException(String code, Throwable cause) {
        super(String.format("Project with code '%s' already exists", code), ERROR_CODE, cause);
    }
}
