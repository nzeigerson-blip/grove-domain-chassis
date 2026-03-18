#!/bin/bash
# Grove Platform — Kafka Topic Initialization for Local Development
# Creates all domain topics and their DLQ counterparts.
#
# Usage:
#   ./create-topics.sh                    # Uses default localhost:9092
#   ./create-topics.sh kafka:9092         # Custom bootstrap server
#
# Run AFTER docker compose up -d and Kafka is healthy.

BOOTSTRAP_SERVER="${1:-localhost:9092}"
PARTITIONS=6
REPLICATION_FACTOR=1
DLQ_PARTITIONS=3
RETENTION_MS=604800000       # 7 days for business topics
DLQ_RETENTION_MS=2592000000  # 30 days for DLQ topics

echo "=== Grove Platform — Kafka Topic Creation ==="
echo "Bootstrap server: $BOOTSTRAP_SERVER"
echo ""

# Function to create a business topic + DLQ pair
create_topic_pair() {
    local topic=$1
    echo "Creating topic: $topic (partitions=$PARTITIONS, retention=7d)"
    docker exec grove-kafka /opt/kafka/bin/kafka-topics.sh \
        --create \
        --bootstrap-server "$BOOTSTRAP_SERVER" \
        --topic "$topic" \
        --partitions "$PARTITIONS" \
        --replication-factor "$REPLICATION_FACTOR" \
        --config retention.ms="$RETENTION_MS" \
        --config cleanup.policy=delete \
        --if-not-exists

    echo "Creating DLQ:   $topic.dlq (partitions=$DLQ_PARTITIONS, retention=30d)"
    docker exec grove-kafka /opt/kafka/bin/kafka-topics.sh \
        --create \
        --bootstrap-server "$BOOTSTRAP_SERVER" \
        --topic "$topic.dlq" \
        --partitions "$DLQ_PARTITIONS" \
        --replication-factor "$REPLICATION_FACTOR" \
        --config retention.ms="$DLQ_RETENTION_MS" \
        --config cleanup.policy=delete \
        --if-not-exists
    echo ""
}

# ─── Sample Domain (chassis template) ───
create_topic_pair "sample-item-created"

# ─── Digital Identity Domain ───
create_topic_pair "digital-identity-account-created"
create_topic_pair "digital-identity-account-activated"
create_topic_pair "digital-identity-account-suspended"
create_topic_pair "digital-identity-email-verified"
create_topic_pair "digital-identity-mfa-enabled"

# ─── Personal Identity Domain ───
create_topic_pair "personal-identity-kyc-completed"
create_topic_pair "personal-identity-kyc-failed"
create_topic_pair "personal-identity-address-verified"
create_topic_pair "personal-identity-pep-screening-completed"
create_topic_pair "personal-identity-affordability-assessed"

# ─── Onboarding Domain ───
create_topic_pair "onboarding-journey-started"
create_topic_pair "onboarding-journey-completed"
create_topic_pair "onboarding-journey-abandoned"
create_topic_pair "onboarding-step-completed"

# ─── Borrow Domain ───
create_topic_pair "borrow-application-submitted"
create_topic_pair "borrow-application-approved"
create_topic_pair "borrow-application-declined"
create_topic_pair "borrow-disbursement-requested"
create_topic_pair "borrow-repayment-received"

# ─── Personal Loan Domain ───
create_topic_pair "personal-loan-schedule-calculated"
create_topic_pair "personal-loan-terms-generated"

# ─── Underwriting Domain ───
create_topic_pair "underwriting-decision-approved"
create_topic_pair "underwriting-decision-declined"
create_topic_pair "underwriting-decision-referred"
create_topic_pair "underwriting-cra-data-received"

# ─── Ledger Domain ───
create_topic_pair "ledger-entry-posted"
create_topic_pair "ledger-allocation-completed"

echo "=== Topic creation complete ==="
echo ""
echo "Listing all topics:"
docker exec grove-kafka /opt/kafka/bin/kafka-topics.sh \
    --list \
    --bootstrap-server "$BOOTSTRAP_SERVER"
