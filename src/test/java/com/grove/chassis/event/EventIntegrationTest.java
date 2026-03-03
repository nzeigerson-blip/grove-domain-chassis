package com.grove.chassis.event;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: proves end-to-end event publish → subscribe via embedded Kafka.
 * Validates EventEnvelope serialization, topic auto-creation, and message delivery.
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 3,
        topics = {"sample-item-created", "sample-item-created.dlq", "integration-test-topic", "integration-test-topic.dlq"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth.grove.io",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://test.auth.grove.io/.well-known/jwks.json",
        "grove.domain.name=test-domain",
        "grove.cache.host=localhost",
        "grove.cache.port=6379",
        "grove.kafka.dlq.topics=sample-item-created.dlq,integration-test-topic.dlq",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration"
})
class EventIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    @DisplayName("should_PublishAndConsumeEvent_When_EndToEndFlow")
    void should_PublishAndConsumeEvent_When_EndToEndFlow() throws Exception {
        // Arrange — set up a consumer to read from the test topic
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.grove.*");

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());

        var consumer = consumerFactory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "integration-test-topic");

        // Act — publish an event using EventPublisher
        record TestPayload(UUID id, String name, String description, Instant createdAt) {}

        UUID entityId = UUID.randomUUID();
        TestPayload payload = new TestPayload(entityId, "Test Entity", "Integration test payload", Instant.now());

        CompletableFuture<SendResult<String, EventEnvelope<?>>> future =
                eventPublisher.publish("integration-test-topic", entityId.toString(), "test.entity.created", payload);

        SendResult<String, EventEnvelope<?>> result = future.get();

        // Assert — verify the message was sent to the correct topic and partition
        assertThat(result).isNotNull();
        assertThat(result.getRecordMetadata().topic()).isEqualTo("integration-test-topic");
        assertThat(result.getRecordMetadata().offset()).isGreaterThanOrEqualTo(0);

        // Assert — consume and verify the message content
        ConsumerRecord<String, String> received = KafkaTestUtils.getSingleRecord(consumer, "integration-test-topic");
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo(entityId.toString());
        assertThat(received.value()).contains("test.entity.created");
        assertThat(received.value()).contains("test-domain");
        assertThat(received.value()).contains("Test Entity");

        consumer.close();
    }

    @Test
    @DisplayName("should_ContainAllEnvelopeFields_When_EventPublished")
    void should_ContainAllEnvelopeFields_When_EventPublished() throws Exception {
        // Arrange
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("envelope-test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());

        var consumer = consumerFactory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "sample-item-created");

        // Act
        record ItemPayload(UUID id, String name) {}
        UUID id = UUID.randomUUID();
        eventPublisher.publish("sample-item-created", id.toString(), "sample.item.created", new ItemPayload(id, "Widget"));

        // Assert — all envelope fields present
        ConsumerRecord<String, String> received = KafkaTestUtils.getSingleRecord(consumer, "sample-item-created");
        String value = received.value();

        assertThat(value).contains("\"eventId\"");
        assertThat(value).contains("\"timestamp\"");
        assertThat(value).contains("\"correlationId\"");
        assertThat(value).contains("\"sourceDomain\"");
        assertThat(value).contains("\"eventType\"");
        assertThat(value).contains("\"payload\"");
        assertThat(value).contains("\"sourceDomain\":\"test-domain\"");
        assertThat(value).contains("\"eventType\":\"sample.item.created\"");

        consumer.close();
    }

    @Test
    @DisplayName("should_UsePartitionKey_When_PublishingWithEntityId")
    void should_UsePartitionKey_When_PublishingWithEntityId() throws Exception {
        // Arrange
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("partition-test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());

        var consumer = consumerFactory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "integration-test-topic");

        // Act — publish two events with same key (should go to same partition)
        String entityKey = UUID.randomUUID().toString();
        eventPublisher.publish("integration-test-topic", entityKey, "test.entity.updated", "payload1").get();
        eventPublisher.publish("integration-test-topic", entityKey, "test.entity.updated", "payload2").get();

        // Assert — both events have same key and go to same partition
        var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        int partition = -1;
        int count = 0;
        for (ConsumerRecord<String, String> record : records) {
            if (record.topic().equals("integration-test-topic") && record.key().equals(entityKey)) {
                if (partition == -1) {
                    partition = record.partition();
                } else {
                    assertThat(record.partition()).isEqualTo(partition);
                }
                count++;
            }
        }
        assertThat(count).isEqualTo(2);

        consumer.close();
    }
}
