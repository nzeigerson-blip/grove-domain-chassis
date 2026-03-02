package com.grove.chassis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth.grove.io",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://test.auth.grove.io/.well-known/jwks.json",
        "grove.domain.name=test"
})
class GroveChassisApplicationTest {

    @Test
    @DisplayName("should_LoadApplicationContext_When_Started")
    void should_LoadApplicationContext_When_Started() {
        // Context loads successfully — validates all chassis beans wire correctly
    }
}
