package com.grove.chassis.event;

import com.grove.chassis.logging.CorrelationIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Chassis-provided event publisher. Domain code uses this to publish events.
 * Automatically wraps payloads in the standard EventEnvelope and injects
 * correlationId from the request context.
 */
@Slf4j
@Component
public class EventPublisher {

    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;
    private final String serviceName;

    public EventPublisher(
            KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate,
            @Value("${grove.domain.name}") String serviceName) {
        this.kafkaTemplate = kafkaTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish a domain event with a rich payload.
     *
     * @param topic     Kafka topic (kebab-case, domain-prefixed, e.g. "save-account-withdrawal-completed")
     * @param key       Partition key (typically entity ID for ordering)
     * @param eventType Event type in dot-notation (e.g. "save.account.withdrawal-completed")
     * @param payload   Rich payload with ALL related domain data — consumers must not call back
     */
    public <T> CompletableFuture<SendResult<String, EventEnvelope<?>>> publish(
            String topic,
            String key,
            String eventType,
            T payload) {

        String correlationId = CorrelationIdContext.get();

        EventEnvelope<T> envelope = EventEnvelope.create(
                correlationId,
                serviceName,
                eventType,
                payload
        );

        log.info("Publishing event: eventType={}, topic={}, key={}, eventId={}, correlationId={}",
                eventType, topic, key, envelope.eventId(), correlationId);

        return kafkaTemplate.send(topic, key, (EventEnvelope<?>) envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event: eventType={}, topic={}, key={}, correlationId={}",
                                eventType, topic, key, correlationId, ex);
                    } else {
                        log.info("Event published: eventType={}, topic={}, partition={}, offset={}, correlationId={}",
                                eventType, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                correlationId);
                    }
                });
    }
}
