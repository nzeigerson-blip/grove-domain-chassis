package com.grove.chassis.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the encryption subsystem.
 * Binds Vault connection properties and provides shared beans.
 */
@Configuration
@ConfigurationProperties(prefix = "grove.vault")
public class CryptoConfig {

    private String addr = "http://localhost:8200";
    private String token = "";

    @Bean("vaultRestTemplate")
    public RestTemplate vaultRestTemplate() {
        return new RestTemplate();
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
