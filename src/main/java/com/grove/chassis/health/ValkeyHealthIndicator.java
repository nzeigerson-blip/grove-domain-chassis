package com.grove.chassis.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Chassis-managed Valkey health indicator.
 * Checks connectivity to the Valkey cache server.
 * Reports UP/DOWN status for Kubernetes readiness probes.
 */
@Component
public class ValkeyHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public ValkeyHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            String pong = connectionFactory.getConnection().ping();
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("cache", "Valkey")
                        .build();
            }
            return Health.down()
                    .withDetail("cache", "Valkey")
                    .withDetail("reason", "Unexpected ping response: " + pong)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("cache", "Valkey")
                    .withDetail("reason", ex.getMessage())
                    .build();
        }
    }
}
