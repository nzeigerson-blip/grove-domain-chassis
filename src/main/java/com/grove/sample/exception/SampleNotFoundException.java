package com.grove.sample.exception;

import com.grove.chassis.exception.ResourceNotFoundException;

/**
 * SAMPLE: Domain-specific exception extending chassis hierarchy.
 */
public class SampleNotFoundException extends ResourceNotFoundException {

    public SampleNotFoundException(String identifier) {
        super("SampleItem", identifier);
    }
}
