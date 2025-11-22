package com.enterprise.address.service;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service responsible for managing application caches.
 * 
 * <p>This service handles both Spring's CacheManager caches and direct Redis operations.
 * It provides methods to clear caches on demand and automatically manages cache clearing
 * during application startup and shutdown.</p>
 * 
 * <p>Cache Invalidation Strategy:</p>
 * <ul>
 *   <li>On Startup: All caches are cleared to ensure a clean state</li>
 *   <li>On Shutdown: All caches are cleared to prevent stale data on next startup</li>
 *   <li>On Demand: Caches can be cleared via the clearAllCaches() method</li>
 * </ul>
 * 
 * <p>Cache Types:</p>
 * <ul>
 *   <li>address: Individual address cache (keyed by ID)</li>
 *   <li>addresses: Complete list of all addresses</li>
 *   <li>userAddresses: Addresses grouped by user ID</li>
 * </ul>
 */

/**
 * Service for managing caches.
 * Implements DisposableBean to clean up caches on application shutdown.
 */
@Slf4j
@Service
public class CacheService implements DisposableBean {
    
    // Redis template for direct Redis operations
    // This template is used for low-level Redis operations, such as flushing the database
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Spring's cache manager for managing all caches
    // This cache manager is responsible for managing all caches defined with @Cacheable, @CachePut, and @CacheEvict
    private final CacheManager cacheManager;
    
    // Flag to track if the application is shutting down
    // This flag is used to prevent cache clearing during shutdown, as it's handled separately
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    
    // Factory for creating Redis connections
    // This factory is used to create Redis connections for low-level Redis operations
    private final RedisConnectionFactory redisConnectionFactory;
    
