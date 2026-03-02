package com.grove.chassis.audit;

import java.time.Instant;

/**
 * Immutable audit event record.
 * Fields per security spec: timestamp, correlationId, actor, action, resource, outcome, sourceIP.
 * Retention: 7+ years. Stored in immutable append-only audit log.
 */
public record AuditEvent(
        Instant timestamp,
        String correlationId,
        String actor,
        String action,
        String resource,
        String outcome,
        String sourceIp
) {
    public static AuditEvent success(String correlationId, String actor, String action,
                                     String resource, String sourceIp) {
        return new AuditEvent(Instant.now(), correlationId, actor, action, resource, "SUCCESS", sourceIp);
    }

    public static AuditEvent failure(String correlationId, String actor, String action,
                                     String resource, String sourceIp) {
        return new AuditEvent(Instant.now(), correlationId, actor, action, resource, "FAILURE", sourceIp);
    }
}
