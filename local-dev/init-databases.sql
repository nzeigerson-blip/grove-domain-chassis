-- Grove Platform — Local Development Database Initialization
-- Creates per-domain databases matching the platform architecture.
-- Each domain gets its own database (database-per-domain pattern).

-- Domain databases
CREATE DATABASE grove_digital_identity;
CREATE DATABASE grove_personal_identity;
CREATE DATABASE grove_onboarding;
CREATE DATABASE grove_borrow;
CREATE DATABASE grove_personal_loan;
CREATE DATABASE grove_underwriting;
CREATE DATABASE grove_ledger;

-- Grant access to grove user (already default owner, but explicit for clarity)
GRANT ALL PRIVILEGES ON DATABASE grove_digital_identity TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_personal_identity TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_onboarding TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_borrow TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_personal_loan TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_underwriting TO grove;
GRANT ALL PRIVILEGES ON DATABASE grove_ledger TO grove;
