package com.grove.chassis.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Chassis health indicator checking database connectivity.
 * Used by Kubernetes liveness/readiness probes.
 * Domain teams can add additional HealthIndicator beans for domain-specific checks.
 */
@Component
public class DomainHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DomainHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return Health.up().withDetail("database", "reachable").build();
            }
        } catch (Exception ex) {
            return Health.down().withDetail("database", "unreachable").withException(ex).build();
        }
        return Health.down().withDetail("database", "unreachable").build();
    }
}
