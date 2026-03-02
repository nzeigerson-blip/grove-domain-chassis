-- Chassis-managed: immutable audit log table
-- Separate from domain tables. In production, shipped to dedicated audit store.
-- Retention: 7+ years per compliance requirements.
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGSERIAL PRIMARY KEY,
    timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    correlation_id  VARCHAR(64) NOT NULL,
    actor           VARCHAR(255) NOT NULL,
    action          VARCHAR(255) NOT NULL,
    resource        VARCHAR(512) NOT NULL,
    outcome         VARCHAR(20) NOT NULL,
    source_ip       VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_correlation_id ON audit_log (correlation_id);
CREATE INDEX idx_audit_log_actor ON audit_log (actor);
CREATE INDEX idx_audit_log_timestamp ON audit_log (timestamp);
