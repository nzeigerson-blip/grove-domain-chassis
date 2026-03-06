package com.grove.chassis.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Immutable audit log entry with hash-chain integrity.
 *
 * <p>Each entry stores a SHA-256 hash of its own content combined with the
 * previous entry's hash, forming an append-only tamper-evident chain.
 * Any modification to historical entries breaks the chain and is detectable.</p>
 *
 * <p>This entity maps to a separate audit-specific schema/database in production
 * to ensure domain applications cannot modify audit records.</p>
 */
@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_correlation_id", columnList = "correlation_id"),
                @Index(name = "idx_audit_actor", columnList = "actor"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
                @Index(name = "idx_audit_resource", columnList = "resource"),
                @Index(name = "idx_audit_action", columnList = "action")
        }
)
public class ImmutableAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "correlation_id", nullable = false, updatable = false, length = 64)
    private String correlationId;

    @Column(name = "actor", nullable = false, updatable = false, length = 256)
    private String actor;

    @Column(name = "action", nullable = false, updatable = false, length = 32)
    private String action;

    @Column(name = "resource", nullable = false, updatable = false, length = 512)
    private String resource;

    @Column(name = "outcome", nullable = false, updatable = false, length = 16)
    private String outcome;

    @Column(name = "source_ip", updatable = false, length = 45)
    private String sourceIp;

    @Column(name = "domain_name", nullable = false, updatable = false, length = 64)
    private String domainName;

    @Column(name = "entity_type", updatable = false, length = 128)
    private String entityType;

    @Column(name = "entity_id", updatable = false, length = 128)
    private String entityId;

    @Column(name = "previous_hash", updatable = false, length = 64)
    private String previousHash;

    @Column(name = "entry_hash", nullable = false, updatable = false, length = 64)
    private String entryHash;

    @Column(name = "sequence_number", nullable = false, updatable = false)
    private Long sequenceNumber;

    protected ImmutableAuditEvent() {
        // JPA
    }

    private ImmutableAuditEvent(Builder builder) {
        this.timestamp = builder.timestamp;
        this.correlationId = builder.correlationId;
        this.actor = builder.actor;
        this.action = builder.action;
        this.resource = builder.resource;
        this.outcome = builder.outcome;
        this.sourceIp = builder.sourceIp;
        this.domainName = builder.domainName;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.previousHash = builder.previousHash;
        this.entryHash = builder.entryHash;
        this.sequenceNumber = builder.sequenceNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- Getters only (immutable) ---

    public Long getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public String getOutcome() { return outcome; }
    public String getSourceIp() { return sourceIp; }
    public String getDomainName() { return domainName; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getPreviousHash() { return previousHash; }
    public String getEntryHash() { return entryHash; }
    public Long getSequenceNumber() { return sequenceNumber; }

    public static class Builder {
        private Instant timestamp;
        private String correlationId;
        private String actor;
        private String action;
        private String resource;
        private String outcome;
        private String sourceIp;
        private String domainName;
        private String entityType;
        private String entityId;
        private String previousHash;
        private String entryHash;
        private Long sequenceNumber;

        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder actor(String actor) { this.actor = actor; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder resource(String resource) { this.resource = resource; return this; }
        public Builder outcome(String outcome) { this.outcome = outcome; return this; }
        public Builder sourceIp(String sourceIp) { this.sourceIp = sourceIp; return this; }
        public Builder domainName(String domainName) { this.domainName = domainName; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(String entityId) { this.entityId = entityId; return this; }
        public Builder previousHash(String previousHash) { this.previousHash = previousHash; return this; }
        public Builder entryHash(String entryHash) { this.entryHash = entryHash; return this; }
        public Builder sequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; return this; }

        public ImmutableAuditEvent build() {
            return new ImmutableAuditEvent(this);
        }
    }
}
