package com.grove.chassis.exception;

/**
 * Thrown when a business rule is violated. Maps to HTTP 422.
 * Domain code should subclass or use directly with a domain-specific error code.
 */
public class BusinessRuleException extends GroveException {

    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message);
    }
}
