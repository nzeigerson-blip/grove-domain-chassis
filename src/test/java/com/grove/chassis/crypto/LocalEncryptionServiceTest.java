package com.grove.chassis.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the local development AES-256-GCM encryption service.
 */
class LocalEncryptionServiceTest {

    private LocalEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new LocalEncryptionService();
    }

    @Test
    @DisplayName("encrypt + decrypt roundtrip preserves plaintext")
    void encryptDecryptRoundtrip() {
        String plaintext = "NI-AB-123456-C";
        String keyName = "grove-pii-key";

        String encrypted = service.encrypt(plaintext, keyName);
        String decrypted = service.decrypt(encrypted, keyName);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("encrypted value starts with vault:v1: prefix")
    void encryptedValueHasVaultPrefix() {
        String encrypted = service.encrypt("sensitive-data", "grove-pii-key");
        assertThat(encrypted).startsWith("vault:v1:");
    }

    @Test
    @DisplayName("same plaintext produces different ciphertexts (random IV)")
    void sameInputDifferentOutput() {
        String plaintext = "same-value";
        String encrypted1 = service.encrypt(plaintext, "grove-pii-key");
        String encrypted2 = service.encrypt(plaintext, "grove-pii-key");

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("null input returns null")
    void nullInputReturnsNull() {
        assertThat(service.encrypt(null, "grove-pii-key")).isNull();
        assertThat(service.decrypt(null, "grove-pii-key")).isNull();
    }

    @Test
    @DisplayName("blank input returns blank")
    void blankInputReturnsBlank() {
        assertThat(service.encrypt("", "grove-pii-key")).isEmpty();
        assertThat(service.decrypt("", "grove-pii-key")).isEmpty();
    }

    @Test
    @DisplayName("unicode text roundtrips correctly")
    void unicodeRoundtrip() {
        String plaintext = "John O'Brien — SSN: 123-45-6789";
        String encrypted = service.encrypt(plaintext, "grove-pii-key");
        String decrypted = service.decrypt(encrypted, "grove-pii-key");

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("corrupted ciphertext throws EncryptionException")
    void corruptedCiphertextThrows() {
        assertThatThrownBy(() -> service.decrypt("vault:v1:INVALID_BASE64!!!", "grove-pii-key"))
                .isInstanceOf(EncryptionException.class);
    }
}
