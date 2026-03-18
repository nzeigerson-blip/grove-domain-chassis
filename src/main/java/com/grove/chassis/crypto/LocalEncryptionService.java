package com.grove.chassis.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Local development encryption service using AES-256-GCM.
 * Uses a static key derived from a deterministic seed for reproducible dev testing.
 *
 * <p>WARNING: This is for local development only. Never use in production.</p>
 * <p>Active on the default Spring profile (no profile = local dev).</p>
 */
@Service
@Profile("default")
public class LocalEncryptionService implements EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(LocalEncryptionService.class);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final String LOCAL_PREFIX = "vault:v1:";

    // Static dev-only key (32 bytes = AES-256). NEVER use in production.
    private static final byte[] DEV_KEY_BYTES = {
            0x47, 0x72, 0x6F, 0x76, 0x65, 0x2D, 0x44, 0x65,
            0x76, 0x2D, 0x4F, 0x6E, 0x6C, 0x79, 0x2D, 0x4B,
            0x65, 0x79, 0x2D, 0x32, 0x35, 0x36, 0x2D, 0x42,
            0x69, 0x74, 0x73, 0x2D, 0x48, 0x65, 0x72, 0x65
    };

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public LocalEncryptionService() {
        this.secretKey = new SecretKeySpec(DEV_KEY_BYTES, "AES");
        this.secureRandom = new SecureRandom();
        log.warn("Using LOCAL encryption service — NOT suitable for production");
    }

    @Override
    public String encrypt(String plaintext, String keyName) {
        if (plaintext == null || plaintext.isBlank()) {
            return plaintext;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            // Prepend IV to ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            String encoded = Base64.getEncoder().encodeToString(buffer.array());
            return LOCAL_PREFIX + encoded;
        } catch (Exception e) {
            throw new EncryptionException("Local encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext, String keyName) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return ciphertext;
        }

        try {
            String encoded = ciphertext.startsWith(LOCAL_PREFIX)
                    ? ciphertext.substring(LOCAL_PREFIX.length())
                    : ciphertext;

            byte[] decoded = Base64.getDecoder().decode(encoded);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encryptedBytes = new byte[buffer.remaining()];
            buffer.get(encryptedBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(encryptedBytes);
            return new String(plaintext);
        } catch (Exception e) {
            throw new EncryptionException("Local decryption failed", e);
        }
    }
}
