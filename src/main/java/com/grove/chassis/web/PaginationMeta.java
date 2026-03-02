package com.grove.chassis.web;

/**
 * Pagination metadata for list responses.
 */
public record PaginationMeta(
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
