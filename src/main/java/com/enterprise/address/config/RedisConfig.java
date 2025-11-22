package com.enterprise.address.config;

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
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration class for Redis caching.
 * This class sets up the Redis connection and cache management.
 */
@Configuration
@EnableCaching  // Enables Spring's annotation-driven cache management
public class RedisConfig {
    
    /**
     * Creates and configures the RedisTemplate bean.
     * This template provides a high-level abstraction for Redis operations.
     * 
     * @param connectionFactory The Redis connection factory
     * @return Configured RedisTemplate instance
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        // Configure host and port if different from default
        // config.setHostName("localhost");
        // config.setPort(6379);
        
        return new LettuceConnectionFactory(config);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys for better readability in Redis CLI
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Configure key serializers - ensures keys are stored as strings in Redis
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Use JSON serializer for values - converts Java objects to JSON for storage
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());
        
        template.afterPropertiesSet();
        
        return template;
    }
    
    
    
    /**
     * Configures the CacheManager bean for Spring's caching abstraction.
     * This defines how caches are created and managed.
     * 
     * @param connectionFactory The Redis connection factory
     * @return Configured CacheManager instance
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure default cache settings
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // Cache entries expire after 10 minutes
            .disableCachingNullValues()        // Don't cache null values
            // Serialize keys as strings
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            // Serialize values as JSON
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(RedisSerializer.json()));
            
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)         // Apply the default configuration
            .transactionAware()                 // Enable transaction support
            .build();
    }
}
