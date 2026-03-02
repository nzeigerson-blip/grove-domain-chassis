package com.grove.chassis.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Immutable, append-only audit logger.
 * Writes to structured JSON log. In production, these logs are shipped
 * to a separate audit store (not the application database).
 */
@Slf4j
@Component
public class AuditLogger {

    public void log(AuditEvent event) {
        log.info("AUDIT: action={}, resource={}, outcome={}, actor={}, correlationId={}, sourceIp={}",
                event.action(),
                event.resource(),
                event.outcome(),
                event.actor(),
                event.correlationId(),
                event.sourceIp());
    }
}
