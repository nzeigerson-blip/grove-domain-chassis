package com.grove.chassis.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chassis-managed OpenTelemetry configuration.
 * Auto-instrumentation via spring-boot-starter. This provides a
 * domain-specific Tracer bean for manual span creation where needed.
 */
@Configuration
public class OpenTelemetryConfig {

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry,
                         @Value("${grove.domain.name}") String domainName) {
        return openTelemetry.getTracer(domainName);
    }
}
