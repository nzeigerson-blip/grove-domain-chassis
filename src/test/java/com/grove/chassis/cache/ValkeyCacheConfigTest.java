package com.grove.chassis.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for ValkeyCacheConfig.
 * Validates configuration wiring without requiring a running Valkey instance.
 */
class ValkeyCacheConfigTest {

    @Test
    @DisplayName("should_CreateConnectionFactory_When_HostAndPortProvided")
    void should_CreateConnectionFactory_When_HostAndPortProvided() {
        ValkeyCacheConfig config = new ValkeyCacheConfig("cache.grove.io", 6379, 60);

        LettuceConnectionFactory factory = config.redisConnectionFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getHostName()).isEqualTo("cache.grove.io");
        assertThat(factory.getPort()).isEqualTo(6379);
    }

    @Test
    @DisplayName("should_CreateCacheManager_When_ConnectionFactoryProvided")
    void should_CreateCacheManager_When_ConnectionFactoryProvided() {
        ValkeyCacheConfig config = new ValkeyCacheConfig("localhost", 6379, 30);
        LettuceConnectionFactory factory = config.redisConnectionFactory();
        factory.afterPropertiesSet();

        var cacheManager = config.cacheManager(factory);

        assertThat(cacheManager).isNotNull();

        factory.destroy();
    }

    @Test
    @DisplayName("should_CreateRedisTemplate_When_ConnectionFactoryProvided")
    void should_CreateRedisTemplate_When_ConnectionFactoryProvided() {
        ValkeyCacheConfig config = new ValkeyCacheConfig("localhost", 6379, 60);
        LettuceConnectionFactory factory = config.redisConnectionFactory();
        factory.afterPropertiesSet();

        var template = config.redisTemplate(factory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(factory);

        factory.destroy();
    }
}
