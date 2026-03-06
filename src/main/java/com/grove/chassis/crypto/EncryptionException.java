package com.grove.chassis.crypto;

/**
 * Thrown when an encryption or decryption operation fails.
 * This is an unchecked exception — callers should not attempt to recover.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
