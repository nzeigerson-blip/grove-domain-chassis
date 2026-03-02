package com.grove.chassis.exception;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends GroveException {

    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resource, identifier));
    }
}
