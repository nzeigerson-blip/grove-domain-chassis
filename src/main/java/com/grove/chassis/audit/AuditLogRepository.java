package com.grove.chassis.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for immutable audit log entries.
 * Provides read-only queries for audit trail verification and compliance.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<ImmutableAuditEvent, Long> {

    /**
     * Finds the most recent audit entry to retrieve the latest hash for chain continuity.
     */
    Optional<ImmutableAuditEvent> findTopByOrderBySequenceNumberDesc();

    /**
     * Finds all audit entries for a specific correlation ID (full request trace).
     */
    List<ImmutableAuditEvent> findByCorrelationIdOrderByTimestamp(String correlationId);

    /**
     * Finds all audit entries for a specific actor within a time range.
     */
    List<ImmutableAuditEvent> findByActorAndTimestampBetweenOrderByTimestamp(
            String actor, Instant from, Instant to);

    /**
     * Finds all audit entries for a specific resource.
     */
    List<ImmutableAuditEvent> findByResourceContainingOrderByTimestamp(String resource);

    /**
     * Retrieves a contiguous range of audit entries for hash-chain verification.
     */
    @Query("SELECT a FROM ImmutableAuditEvent a WHERE a.sequenceNumber BETWEEN :fromSeq AND :toSeq ORDER BY a.sequenceNumber")
    List<ImmutableAuditEvent> findBySequenceRange(Long fromSeq, Long toSeq);

    /**
     * Counts entries by domain for reporting.
     */
    long countByDomainName(String domainName);
}
