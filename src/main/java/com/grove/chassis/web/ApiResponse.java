package com.grove.chassis.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard success response envelope per API Contracts spec.
 * All API responses MUST use this envelope.
 *
 * Format: { "data": {...}, "meta": { "requestId": "...", "timestamp": "..." } }
 * Paginated: adds "pagination": { "page", "pageSize", "totalElements", "totalPages" }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        Meta meta,
        PaginationMeta pagination
) {
    public record Meta(String requestId, Instant timestamp) {
    }

    public static <T> ApiResponse<T> of(T data, String requestId) {
        return new ApiResponse<>(data, new Meta(requestId, Instant.now()), null);
    }

    public static <T> ApiResponse<T> of(T data, String requestId, PaginationMeta pagination) {
        return new ApiResponse<>(data, new Meta(requestId, Instant.now()), pagination);
    }
}
