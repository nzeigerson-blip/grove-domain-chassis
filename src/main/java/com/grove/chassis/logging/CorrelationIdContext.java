package com.grove.chassis.logging;

import java.util.UUID;

/**
 * Thread-local holder for correlation ID.
 * Used by all chassis components (filter, event publisher, audit interceptor).
 */
public final class CorrelationIdContext {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationIdContext() {
    }

    public static String get() {
        String id = CORRELATION_ID.get();
        return id != null ? id : UUID.randomUUID().toString();
    }

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static void clear() {
        CORRELATION_ID.remove();
    }
}
