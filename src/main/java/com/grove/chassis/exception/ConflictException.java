package com.grove.chassis.exception;

/**
 * Thrown on conflicts such as duplicates or version mismatches. Maps to HTTP 409.
 */
public class ConflictException extends GroveException {

    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
}
