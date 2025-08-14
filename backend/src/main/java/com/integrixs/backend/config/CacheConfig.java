package com.integrixs.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

/**
 * Memory-aware caching configuration with automatic eviction.
 * 
 * <p>Implements memory-sensitive caching to prevent memory leaks
 * and ensure efficient garbage collection.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Primary cache manager with memory-aware eviction policies.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCaffeineConfig());
        cacheManager.setAllowNullValues(false);
        
        // Register specific caches with custom configurations
        cacheManager.registerCustomCache("flows", flowsCacheConfig().build());
        cacheManager.registerCustomCache("adapters", adaptersCacheConfig().build());
        cacheManager.registerCustomCache("users", usersCacheConfig().build());
        cacheManager.registerCustomCache("businessComponents", businessComponentsCacheConfig().build());
        
        return cacheManager;
    }
    
    /**
     * Session cache with short TTL for security.
     */
    @Bean
    public CacheManager sessionCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("sessions", "tokens");
        cacheManager.setCaffeine(sessionCaffeineConfig());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
    
    /**
     * Query result cache manager for database query caching.
     */
    @Bean
    public CacheManager queryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(queryCacheConfig());
        cacheManager.setAllowNullValues(false);
        
        // Register query-specific caches
        Arrays.asList(
            "flowsByStatus",
            "flowsByUser",
            "activeFlows",
            "adaptersByType",
            "userPermissions",
            "componentAdapters"
        ).forEach(cacheName -> 
            cacheManager.registerCustomCache(cacheName, queryCacheConfig().build())
        );
        
        return cacheManager;
    }
    
    /**
     * Default Caffeine configuration with memory-aware settings.
     */
    private Caffeine<Object, Object> defaultCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumWeight(100_000_000) // 100MB max weight
                .weigher((key, value) -> estimateSize(value))
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .weakKeys() // Allow keys to be garbage collected
                .softValues() // Allow values to be garbage collected under memory pressure
                .recordStats()
                .removalListener(new LoggingRemovalListener());
    }
    
    /**
     * Session cache configuration with strict security settings.
     */
    private Caffeine<Object, Object> sessionCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(15, TimeUnit.MINUTES) // Short TTL for sessions
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        log.debug("Session cache entry expired: {}", key);
                    }
                });
    }
    
    /**
     * Query cache configuration for database results.
     */
    private Caffeine<Object, Object> queryCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(5, TimeUnit.MINUTES) // Short TTL for query results
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .recordStats()
                .removalListener(new LoggingRemovalListener());
    }
    
    /**
     * Flows cache configuration.
     */
    private Caffeine<Object, Object> flowsCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats();
    }
    
    /**
     * Adapters cache configuration.
     */
    private Caffeine<Object, Object> adaptersCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .recordStats();
    }
    
    /**
     * Users cache configuration.
     */
    private Caffeine<Object, Object> usersCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats();
    }
    
    /**
     * Business components cache configuration.
     */
    private Caffeine<Object, Object> businessComponentsCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES) // Longer TTL for rarely changing data
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats();
    }
    
    /**
     * Estimate object size for cache weight calculation.
     */
    private int estimateSize(Object value) {
        if (value == null) return 1;
        
        // Basic size estimation - can be refined based on actual objects
        if (value instanceof String) {
            return ((String) value).length() * 2; // 2 bytes per char
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        } else if (value instanceof WeakReference) {
            Object ref = ((WeakReference<?>) value).get();
            return ref != null ? estimateSize(ref) : 1;
        } else {
            // Default estimation: 100 bytes per object
            return 100;
        }
    }
    
    /**
     * Removal listener for cache debugging and monitoring.
     */
    private static class LoggingRemovalListener implements RemovalListener<Object, Object> {
        @Override
        public void onRemoval(Object key, Object value, RemovalCause cause) {
            if (cause == RemovalCause.SIZE) {
                log.warn("Cache entry evicted due to size limit: key={}", key);
            } else if (cause == RemovalCause.COLLECTED) {
                log.debug("Cache entry garbage collected: key={}", key);
            }
        }
    }
    
    /**
     * Bean for monitoring cache statistics.
     */
    @Bean
    public CacheStatsMonitor cacheStatsMonitor(CacheManager cacheManager) {
        return new CacheStatsMonitor(cacheManager);
    }
    
    /**
     * Monitor for cache statistics and memory usage.
     */
    public static class CacheStatsMonitor {
        private final CacheManager cacheManager;
        
        public CacheStatsMonitor(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }
        
        @Scheduled(fixedDelay = 300000) // Every 5 minutes
        public void logCacheStats() {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                    var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                    var stats = caffeineCache.stats();
                    
                    if (stats.requestCount() > 0) {
                        log.info("Cache '{}' stats - Hit rate: {:.2f}%, Evictions: {}, Size: {}",
                                cacheName,
                                stats.hitRate() * 100,
                                stats.evictionCount(),
                                caffeineCache.estimatedSize());
                    }
                }
            });
        }
        
        /**
         * Clear cache entries that haven't been accessed recently.
         */
        public void cleanupStaleEntries() {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                    var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                    caffeineCache.cleanUp();
                }
            });
        }
    }
}