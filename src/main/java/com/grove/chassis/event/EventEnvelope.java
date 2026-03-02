package com.grove.chassis.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard event envelope for all Kafka events.
 * All published events MUST use this envelope.
 * Payloads MUST carry ALL related domain data (rich payloads).
 *
 * Envelope fields: eventId, timestamp, correlationId, sourceDomain, eventType, payload
 */
public record EventEnvelope<T>(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("sourceDomain") String sourceDomain,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("payload") T payload
) {
    @JsonCreator
    public EventEnvelope {
    }

    /**
     * Factory method for creating a new event envelope.
     *
     * @param correlationId Trace correlation ID from the request context
     * @param sourceDomain  The domain publishing this event (e.g. "save", "pay")
     * @param eventType     Dot-notation event type (e.g. "save.account.withdrawal-completed")
     * @param payload       Rich payload with ALL data the domain owns about this event
     */
    public static <T> EventEnvelope<T> create(
            String correlationId,
            String sourceDomain,
            String eventType,
            T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                Instant.now(),
                correlationId,
                sourceDomain,
                eventType,
                payload
        );
    }
}
