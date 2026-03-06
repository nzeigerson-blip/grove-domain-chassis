package com.grove.chassis.crypto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as containing PII that must be encrypted at the application layer.
 * Fields annotated with {@code @FieldEncryption} are automatically encrypted before
 * persistence and decrypted after retrieval.
 *
 * <p>Supported on {@code String} entity fields only. The encrypted value is stored
 * as a Base64-encoded ciphertext with a key-version prefix for rotation support.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * &#64;FieldEncryption(keyName = "grove-pii-key")
 * private String nationalInsuranceNumber;
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldEncryption {

    /**
     * The encryption key name in Vault Transit engine.
     * Defaults to "grove-pii-key" for PII fields.
     */
    String keyName() default "grove-pii-key";
}
