package com.grove.chassis.exception;

/**
 * Base exception for all Grove platform exceptions.
 * All domain-specific exceptions MUST extend this hierarchy.
 * The chassis GlobalExceptionHandler maps these to standard error responses.
 */
public abstract class GroveException extends RuntimeException {

    private final String errorCode;

    protected GroveException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected GroveException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
