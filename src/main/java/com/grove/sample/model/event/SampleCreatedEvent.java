package com.grove.sample.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * SAMPLE: Rich event payload. Carries ALL data the domain owns about this entity.
 * Consumers of this event must NOT need to call back to the source domain.
 */
public record SampleCreatedEvent(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
}
