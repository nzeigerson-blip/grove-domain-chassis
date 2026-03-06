package com.grove.chassis.crypto;

/**
 * Provides field-level encryption and decryption using Vault Transit engine
 * or a local fallback for development environments.
 *
 * <p>This is the core interface — implementations are swapped via Spring profiles:</p>
 * <ul>
 *   <li>{@code VaultEncryptionService} — production (Vault Transit API)</li>
 *   <li>{@code LocalEncryptionService} — local dev (AES-256-GCM with static key)</li>
 * </ul>
 */
public interface EncryptionService {

    /**
     * Encrypts a plaintext value using the specified key.
     *
     * @param plaintext the value to encrypt (must not be null)
     * @param keyName   the Vault Transit key name (e.g., "grove-pii-key")
     * @return Base64-encoded ciphertext with version prefix
     */
    String encrypt(String plaintext, String keyName);

    /**
     * Decrypts a ciphertext value using the specified key.
     *
     * @param ciphertext the encrypted value (Base64-encoded with version prefix)
     * @param keyName    the Vault Transit key name
     * @return the original plaintext
     */
    String decrypt(String ciphertext, String keyName);
}
