package com.grove.chassis.event;

import com.grove.chassis.logging.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new EventPublisher(kafkaTemplate, "test-domain");
        CorrelationIdContext.set("test-correlation-id");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdContext.clear();
    }

    @Test
    @DisplayName("should_PublishEventWithCorrectEnvelope_When_Called")
    void should_PublishEventWithCorrectEnvelope_When_Called() {
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        record TestPayload(String name) {}

        eventPublisher.publish("test-topic", "key-1", "test.item.created", new TestPayload("test"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(eq("test-topic"), eq("key-1"), captor.capture());

        EventEnvelope<?> envelope = captor.getValue();
        assertThat(envelope.sourceDomain()).isEqualTo("test-domain");
        assertThat(envelope.eventType()).isEqualTo("test.item.created");
        assertThat(envelope.correlationId()).isEqualTo("test-correlation-id");
        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("should_UseCorrectTopicAndKey_When_Publishing")
    void should_UseCorrectTopicAndKey_When_Publishing() {
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        eventPublisher.publish("payment-completed", "txn-abc", "pay.txn.completed", "payload");

        verify(kafkaTemplate).send(eq("payment-completed"), eq("txn-abc"), any());
    }
}
