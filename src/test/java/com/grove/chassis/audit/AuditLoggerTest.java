package com.grove.chassis.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AuditLoggerTest {

    private final AuditLogger auditLogger = new AuditLogger();

    @Test
    @DisplayName("should_LogSuccessEvent_When_Called")
    void should_LogSuccessEvent_When_Called() {
        AuditEvent event = AuditEvent.success("corr-1", "user@grove.io",
                "GET /api/v1/samples", "/api/v1/samples", "10.0.0.1");

        assertThatNoException().isThrownBy(() -> auditLogger.log(event));
        assertThat(event.outcome()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("should_LogFailureEvent_When_Called")
    void should_LogFailureEvent_When_Called() {
        AuditEvent event = AuditEvent.failure("corr-2", "user@grove.io",
                "POST /api/v1/samples", "/api/v1/samples", "10.0.0.1");

        assertThatNoException().isThrownBy(() -> auditLogger.log(event));
        assertThat(event.outcome()).isEqualTo("FAILURE");
    }

    @Test
    @DisplayName("should_SetTimestamp_When_AuditEventCreated")
    void should_SetTimestamp_When_AuditEventCreated() {
        AuditEvent event = AuditEvent.success("c", "a", "act", "res", "ip");
        assertThat(event.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("should_ContainAllRequiredFields_When_EventCreated")
    void should_ContainAllRequiredFields_When_EventCreated() {
        AuditEvent event = AuditEvent.success("corr-3", "admin@grove.io",
                "DELETE /api/v1/samples/123", "/api/v1/samples/123", "192.168.1.1");

        assertThat(event.correlationId()).isEqualTo("corr-3");
        assertThat(event.actor()).isEqualTo("admin@grove.io");
        assertThat(event.action()).isEqualTo("DELETE /api/v1/samples/123");
        assertThat(event.resource()).isEqualTo("/api/v1/samples/123");
        assertThat(event.sourceIp()).isEqualTo("192.168.1.1");
    }
}
