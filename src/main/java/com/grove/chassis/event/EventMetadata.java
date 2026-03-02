package com.grove.chassis.event;

import java.time.Instant;

/**
 * Metadata extracted from an incoming event for logging/audit purposes.
 */
public record EventMetadata(
        String eventId,
        Instant timestamp,
        String correlationId,
        String sourceDomain,
        String eventType
) {
    public static EventMetadata from(EventEnvelope<?> envelope) {
        return new EventMetadata(
                envelope.eventId(),
                envelope.timestamp(),
                envelope.correlationId(),
                envelope.sourceDomain(),
                envelope.eventType()
        );
    }
}
