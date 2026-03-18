-- =============================================================================
-- V2: Enhance audit_log table with hash-chain integrity
-- KAN-15: Security Foundation — Immutable Audit Store
-- =============================================================================

-- Add hash-chain columns to existing audit_log table
ALTER TABLE audit_log
    ADD COLUMN IF NOT EXISTS domain_name     VARCHAR(64),
    ADD COLUMN IF NOT EXISTS entity_type     VARCHAR(128),
    ADD COLUMN IF NOT EXISTS entity_id       VARCHAR(128),
    ADD COLUMN IF NOT EXISTS previous_hash   VARCHAR(64),
    ADD COLUMN IF NOT EXISTS entry_hash      VARCHAR(64) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS sequence_number BIGINT;

-- Create unique index on sequence_number for chain ordering
CREATE UNIQUE INDEX IF NOT EXISTS idx_audit_sequence
    ON audit_log (sequence_number);

-- Create index for hash-chain verification queries
CREATE INDEX IF NOT EXISTS idx_audit_entry_hash
    ON audit_log (entry_hash);

-- Create index for entity-based audit queries
CREATE INDEX IF NOT EXISTS idx_audit_entity
    ON audit_log (entity_type, entity_id);

-- Create index for domain-based queries
CREATE INDEX IF NOT EXISTS idx_audit_domain
    ON audit_log (domain_name);

-- Protect audit_log from UPDATE and DELETE at the database level
-- This trigger rejects any modification to existing audit records
CREATE OR REPLACE FUNCTION prevent_audit_modification()
    RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit log entries are immutable and cannot be modified or deleted';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Block UPDATE on audit_log
DROP TRIGGER IF EXISTS trg_audit_no_update ON audit_log;
CREATE TRIGGER trg_audit_no_update
    BEFORE UPDATE ON audit_log
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_modification();

-- Block DELETE on audit_log
DROP TRIGGER IF EXISTS trg_audit_no_delete ON audit_log;
CREATE TRIGGER trg_audit_no_delete
    BEFORE DELETE ON audit_log
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_modification();

-- Comment for documentation
COMMENT ON TABLE audit_log IS
    'Immutable append-only audit log with SHA-256 hash-chain integrity. '
    'UPDATE and DELETE blocked by database triggers. Minimum 7-year retention.';
