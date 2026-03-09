#!/bin/sh
# =============================================================================
# Vault Local Dev Initialization Script
# Configures Transit engine, PKI, and policies for local development
# =============================================================================
set -e

echo "=== Initializing Vault for Grove local development ==="

# Enable Transit secrets engine (encryption-as-a-service)
vault secrets enable transit 2>/dev/null || echo "Transit already enabled"

# Create PII encryption key (AES-256-GCM)
vault write -f transit/keys/grove-pii-key \
  type=aes256-gcm96

# Create general encryption key
vault write -f transit/keys/grove-general-key \
  type=aes256-gcm96

# Enable KV v2 secrets engine
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "KV v2 already enabled"

# Pre-populate dev secrets for each domain
for domain in digital-identity personal-identity onboarding borrow personal-loan underwriting ledger; do
  vault kv put "secret/grove/dev/${domain}/db" \
    username="grove" \
    password="grove" \
    url="jdbc:postgresql://localhost:5432/grove_${domain//-/_}"

  echo "  Created secrets for domain: ${domain}"
done

# Shared secrets
vault kv put secret/grove/dev/shared/kafka \
  bootstrap-servers="localhost:9092"

vault kv put secret/grove/dev/shared/valkey \
  host="localhost" \
  port="6379"

vault kv put secret/grove/dev/shared/keycloak \
  issuer-uri="http://localhost:8180/realms/grove-dev" \
  jwk-set-uri="http://localhost:8180/realms/grove-dev/protocol/openid-connect/certs"

# Create dev policy (permissive for local development)
vault policy write grove-dev - <<'POLICY'
path "secret/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "transit/*" {
  capabilities = ["create", "read", "update", "list"]
}
POLICY

echo "=== Vault initialization complete ==="
echo "  Transit engine: enabled (grove-pii-key, grove-general-key)"
echo "  KV v2 secrets:  pre-populated for all domains"
echo "  Dev policy:     grove-dev (full access)"
