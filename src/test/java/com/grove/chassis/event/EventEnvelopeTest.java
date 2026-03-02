package com.grove.chassis.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventEnvelopeTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    record TestPayload(String value, int count) {}

    @Test
    @DisplayName("should_CreateEnvelopeWithAllFields_When_UsingFactory")
    void should_CreateEnvelopeWithAllFields_When_UsingFactory() {
        EventEnvelope<TestPayload> envelope = EventEnvelope.create(
                "corr-123", "sample", "sample.item.created", new TestPayload("test", 42));

        assertThat(envelope.eventId()).isNotNull().isNotBlank();
        assertThat(envelope.timestamp()).isNotNull();
        assertThat(envelope.correlationId()).isEqualTo("corr-123");
        assertThat(envelope.sourceDomain()).isEqualTo("sample");
        assertThat(envelope.eventType()).isEqualTo("sample.item.created");
        assertThat(envelope.payload().value()).isEqualTo("test");
        assertThat(envelope.payload().count()).isEqualTo(42);
    }

    @Test
    @DisplayName("should_GenerateUniqueEventIds_When_MultipleEnvelopesCreated")
    void should_GenerateUniqueEventIds_When_MultipleEnvelopesCreated() {
        EventEnvelope<String> first = EventEnvelope.create("c1", "d1", "t1", "p1");
        EventEnvelope<String> second = EventEnvelope.create("c1", "d1", "t1", "p1");

        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }

    @Test
    @DisplayName("should_SerializeToJson_When_UsingJackson")
    void should_SerializeToJson_When_UsingJackson() throws Exception {
        EventEnvelope<TestPayload> envelope = EventEnvelope.create(
                "corr-456", "pay", "pay.transaction.completed", new TestPayload("transfer", 100));

        String json = objectMapper.writeValueAsString(envelope);

        assertThat(json).contains("\"eventId\"");
        assertThat(json).contains("\"correlationId\":\"corr-456\"");
        assertThat(json).contains("\"sourceDomain\":\"pay\"");
        assertThat(json).contains("\"eventType\":\"pay.transaction.completed\"");
        assertThat(json).contains("\"value\":\"transfer\"");
    }

    @Test
    @DisplayName("should_DeserializeFromJson_When_UsingJackson")
    void should_DeserializeFromJson_When_UsingJackson() throws Exception {
        EventEnvelope<TestPayload> original = EventEnvelope.create(
                "corr-789", "save", "save.account.created", new TestPayload("savings", 0));

        String json = objectMapper.writeValueAsString(original);

        EventEnvelope<?> deserialized = objectMapper.readValue(json, EventEnvelope.class);

        assertThat(deserialized.eventId()).isEqualTo(original.eventId());
        assertThat(deserialized.correlationId()).isEqualTo("corr-789");
        assertThat(deserialized.sourceDomain()).isEqualTo("save");
    }
}
