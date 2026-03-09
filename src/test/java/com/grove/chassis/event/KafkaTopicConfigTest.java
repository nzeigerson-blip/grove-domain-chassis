package com.grove.chassis.event;

import com.grove.chassis.config.KafkaTopicConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for KafkaTopicConfig.
 * Validates topic creation, naming conventions, and DLQ topic generation.
 */
class KafkaTopicConfigTest {

    private final KafkaTopicConfig config = new KafkaTopicConfig("localhost:9092", 6, (short) 1);

    @Test
    @DisplayName("should_CreateTopicWithCorrectConfig_When_ValidName")
    void should_CreateTopicWithCorrectConfig_When_ValidName() {
        NewTopic topic = config.createTopic("save-account-created");

        assertThat(topic.name()).isEqualTo("save-account-created");
        assertThat(topic.numPartitions()).isEqualTo(6);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("should_CreateDlqTopicWithSuffix_When_BusinessTopicProvided")
    void should_CreateDlqTopicWithSuffix_When_BusinessTopicProvided() {
        NewTopic dlq = config.createDlqTopic("save-account-created");

        assertThat(dlq.name()).isEqualTo("save-account-created.dlq");
        assertThat(dlq.numPartitions()).isEqualTo(3); // DLQ uses fewer partitions
    }

    @Test
    @DisplayName("should_RejectInvalidTopicName_When_NotKebabCase")
    void should_RejectInvalidTopicName_When_NotKebabCase() {
        assertThatThrownBy(() -> config.createTopic("SaveAccountCreated"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kebab-case");
    }

    @Test
    @DisplayName("should_RejectBlankTopicName_When_Empty")
    void should_RejectBlankTopicName_When_Empty() {
        assertThatThrownBy(() -> config.createTopic(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("should_RejectTopicName_When_ContainsUnderscores")
    void should_RejectTopicName_When_ContainsUnderscores() {
        assertThatThrownBy(() -> config.createTopic("save_account_created"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kebab-case");
    }

    @Test
    @DisplayName("should_AcceptSingleWordTopicName_When_Valid")
    void should_AcceptSingleWordTopicName_When_Valid() {
        NewTopic topic = config.createTopic("notifications");
        assertThat(topic.name()).isEqualTo("notifications");
    }

    @Test
    @DisplayName("should_CreateSampleTopicBean_When_Initialized")
    void should_CreateSampleTopicBean_When_Initialized() {
        NewTopic sampleTopic = config.sampleItemCreatedTopic();

        assertThat(sampleTopic.name()).isEqualTo("sample-item-created");
    }

    @Test
    @DisplayName("should_CreateSampleDlqBean_When_Initialized")
    void should_CreateSampleDlqBean_When_Initialized() {
        NewTopic dlq = config.sampleItemCreatedDlq();

        assertThat(dlq.name()).isEqualTo("sample-item-created.dlq");
    }
}
