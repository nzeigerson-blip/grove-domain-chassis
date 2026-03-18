package com.grove.chassis.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Chassis-managed Valkey (cache) configuration.
 * Uses Spring Data Redis with Lettuce client — fully compatible with Valkey 7.2+.
 *
 * Valkey is the Apache 2.0 licensed fork (not Redis Ltd).
 * Cloud portability: works with Amazon ElastiCache (Valkey), self-hosted Valkey, or any Redis-compatible cache.
 *
 * Provides:
 *   - LettuceConnectionFactory for Valkey connectivity
 *   - RedisCacheManager with JSON serialization and configurable TTL
 *   - RedisTemplate for direct key-value operations (rate limiting, sessions, etc.)
 */
@Slf4j
@Configuration
@EnableCaching
public class ValkeyCacheConfig {

    private final String host;
    private final int port;
    private final long defaultTtlMinutes;

    public ValkeyCacheConfig(
            @Value("${grove.cache.host:localhost}") String host,
            @Value("${grove.cache.port:6379}") int port,
            @Value("${grove.cache.default-ttl-minutes:60}") long defaultTtlMinutes) {
        this.host = host;
        this.port = port;
        this.defaultTtlMinutes = defaultTtlMinutes;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        log.info("Valkey connection configured: host={}, port={}", host, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("grove:");

        log.info("Valkey CacheManager configured: defaultTtl={}min", defaultTtlMinutes);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware()
                .build();
    }

    /**
     * General-purpose RedisTemplate for operations beyond @Cacheable:
     *   - Rate limiting counters
     *   - Session storage
     *   - Distributed locks
     *   - Temporary data sharing between instances
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
