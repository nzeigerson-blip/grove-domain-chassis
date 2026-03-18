package com.grove.chassis.audit;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Computes SHA-256 hashes for audit log entries, forming a tamper-evident chain.
 *
 * <p>Each entry's hash is computed as:
 * {@code SHA-256(previousHash + timestamp + correlationId + actor + action + resource + outcome)}</p>
 *
 * <p>The first entry in the chain uses a well-known genesis hash.</p>
 */
@Service
public class AuditHashService {

    /** Genesis hash for the first entry in the chain. */
    public static final String GENESIS_HASH =
            "0000000000000000000000000000000000000000000000000000000000000000";

    private static final HexFormat HEX = HexFormat.of();

    /**
     * Computes the hash for an audit entry, chaining it to the previous entry.
     *
     * @param previousHash the hash of the preceding entry (or GENESIS_HASH for the first)
     * @param timestamp    event timestamp
     * @param correlationId request correlation ID
     * @param actor        the authenticated user or service
     * @param action       the action performed
     * @param resource     the resource acted upon
     * @param outcome      SUCCESS or FAILURE
     * @return SHA-256 hex-encoded hash
     */
    public String computeHash(String previousHash, Instant timestamp, String correlationId,
                              String actor, String action, String resource, String outcome) {
        String payload = String.join("|",
                previousHash != null ? previousHash : GENESIS_HASH,
                timestamp.toString(),
                correlationId,
                actor,
                action,
                resource,
                outcome
        );

        return sha256(payload);
    }

    /**
     * Verifies the integrity of a contiguous sequence of audit entries.
     *
     * @param entries ordered list of audit events to verify
     * @return true if the hash chain is intact, false if tampering is detected
     */
    public boolean verifyChain(java.util.List<ImmutableAuditEvent> entries) {
        if (entries == null || entries.isEmpty()) {
            return true;
        }

        for (ImmutableAuditEvent entry : entries) {
            String expectedHash = computeHash(
                    entry.getPreviousHash(),
                    entry.getTimestamp(),
                    entry.getCorrelationId(),
                    entry.getActor(),
                    entry.getAction(),
                    entry.getResource(),
                    entry.getOutcome()
            );

            if (!expectedHash.equals(entry.getEntryHash())) {
                return false;
            }
        }

        // Verify chain linkage (each entry's previousHash matches the prior entry's entryHash)
        for (int i = 1; i < entries.size(); i++) {
            String expectedPrevious = entries.get(i - 1).getEntryHash();
            String actualPrevious = entries.get(i).getPreviousHash();
            if (!expectedPrevious.equals(actualPrevious)) {
                return false;
            }
        }

        return true;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
