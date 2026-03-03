package com.grove.chassis.config;

import com.grove.chassis.event.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Chassis-managed Kafka consumer configuration.
 * Includes Dead Letter Queue (DLQ) routing for poison messages.
 *
 * Error handling strategy:
 *   1. Retry 3 times with 1-second backoff
 *   2. On exhausted retries, publish to DLQ topic ({original-topic}.dlq)
 *   3. Log poison message details for investigation
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    private final String bootstrapServers;
    private final String groupId;
    private final int maxRetries;
    private final long retryBackoffMs;

    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${grove.kafka.consumer.max-retries:3}") int maxRetries,
            @Value("${grove.kafka.consumer.retry-backoff-ms:1000}") long retryBackoffMs) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.maxRetries = maxRetries;
        this.retryBackoffMs = retryBackoffMs;
    }

    @Bean
    public ConsumerFactory<String, EventEnvelope<?>> consumerFactory() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName(),
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName(),
                JsonDeserializer.TRUSTED_PACKAGES, "com.grove.*",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * DLQ recoverer: routes failed messages to {original-topic}.dlq after retries are exhausted.
     * Logs the poison message details including topic, partition, offset, and exception.
     */
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    String dlqTopic = record.topic() + ".dlq";
                    log.error("Routing poison message to DLQ: topic={}, dlqTopic={}, partition={}, offset={}, key={}",
                            record.topic(), dlqTopic, record.partition(), record.offset(), record.key(), ex);
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, -1);
                }
        );
        return recoverer;
    }

    /**
     * Error handler with DLQ support:
     *   - Retries up to maxRetries times with fixed backoff
     *   - After exhaustion, delegates to DeadLetterPublishingRecoverer
     *   - Logs each retry attempt for observability
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
                new FixedBackOff(retryBackoffMs, maxRetries)
        );
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Kafka consumer retry: topic={}, partition={}, offset={}, attempt={}/{}",
                        record.topic(), record.partition(), record.offset(),
                        deliveryAttempt, maxRetries, ex)
        );
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> kafkaListenerContainerFactory(
            ConsumerFactory<String, EventEnvelope<?>> consumerFactory,
            CommonErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
