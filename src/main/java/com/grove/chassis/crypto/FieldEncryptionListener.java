package com.grove.chassis.crypto;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * JPA entity listener that automatically encrypts/decrypts fields annotated
 * with {@link FieldEncryption}. Integrates with the active {@link EncryptionService}
 * (Vault in deployed environments, local AES-256-GCM in dev).
 *
 * <p>Registered as a default entity listener via {@code orm.xml} or
 * applied per-entity using {@code @EntityListeners(FieldEncryptionListener.class)}.</p>
 */
@Component
public class FieldEncryptionListener {

    private static final Logger log = LoggerFactory.getLogger(FieldEncryptionListener.class);

    private final EncryptionService encryptionService;

    public FieldEncryptionListener(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PrePersist
    @PreUpdate
    public void encryptFields(Object entity) {
        processFields(entity, CryptoOperation.ENCRYPT);
    }

    @PostLoad
    public void decryptFields(Object entity) {
        processFields(entity, CryptoOperation.DECRYPT);
    }

    private void processFields(Object entity, CryptoOperation operation) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            FieldEncryption annotation = field.getAnnotation(FieldEncryption.class);
            if (annotation == null) {
                continue;
            }

            if (!String.class.equals(field.getType())) {
                log.warn("@FieldEncryption on non-String field {} in {} — skipping",
                        field.getName(), entity.getClass().getSimpleName());
                continue;
            }

            field.setAccessible(true);
            try {
                String value = (String) field.get(entity);
                if (value == null || value.isBlank()) {
                    continue;
                }

                String processed = switch (operation) {
                    case ENCRYPT -> isAlreadyEncrypted(value)
                            ? value
                            : encryptionService.encrypt(value, annotation.keyName());
                    case DECRYPT -> isAlreadyEncrypted(value)
                            ? encryptionService.decrypt(value, annotation.keyName())
                            : value;
                };

                field.set(entity, processed);
            } catch (IllegalAccessException e) {
                throw new EncryptionException(
                        "Cannot access field " + field.getName() + " on " + entity.getClass().getSimpleName(), e);
            }
        }
    }

    private boolean isAlreadyEncrypted(String value) {
        return value.startsWith("vault:v");
    }

    private enum CryptoOperation {
        ENCRYPT, DECRYPT
    }
}
