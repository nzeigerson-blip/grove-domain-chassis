package com.grove.chassis.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for recording immutable audit events with hash-chain integrity.
 *
 * <p>Runs in a REQUIRES_NEW transaction to ensure audit entries are persisted
 * even if the parent business transaction rolls back.</p>
 *
 * <p>Domain code should NOT call this directly — it is invoked by
 * the {@code AuditInterceptor} for HTTP requests and can be extended
 * for Kafka event auditing.</p>
 */
@Service
public class ImmutableAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(ImmutableAuditLogger.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditHashService auditHashService;
    private final String domainName;

    public ImmutableAuditLogger(
            AuditLogRepository auditLogRepository,
            AuditHashService auditHashService,
            @Value("${grove.domain.name:unknown}") String domainName) {
        this.auditLogRepository = auditLogRepository;
        this.auditHashService = auditHashService;
        this.domainName = domainName;
    }

    /**
     * Records an audit event with hash-chain integrity.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImmutableAuditEvent record(String correlationId, String actor, String action,
                                       String resource, String outcome, String sourceIp,
                                       String entityType, String entityId) {
        // Get the previous entry for chain continuity
        var previousEntry = auditLogRepository.findTopByOrderBySequenceNumberDesc();
        String previousHash = previousEntry.map(ImmutableAuditEvent::getEntryHash)
                .orElse(AuditHashService.GENESIS_HASH);
        long nextSequence = previousEntry.map(e -> e.getSequenceNumber() + 1).orElse(1L);

        Instant now = Instant.now();

        // Compute hash for this entry
        String entryHash = auditHashService.computeHash(
                previousHash, now, correlationId, actor, action, resource, outcome);

        ImmutableAuditEvent event = ImmutableAuditEvent.builder()
                .timestamp(now)
                .correlationId(correlationId)
                .actor(actor)
                .action(action)
                .resource(resource)
                .outcome(outcome)
                .sourceIp(sourceIp)
                .domainName(domainName)
                .entityType(entityType)
                .entityId(entityId)
                .previousHash(previousHash)
                .entryHash(entryHash)
                .sequenceNumber(nextSequence)
                .build();

        ImmutableAuditEvent saved = auditLogRepository.save(event);

        log.info("Audit: seq={} actor={} action={} resource={} outcome={} hash={}",
                saved.getSequenceNumber(), actor, action, resource, outcome,
                entryHash.substring(0, 16) + "...");

        return saved;
    }

    /**
     * Verifies the integrity of the entire audit chain or a range.
     *
     * @param fromSequence start of range (inclusive)
     * @param toSequence   end of range (inclusive)
     * @return true if the chain is intact
     */
    @Transactional(readOnly = true)
    public boolean verifyIntegrity(long fromSequence, long toSequence) {
        var entries = auditLogRepository.findBySequenceRange(fromSequence, toSequence);
        boolean intact = auditHashService.verifyChain(entries);

        if (!intact) {
            log.error("AUDIT INTEGRITY VIOLATION: hash chain broken in range [{}, {}]",
                    fromSequence, toSequence);
        }

        return intact;
    }
}
