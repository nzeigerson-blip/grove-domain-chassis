package com.grove.sample.model.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * SAMPLE: Response DTO using Java record. ISO 8601 timestamps.
 */
public record SampleResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
