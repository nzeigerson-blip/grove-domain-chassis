-- SAMPLE: Delete this migration when cloning chassis for a real domain.
-- Shows the pattern: snake_case singular table name, UUID PK, soft-delete flag, timestamps.
CREATE TABLE IF NOT EXISTS sample_item (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
