package com.grove.chassis.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope per API Contracts spec.
 *
 * Format: { "error": { "code": "...", "message": "...", "requestId": "...", "timestamp": "...", "details": [...] } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(ErrorBody error) {

    public record ErrorBody(
            String code,
            String message,
            String requestId,
            Instant timestamp,
            List<String> details
    ) {
    }

    public static ApiError of(String code, String message, String requestId) {
        return new ApiError(new ErrorBody(code, message, requestId, Instant.now(), null));
    }

    public static ApiError of(String code, String message, String requestId, List<String> details) {
        return new ApiError(new ErrorBody(code, message, requestId, Instant.now(), details));
    }
}
