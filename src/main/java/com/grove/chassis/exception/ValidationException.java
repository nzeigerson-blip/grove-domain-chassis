package com.grove.chassis.exception;

import java.util.List;

/**
 * Thrown when input validation fails. Maps to HTTP 400 with field-level details.
 */
public class ValidationException extends GroveException {

    private final List<String> details;

    public ValidationException(String message, List<String> details) {
        super("VALIDATION_ERROR", message);
        this.details = List.copyOf(details);
    }

    public List<String> getDetails() {
        return details;
    }
}
