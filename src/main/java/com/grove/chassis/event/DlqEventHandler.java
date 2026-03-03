package com.grove.chassis.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Chassis-managed DLQ event handler.
 * Monitors DLQ topics and logs poison messages with full context for investigation.
 *
 * Domains can override this by defining their own @KafkaListener for specific DLQ topics
 * with custom recovery logic (e.g., alerting, manual retry queues).
 *
 * DLQ topic pattern: {business-topic}.dlq
 *
 * Headers added by DeadLetterPublishingRecoverer:
 *   - kafka_dlt-original-topic: source topic
 *   - kafka_dlt-original-partition: source partition
 *   - kafka_dlt-original-offset: source offset
 *   - kafka_dlt-exception-message: failure reason
 *   - kafka_dlt-exception-fqcn: exception class name
 */
@Slf4j
@Component
public class DlqEventHandler {

    /**
     * Listens to all DLQ topics matching the pattern.
     * Domains should configure their DLQ topic patterns via grove.kafka.dlq.topic-pattern.
     *
     * Default pattern listens to sample domain DLQ.
     * Each domain overrides this with their own DLQ listeners.
     */
    @KafkaListener(
            topics = "${grove.kafka.dlq.topics:sample-item-created.dlq}",
            groupId = "${spring.kafka.consumer.group-id}-dlq"
    )
    public void handleDlqMessage(ConsumerRecord<String, ?> record) {
        String originalTopic = extractHeader(record, "kafka_dlt-original-topic");
        String originalPartition = extractHeader(record, "kafka_dlt-original-partition");
        String originalOffset = extractHeader(record, "kafka_dlt-original-offset");
        String exceptionMessage = extractHeader(record, "kafka_dlt-exception-message");
        String exceptionClass = extractHeader(record, "kafka_dlt-exception-fqcn");

        log.error("POISON MESSAGE received on DLQ: " +
                        "dlqTopic={}, key={}, " +
                        "originalTopic={}, originalPartition={}, originalOffset={}, " +
                        "exceptionClass={}, exceptionMessage={}, " +
                        "value={}",
                record.topic(), record.key(),
                originalTopic, originalPartition, originalOffset,
                exceptionClass, exceptionMessage,
                record.value());
    }

    private String extractHeader(ConsumerRecord<String, ?> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null) {
            return "unknown";
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