    // Configuration for retry behavior
    // These constants define the number of retry attempts and the delay between retries for Redis operations
    private static final int REDIS_RETRY_ATTEMPTS = 3;
    private static final long REDIS_RETRY_DELAY_MS = 100; // 100ms between retries

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate, 
                       CacheManager cacheManager,
                       RedisConnectionFactory redisConnectionFactory) {
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
        this.redisConnectionFactory = redisConnectionFactory;
    }
    
    /**
     * Clears all caches in the application.
     * This can be called on startup or manually when needed.
     */
    /**
     * Clears all caches in the application.
     * This can be called manually when needed.
     */
    /**
     * Clears all caches in the application.
     * This method is safe to call multiple times and will not clear caches during shutdown.
     * 
     * <p>Clears both Spring-managed caches and Redis caches.</p>
     * 
     * @throws IllegalStateException if there's an error clearing caches (only if not shutting down)
     */
    public void clearAllCaches() {
        // Skip if we're already in the process of shutting down
        if (isShuttingDown.get()) {
            System.out.println("⚠️  Skipping cache clear - application is shutting down");
            return;
        }

        try {
            log.info("🔄 Starting cache clearing process...");
            
            // Clear Spring caches first
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    try {
                        Cache cache = cacheManager.getCache(cacheName);
                        if (cache != null) {
                            cache.clear();
                            System.out.println("✅ Cleared Spring cache: " + cacheName);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️  Error clearing Spring cache " + cacheName + ": " + e.getMessage());
                    }
                });
            }
            
            // Then clear Redis
            if (redisTemplate != null && redisConnectionFactory != null) {
                try {
                    redisTemplate.getConnectionFactory().getConnection().flushDb();
                    System.out.println("✅ Cleared all Redis keys");
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("STOPPED")) {
                        System.out.println("ℹ️  Redis connection already closed, skipping Redis cache clear");
                    } else {
                        throw e;
                    }
                }
            }
            
            System.out.println("✨ Successfully cleared all caches");
        } catch (Exception e) {
            System.err.println("❌ Error during cache clearing: " + e.getMessage());
            if (!isShuttingDown.get()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Called by Spring when the application is shutting down.
     * Cleans up all caches before the application stops.
     */
    /**
     * Initializes the cache service.
     * 
     * <p>This method is called by Spring after dependency injection is done.
     * It performs the following actions:</p>
     * <ol>
     *   <li>Clears all caches to ensure a clean startup state</li>
     *   <li>Registers a JVM shutdown hook to clear caches on shutdown</li>
     * </ol>
     * 
     * @see #clearAllCaches()
     * @see #clearAllCachesWithRetry()
     */
    @PostConstruct
    public void init() {
        log.info("🏁 Initializing caches...");
        // Clear caches on startup to ensure a clean state
        clearAllCaches();
        
        // Register a shutdown hook to clear caches when the JVM shuts down
        // This is a safety net in case the @PreDestroy method doesn't get called
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🔔 JVM Shutdown Hook triggered - cleaning up caches...");
            clearAllCachesWithRetry();
        }));
    }
    
    /**
     * Handles application shutdown.
     * 
     * <p>This method is called by Spring when the application is shutting down.
     * It sets the shutdown flag and attempts to clear all caches with retry logic.</p>
     * 
     * <p>The shutdown flag prevents new cache operations from starting during shutdown.</p>
     * 
     * @see #clearAllCachesWithRetry()
     */
    @PreDestroy
    public void onShutdown() {
        // Set the shutdown flag to prevent new cache operations
        isShuttingDown.set(true);
        log.info("🛑 Application shutdown detected, clearing caches...");
        // Clear caches with retry logic to ensure best effort cleanup
        clearAllCachesWithRetry();
    }
    
    /**
     * Implementation of DisposableBean interface.
     * 
     * <p>This method is called by the Spring container during application shutdown.
     * It delegates to the onShutdown() method to maintain compatibility with
     * both @PreDestroy and DisposableBean lifecycle callbacks.</p>
     * 
     * @see #onShutdown()
     */
    @Override
    public void destroy() {
        // Delegate to onShutdown to avoid code duplication
        onShutdown();
    }
    
    /**
     * Clears all caches with retry logic for better reliability.
     * This is called during application shutdown.
     */
    /**
     * Clears all Spring-managed caches.
     */
    /**
     * Clears all caches managed by Spring's CacheManager.
     * This includes all caches defined with @Cacheable, @CachePut, and @CacheEvict.
     */
    private void clearSpringCaches() {
        if (cacheManager == null) {
            System.out.println("ℹ️  CacheManager is not available - skipping Spring cache clearing");
            return;
        }
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    System.out.println("✅ Cleared Spring cache: " + cacheName);
                }
            } catch (Exception e) {
                log.error("⚠️  Failed to clear Spring cache {}: {}", cacheName, e.getMessage());
            }
        });
    }
    
    /**
     * Clears all Redis caches.
     */
    /**
     * Clears all caches in the Redis data store.
     * 
     * <p>This method performs the following actions:</p>
     * <ol>
     *   <li>Checks if Redis is properly configured</li>
     *   <li>Opens a new Redis connection</li>
     *   <li>Executes the FLUSHDB command to clear the current database</li>
     *   <li>Handles the case when Redis is already stopped during shutdown</li>
     * </ol>
     * 
     * <p>Note: This is a destructive operation that removes all keys from the current Redis database.</p>
     * 
     * @throws IllegalStateException if there's an error clearing Redis caches (only if not shutting down)
     */
    private void clearRedisCaches() {
        // Check if Redis is properly configured
        if (redisTemplate == null || redisConnectionFactory == null) {
            log.warn("Redis is not configured - skipping Redis cache clearing");
            return;
        }
        
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            // Clear the current Redis database
            connection.flushDb();
            log.info("Successfully cleared all Redis caches");
            log.info("✅ Successfully cleared all Redis caches");
        } catch (IllegalStateException e) {
            // Handle the case when Redis is already stopped
            if (e.getMessage() != null && e.getMessage().contains("STOPPED")) {
                log.info("Redis connection already closed - skipping Redis cache clearing");
            } else {
                // Log and rethrow unexpected IllegalStateException if not shutting down
                log.error("Failed to clear Redis caches: {}", e.getMessage());
                if (!isShuttingDown.get()) {
                    throw e;
                }
            }
        } catch (Exception e) {
            // Log and rethrow other exceptions if not shutting down
            System.err.println("⚠️  Failed to clear Redis caches: " + e.getMessage());
            if (!isShuttingDown.get()) {
                throw e;
            }
        }
    }
    
    /**
     * Clears all caches with retry logic for better reliability.
     * This is called during application shutdown.
     */
    private void clearAllCachesWithRetry() {
        int attempt = 0;
        final int maxAttempts = 3;
        final long retryDelayMs = 100; // 100ms between retries
        
        while (attempt < maxAttempts) {
            try {
                attempt++;
                log.info("🔄 Attempt {} to clear caches...", attempt);
                
                // Clear Spring caches first
                clearSpringCaches();
                
                // Then clear Redis
                clearRedisCaches();
                
                log.info("✅ Successfully cleared all caches (attempt {})", attempt);
                return; // Success - exit the retry loop
                
            } catch (Exception e) {
                log.warn("Cache clear attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt >= maxAttempts) {
                    log.error("❌ Failed to clear caches after {} attempts", maxAttempts);
                    return;
                }
                
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Cache clear operation was interrupted");
                    return;
                }
            }
        }
    }
}
