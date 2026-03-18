package com.grove.chassis.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Production encryption service that delegates to HashiCorp Vault Transit engine.
 * Uses Vault's encryption-as-a-service to encrypt/decrypt PII fields without
 * exposing encryption keys to the application.
 *
 * <p>Active on profiles: dev (EKS), stg, prod — NOT on local/default profile.</p>
 */
@Service
@Profile({"dev", "stg", "prod"})
public class VaultEncryptionService implements EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(VaultEncryptionService.class);

    private final RestTemplate restTemplate;
    private final String vaultAddr;
    private final String vaultToken;

    public VaultEncryptionService(
            RestTemplate restTemplate,
            @Value("${grove.vault.addr}") String vaultAddr,
            @Value("${grove.vault.token}") String vaultToken) {
        this.restTemplate = restTemplate;
        this.vaultAddr = vaultAddr;
        this.vaultToken = vaultToken;
    }

    @Override
    public String encrypt(String plaintext, String keyName) {
        if (plaintext == null || plaintext.isBlank()) {
            return plaintext;
        }

        String base64Plaintext = Base64.getEncoder().encodeToString(plaintext.getBytes());

        HttpHeaders headers = createHeaders();
        Map<String, String> body = Map.of("plaintext", base64Plaintext);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                vaultAddr + "/v1/transit/encrypt/" + keyName,
                request,
                Map.class
        );

        if (response == null || !response.containsKey("data")) {
            throw new EncryptionException("Vault returned empty response for encrypt operation");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) response.get("data");
        String ciphertext = data.get("ciphertext");

        log.debug("Encrypted field using key={}", keyName);
        return ciphertext;
    }

    @Override
    public String decrypt(String ciphertext, String keyName) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return ciphertext;
        }

        HttpHeaders headers = createHeaders();
        Map<String, String> body = Map.of("ciphertext", ciphertext);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                vaultAddr + "/v1/transit/decrypt/" + keyName,
                request,
                Map.class
        );

        if (response == null || !response.containsKey("data")) {
            throw new EncryptionException("Vault returned empty response for decrypt operation");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) response.get("data");
        String base64Plaintext = data.get("plaintext");

        log.debug("Decrypted field using key={}", keyName);
        return new String(Base64.getDecoder().decode(base64Plaintext));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Vault-Token", vaultToken);
        return headers;
    }
}
