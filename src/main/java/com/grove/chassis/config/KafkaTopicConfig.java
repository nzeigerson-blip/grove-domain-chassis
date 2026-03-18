package com.grove.chassis.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

/**
 * Chassis-managed Kafka topic auto-provisioning.
 * Creates domain topics and their corresponding DLQ topics on startup.
 *
 * Topic naming convention:
 *   - Business topics: {domain}-{entity}-{action} (kebab-case, domain-prefixed)
 *   - DLQ topics: {domain}-{entity}-{action}.dlq
 *
 * Domains register their topics by defining NewTopic beans in their own config.
 * This class provides the KafkaAdmin and DLQ topic factory.
 */
@Slf4j
@Configuration
public class KafkaTopicConfig {

    private final String bootstrapServers;
    private final int defaultPartitions;
    private final short defaultReplicationFactor;

    public KafkaTopicConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${grove.kafka.topic.default-partitions:6}") int defaultPartitions,
            @Value("${grove.kafka.topic.default-replication-factor:1}") short defaultReplicationFactor) {
        this.bootstrapServers = bootstrapServers;
        this.defaultPartitions = defaultPartitions;
        this.defaultReplicationFactor = defaultReplicationFactor;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        );
        KafkaAdmin admin = new KafkaAdmin(configs);
        admin.setAutoCreate(true);
        log.info("KafkaAdmin configured: bootstrapServers={}, defaultPartitions={}, defaultReplicationFactor={}",
                bootstrapServers, defaultPartitions, defaultReplicationFactor);
        return admin;
    }

    /**
     * Creates a business topic with standard configuration.
     * Domain code should call this to define topics:
     *
     * <pre>{@code
     * @Bean
     * public NewTopic myDomainTopic(KafkaTopicConfig topicConfig) {
     *     return topicConfig.createTopic("save-account-created");
     * }
     * }</pre>
     *
     * @param topicName kebab-case, domain-prefixed topic name
     * @return NewTopic bean for auto-creation
     */
    public NewTopic createTopic(String topicName) {
        validateTopicName(topicName);
        log.info("Registering topic: name={}, partitions={}, replicationFactor={}",
                topicName, defaultPartitions, defaultReplicationFactor);
        return TopicBuilder.name(topicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicationFactor)
                .config("retention.ms", "604800000") // 7 days
                .config("cleanup.policy", "delete")
                .build();
    }

    /**
     * Creates a DLQ topic for a given business topic.
     * DLQ topics use fewer partitions (3) since they carry low volume.
     *
     * @param businessTopicName the business topic this DLQ is paired with
     * @return NewTopic bean for auto-creation
     */
    public NewTopic createDlqTopic(String businessTopicName) {
        String dlqName = businessTopicName + ".dlq";
        log.info("Registering DLQ topic: name={}", dlqName);
        return TopicBuilder.name(dlqName)
                .partitions(3)
                .replicas(defaultReplicationFactor)
                .config("retention.ms", "2592000000") // 30 days (longer for investigation)
                .config("cleanup.policy", "delete")
                .build();
    }

    /**
     * Sample domain topic — domains replace this with their own.
     */
    @Bean
    public NewTopic sampleItemCreatedTopic() {
        return createTopic("sample-item-created");
    }

    @Bean
    public NewTopic sampleItemCreatedDlq() {
        return createDlqTopic("sample-item-created");
    }

    private void validateTopicName(String topicName) {
        if (topicName == null || topicName.isBlank()) {
            throw new IllegalArgumentException("Topic name must not be blank");
        }
        if (!topicName.matches("^[a-z][a-z0-9]*(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException(
                    "Topic name must be kebab-case, domain-prefixed: " + topicName +
                    " (expected pattern: {domain}-{entity}-{action})");
        }
    }
}
