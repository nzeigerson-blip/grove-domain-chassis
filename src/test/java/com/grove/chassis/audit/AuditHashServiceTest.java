package com.grove.chassis.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the audit hash-chain integrity service.
 */
class AuditHashServiceTest {

    private AuditHashService hashService;

    @BeforeEach
    void setUp() {
        hashService = new AuditHashService();
    }

    @Test
    @DisplayName("hash is deterministic for same inputs")
    void deterministicHash() {
        Instant now = Instant.parse("2026-03-06T10:00:00Z");
        String hash1 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                now, "corr-1", "admin", "GET", "/api/v1/users", "SUCCESS");
        String hash2 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                now, "corr-1", "admin", "GET", "/api/v1/users", "SUCCESS");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("hash changes when any input changes")
    void hashChangesWithInput() {
        Instant now = Instant.parse("2026-03-06T10:00:00Z");
        String hash1 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                now, "corr-1", "admin", "GET", "/api/v1/users", "SUCCESS");
        String hash2 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                now, "corr-1", "admin", "GET", "/api/v1/users", "FAILURE");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("hash is 64 characters (SHA-256 hex)")
    void hashLength() {
        String hash = hashService.computeHash(AuditHashService.GENESIS_HASH,
                Instant.now(), "corr-1", "admin", "GET", "/resource", "SUCCESS");
        assertThat(hash).hasSize(64);
    }

    @Test
    @DisplayName("valid chain verifies successfully")
    void validChainVerifies() {
        Instant t1 = Instant.parse("2026-03-06T10:00:00Z");
        Instant t2 = Instant.parse("2026-03-06T10:01:00Z");

        String hash1 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                t1, "corr-1", "admin", "POST", "/api/v1/users", "SUCCESS");

        String hash2 = hashService.computeHash(hash1,
                t2, "corr-2", "user", "GET", "/api/v1/users/1", "SUCCESS");

        ImmutableAuditEvent event1 = ImmutableAuditEvent.builder()
                .timestamp(t1).correlationId("corr-1").actor("admin").action("POST")
                .resource("/api/v1/users").outcome("SUCCESS")
                .previousHash(AuditHashService.GENESIS_HASH).entryHash(hash1)
                .sequenceNumber(1L).build();

        ImmutableAuditEvent event2 = ImmutableAuditEvent.builder()
                .timestamp(t2).correlationId("corr-2").actor("user").action("GET")
                .resource("/api/v1/users/1").outcome("SUCCESS")
                .previousHash(hash1).entryHash(hash2)
                .sequenceNumber(2L).build();

        assertThat(hashService.verifyChain(List.of(event1, event2))).isTrue();
    }

    @Test
    @DisplayName("tampered chain fails verification")
    void tamperedChainFails() {
        Instant t1 = Instant.parse("2026-03-06T10:00:00Z");

        String hash1 = hashService.computeHash(AuditHashService.GENESIS_HASH,
                t1, "corr-1", "admin", "DELETE", "/api/v1/users/1", "SUCCESS");

        // Tamper: change the entry hash
        ImmutableAuditEvent tampered = ImmutableAuditEvent.builder()
                .timestamp(t1).correlationId("corr-1").actor("admin").action("DELETE")
                .resource("/api/v1/users/1").outcome("SUCCESS")
                .previousHash(AuditHashService.GENESIS_HASH).entryHash("tampered_hash_value")
                .sequenceNumber(1L).build();

        assertThat(hashService.verifyChain(List.of(tampered))).isFalse();
    }

    @Test
    @DisplayName("empty list verifies as true")
    void emptyListVerifies() {
        assertThat(hashService.verifyChain(List.of())).isTrue();
    }
}
